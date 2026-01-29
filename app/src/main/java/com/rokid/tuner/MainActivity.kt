package com.rokid.tuner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rokid.tuner.audio.AudioRecorder
import com.rokid.tuner.pitch.PitchDetector
import com.rokid.tuner.ui.SettingsDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {

    private lateinit var noteTextView: TextView
    private lateinit var frequencyTextView: TextView
    private lateinit var centsTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var mainLayout: LinearLayout

    private var audioRecorder: AudioRecorder? = null
    private var pitchDetector: PitchDetector? = null
    private var tuningJob: Job? = null
    @Volatile private var isTuning = false
    private var activityStartTime: Long = 0
    private var settingsDialog: SettingsDialog? = null
    private var currentRms = 0.0
    private var currentSensitivity = DEFAULT_SENSITIVITY
    private var consecutiveNullReads = 0
    private val MAX_CONSECUTIVE_NULL_READS = 10
    private var lastValidPitchResult: PitchDetector.PitchResult? = null
    private var lastValidPitchTime: Long = 0
    @Volatile private var displayDelayMs = DEFAULT_DISPLAY_DELAY_MS // Default 300ms display delay
    private val DISPLAY_DELAY_MIN = 0L
    private val DISPLAY_DELAY_MAX = 1000L
    @Volatile private var pitchUpdateDelayMs = DEFAULT_PITCH_UPDATE_DELAY_MS // Default 100ms delay between pitch updates
    private val PITCH_UPDATE_DELAY_MIN = 0L
    private val PITCH_UPDATE_DELAY_MAX = 1000L
    private var lastPitchUpdateTime: Long = 0

    private companion object {
        private const val TAG = "MainActivity"
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1001
        private const val DEBUG = true
        private const val DEFAULT_SENSITIVITY = 100 // 0-100 scale, higher = more sensitive (100 = maximum sensitivity)
        private const val DEFAULT_DISPLAY_DELAY_MS = 1000L // Default 1000ms display delay
        private const val DEFAULT_PITCH_UPDATE_DELAY_MS = 200L // Default 200ms delay between pitch updates
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        initViews()
        setupClickListeners()
        
        activityStartTime = System.currentTimeMillis()
        
        // Check permission on create
        if (!hasAudioPermission()) {
            requestAudioPermission()
        }
        

        
        // Don't start tuning here - wait for onResume
    }

    private fun initViews() {
        noteTextView = findViewById(R.id.note_text)
        frequencyTextView = findViewById(R.id.frequency_text)
        centsTextView = findViewById(R.id.cents_text)
        statusTextView = findViewById(R.id.status_text)
        mainLayout = findViewById(R.id.main_layout)
    }



    private fun setupClickListeners() {
        mainLayout.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun startTuning() {
        if (isTuning) return

        try {
            Log.d(TAG, "Starting tuning...")
            Log.d(TAG, "Current sensitivity: $currentSensitivity")
            audioRecorder = AudioRecorder()

             pitchDetector = PitchDetector()
             pitchDetector?.setSensitivity(currentSensitivity)

                audioRecorder?.start()
                isTuning = true
                 lastValidPitchResult = null
                 lastValidPitchTime = 0
                 lastPitchUpdateTime = 0
                 consecutiveNullReads = 0
                statusTextView.text = getString(R.string.in_tune)


            tuningJob = lifecycleScope.launch(Dispatchers.IO) {
                Log.d(TAG, "Tuning coroutine started")
                var iteration = 0
                while (isActive && isTuning) {
                    iteration++
                     val debug = iteration <= 10 || iteration % 20 == 0 || iteration % 100 == 0
                    
                    if (debug) Log.d(TAG, "Loop iteration $iteration, isTuning=$isTuning")
                    
                     val audioData = audioRecorder?.readNext()
                      if (audioData == null) {
                          consecutiveNullReads++
                          if (debug) Log.d(TAG, "No audio data received, consecutive: $consecutiveNullReads")
                          if (consecutiveNullReads >= MAX_CONSECUTIVE_NULL_READS) {
                              Log.w(TAG, "Too many consecutive null reads ($consecutiveNullReads), restarting audio recorder")
                              restartAudioRecorder()
                          }
                           withContext(Dispatchers.Main) {
                               currentRms = 0.0
                               updateRmsInDialog()
                           }
                       } else {
                           consecutiveNullReads = 0
                          if (debug) Log.d(TAG, "Audio data size: ${audioData.size}")
                          val rms = computeRMS(audioData)
                          if (debug) Log.d(TAG, "RMS: $rms")
                         withContext(Dispatchers.Main) {
                              currentRms = rms
                              updateRmsInDialog()
                         }
                          val pitchResult = pitchDetector?.detectPitch(audioData)
                            if (pitchResult == null) {
                                if (debug) Log.d(TAG, "No pitch detected, RMS: $rms")
                                val currentTime = System.currentTimeMillis()
                                val timeSinceLastValid = currentTime - lastValidPitchTime
                                
                                if (lastValidPitchResult != null && timeSinceLastValid <= displayDelayMs) {
                                    // Still within display delay, show last valid result
                                    if (debug) Log.d(TAG, "Within display delay ($timeSinceLastValid ms <= $displayDelayMs ms), showing last valid pitch")
                                    withContext(Dispatchers.Main) {
                                        updateTunerDisplay(lastValidPitchResult!!)
                                    }
                                } else {
                                    // Delay expired or no valid result, show listening
                                    withContext(Dispatchers.Main) {
                                        // Clear display or show listening
                                        noteTextView.text = "--"
                                        frequencyTextView.text = ""
                                        centsTextView.text = ""
                                        statusTextView.text = "Listening"
                                    }
                                }
                            } else {
                                 Log.d(TAG, "Pitch detected: ${pitchResult.noteName} at ${pitchResult.frequency} Hz")
                                 // Update last valid result and timestamp (for display delay)
                                 lastValidPitchResult = pitchResult
                                 lastValidPitchTime = System.currentTimeMillis()
                                 
                                 // Check if enough time has passed since last UI update
                                 val currentTime = System.currentTimeMillis()
                                 val timeSinceLastUpdate = currentTime - lastPitchUpdateTime
                                 
                                 if (timeSinceLastUpdate >= pitchUpdateDelayMs) {
                                     // Update UI
                                     lastPitchUpdateTime = currentTime
                                     withContext(Dispatchers.Main) {
                                         updateTunerDisplay(pitchResult)
                                     }
                                 } else {
                                     if (debug) Log.d(TAG, "Skipping UI update (${timeSinceLastUpdate}ms < ${pitchUpdateDelayMs}ms)")
                                 }
                            }
                     }
                    delay(50) // Update ~20 times per second
                }
                Log.d(TAG, "Tuning loop ended")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in startTuning", e)
            statusTextView.text = "Error: ${e.message}"
            stopTuning()
        }
    }

    private fun stopTuning() {
        Log.e(TAG, "Stopping tuning...", Throwable("Stack trace"))
        lifecycleScope.launch {
            isTuning = false
            tuningJob?.cancel()
            audioRecorder?.stop()
            audioRecorder = null
            pitchDetector = null
            lastValidPitchResult = null
            lastValidPitchTime = 0
            lastPitchUpdateTime = 0
            
             withContext(Dispatchers.Main) {
                  statusTextView.text = ""
                  currentRms = 0.0
                  updateRmsInDialog()
                  
                  // Reset tuner display
                  noteTextView.text = "--"
                  frequencyTextView.text = ""
                  centsTextView.text = ""
              }
            Log.d(TAG, "Tuning stopped")
        }
    }

    private suspend fun restartAudioRecorder() {
        Log.w(TAG, "Restarting audio recorder due to consecutive null reads")
        audioRecorder?.stop()
        audioRecorder = AudioRecorder()
        try {
            audioRecorder?.start()
            consecutiveNullReads = 0
            Log.d(TAG, "Audio recorder restarted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart audio recorder", e)
            // If restart fails, stop tuning
            withContext(Dispatchers.Main) {
                statusTextView.text = "Audio error"
            }
            stopTuning()
        }
    }

    private fun computeRMS(audioData: FloatArray): Double {
        var sum = 0.0
        for (sample in audioData) {
            sum += sample * sample
        }
        return Math.sqrt(sum / audioData.size)
    }

    private fun updateTunerDisplay(result: PitchDetector.PitchResult) {
        noteTextView.text = result.noteName
        frequencyTextView.text = "${String.format("%.1f", result.frequency)} Hz"
        centsTextView.text = if (result.cents > 0) {
            "+${String.format("%.1f", result.cents)}"
        } else {
            String.format("%.1f", result.cents)
        }
        
        // Update status based on cents
        val absCents = Math.abs(result.cents)
        statusTextView.text = when {
            absCents < 5 -> getString(R.string.in_tune)
            result.cents > 0 -> getString(R.string.sharp_indicator)
            else -> getString(R.string.flat_indicator)
        }
    }

    private fun showSettingsDialog() {
        // Don't show another dialog if one is already showing
        if (settingsDialog?.isAdded == true) {
            return
        }
        
          settingsDialog = SettingsDialog.newInstance(
              sensitivity = currentSensitivity,
              displayDelayMs = displayDelayMs,
              pitchUpdateDelayMs = pitchUpdateDelayMs,
              onReferenceFrequencyChanged = { referenceFrequency ->
                  // Update pitch detector with new reference frequency
                  pitchDetector?.setReferenceFrequency(referenceFrequency)
              },
              onSensitivityChanged = { sensitivity ->
                  // Update pitch detector with new sensitivity
                  currentSensitivity = sensitivity
                  pitchDetector?.setSensitivity(sensitivity)
              },
              onDisplayDelayChanged = { delayMs ->
                  // Update display delay
                  displayDelayMs = delayMs
              },
              onPitchUpdateDelayChanged = { delayMs ->
                  // Update pitch update delay
                  pitchUpdateDelayMs = delayMs
              }
          ).apply {
            onDismissListener = {
                settingsDialog = null
            }
            show(supportFragmentManager, "settings")
        }
    }

    private fun updateRmsInDialog() {
        settingsDialog?.updateRmsValue(currentRms)
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, can start tuning if button was pressed
                if (!isTuning) {
                    startTuning()
                }
            } else {
                // Permission denied
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.no_audio_permission))
                    .setMessage(getString(R.string.permission_rationale))
                    .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                        requestAudioPermission()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause called, isTuning=$isTuning")
        super.onPause()
        // Don't stop tuning on pause, only on stop
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called, isTuning=$isTuning")
        if (!isTuning && hasAudioPermission()) {
            Log.d(TAG, "Auto-starting tuning in onResume")
            startTuning()
        }
    }
    
    override fun onStop() {
        Log.d(TAG, "onStop called, isTuning=$isTuning")
        super.onStop()
        val elapsed = System.currentTimeMillis() - activityStartTime
        if (elapsed < 2000) {
            Log.d(TAG, "Activity stopped too soon ($elapsed ms), not stopping tuning")
            return
        }
        if (isTuning) {
            Log.d(TAG, "Calling stopTuning from onStop")
            stopTuning()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called, isTuning=$isTuning")
        super.onDestroy()
        stopTuning()
    }
}