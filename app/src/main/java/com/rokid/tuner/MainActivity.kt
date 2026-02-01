package com.rokid.tuner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rokid.tuner.constants.UiConstants
import com.rokid.tuner.pitch.PitchDetector
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var noteTextView: TextView
    private lateinit var frequencyTextView: TextView
    private lateinit var centsTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var mainLayout: LinearLayout

    private val viewModel: TunerViewModel by viewModels()
    
    private var activityStartTime: Long = UiConstants.INITIAL_TIME

    private companion object {
        private const val TAG = "MainActivity"
        private const val AUDIO_PERMISSION_REQUEST_CODE = UiConstants.AUDIO_PERMISSION_REQUEST_CODE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        observeViewModel()
        
        activityStartTime = System.currentTimeMillis()
        
        if (!hasAudioPermission()) {
            requestAudioPermission()
        }
    }

    private fun initViews() {
        noteTextView = findViewById(R.id.note_text)
        frequencyTextView = findViewById(R.id.frequency_text)
        centsTextView = findViewById(R.id.cents_text)
        statusTextView = findViewById(R.id.status_text)
        mainLayout = findViewById(R.id.main_layout)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tuningState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: TunerViewModel.TuningState) {
        when (state) {
            is TunerViewModel.TuningState.Idle -> {
                noteTextView.text = getString(R.string.note_placeholder)
                frequencyTextView.text = ""
                centsTextView.text = ""
                statusTextView.text = ""
            }
            is TunerViewModel.TuningState.Listening -> {
                noteTextView.text = getString(R.string.note_placeholder)
                frequencyTextView.text = ""
                centsTextView.text = ""
                statusTextView.text = getString(R.string.listening)
            }
            is TunerViewModel.TuningState.Detected -> {
                updateTunerDisplay(state.result)
            }
            is TunerViewModel.TuningState.Error -> {
                noteTextView.text = getString(R.string.note_placeholder)
                frequencyTextView.text = ""
                centsTextView.text = ""
                statusTextView.text = getString(R.string.error_format, state.message)
            }
        }
    }

    private fun updateTunerDisplay(result: PitchDetector.PitchResult) {
        noteTextView.text = result.noteName
        frequencyTextView.text = String.format("%.1f Hz", result.frequency)
        centsTextView.text = if (result.cents > 0) {
            String.format("+%.1f", result.cents)
        } else {
            String.format("%.1f", result.cents)
        }
        
        val status = viewModel.getTuningStatus(result)
        statusTextView.text = when (status) {
            TunerViewModel.TuningStatus.IN_TUNE -> getString(R.string.in_tune)
            TunerViewModel.TuningStatus.SHARP -> getString(R.string.sharp_indicator)
            TunerViewModel.TuningStatus.FLAT -> getString(R.string.flat_indicator)
            TunerViewModel.TuningStatus.LISTENING -> getString(R.string.listening)
        }
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
                if (!viewModel.isRunning.value) {
                    viewModel.startTuning()
                }
            } else {
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
        Log.d(TAG, "onPause called")
        super.onPause()
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called, isRunning=${viewModel.isRunning.value}")
        if (!viewModel.isRunning.value && hasAudioPermission()) {
            Log.d(TAG, "Auto-starting tuning in onResume")
            viewModel.startTuning()
        }
    }
    
    override fun onStop() {
        Log.d(TAG, "onStop called")
        super.onStop()
        val elapsed = System.currentTimeMillis() - activityStartTime
        if (elapsed < UiConstants.MIN_ACTIVITY_LIFETIME_MS) {
            Log.d(TAG, "Activity stopped too soon ($elapsed ms), not stopping tuning")
            return
        }
        if (viewModel.isRunning.value) {
            Log.d(TAG, "Stopping tuning from onStop")
            viewModel.stopTuning()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        super.onDestroy()
        // ViewModel will clean up in onCleared()
    }
}
