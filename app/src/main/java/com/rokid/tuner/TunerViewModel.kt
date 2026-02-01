package com.rokid.tuner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokid.tuner.audio.AudioRecorder
import com.rokid.tuner.constants.UiConstants
import com.rokid.tuner.pitch.PitchDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ViewModel for managing tuner state and business logic.
 * Separates UI concerns from audio processing and pitch detection.
 */
class TunerViewModel : ViewModel() {

    companion object {
        private const val TAG = "TunerViewModel"
        private val DEBUG = UiConstants.DEBUG
    }

    // Tuning state
    sealed class TuningState {
        object Idle : TuningState()
        object Listening : TuningState()
        data class Detected(val result: PitchDetector.PitchResult) : TuningState()
        data class Error(val message: String) : TuningState()
    }

    // Tuning status for UI display
    enum class TuningStatus {
        IN_TUNE, SHARP, FLAT, LISTENING
    }

    // State flows for UI observation
    private val _tuningState = MutableStateFlow<TuningState>(TuningState.Idle)
    val tuningState: StateFlow<TuningState> = _tuningState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _currentRms = MutableStateFlow(0.0)
    val currentRms: StateFlow<Double> = _currentRms.asStateFlow()

    // Internal state
    private var audioRecorder: AudioRecorder? = null
    private var pitchDetector: PitchDetector? = null
    private var tuningJob: Job? = null
    private val isTuning = AtomicBoolean(false)
    private val tuningLock = Any()

    // Configuration
    private var sensitivity = UiConstants.DEFAULT_SENSITIVITY
    private val inTuneThresholdCents = UiConstants.DEFAULT_IN_TUNE_THRESHOLD_CENTS
    private val displayDelayMs = UiConstants.DEFAULT_DISPLAY_DELAY_MS
    private val pitchUpdateDelayMs = UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS

    // Timing state
    private var lastValidPitchResult: PitchDetector.PitchResult? = null
    private var lastValidPitchTime: Long = UiConstants.INITIAL_TIME
    private var lastPitchUpdateTime: Long = UiConstants.INITIAL_TIME
    private var consecutiveNullReads = UiConstants.INITIAL_NULL_READS

    /**
     * Starts the tuning process.
     * Call this when the user grants audio permission and the activity is ready.
     */
    fun startTuning() {
        synchronized(tuningLock) {
            if (isTuning.get()) {
                Log.d(TAG, "Tuning already running")
                return
            }

            try {
                Log.d(TAG, "Starting tuning with sensitivity: $sensitivity")

                audioRecorder = AudioRecorder()
                pitchDetector = PitchDetector().apply {
                    setSensitivity(sensitivity)
                }

                audioRecorder?.start()
                isTuning.set(true)
                _isRunning.value = true
                
                resetTimingState()

                tuningJob = viewModelScope.launch(Dispatchers.IO) {
                    runTuningLoop()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting tuning", e)
                _tuningState.value = TuningState.Error(e.message ?: "Unknown error")
                stopTuning()
            }
        }
    }

    /**
     * Stops the tuning process and releases resources.
     */
    fun stopTuning() {
        Log.d(TAG, "Stopping tuning...")
        synchronized(tuningLock) {
            if (!isTuning.get()) return

            isTuning.set(false)
            _isRunning.value = false
            
            tuningJob?.cancel()
            tuningJob = null

            audioRecorder?.stop()
            audioRecorder = null

            pitchDetector?.reset()
            pitchDetector = null

            resetTimingState()
        }
        
        _tuningState.value = TuningState.Idle
        _currentRms.value = 0.0
        Log.d(TAG, "Tuning stopped")
    }

    /**
     * Sets the sensitivity level (0-100).
     * Higher values = more sensitive detection.
     */
    fun setSensitivity(value: Int) {
        sensitivity = value.coerceIn(UiConstants.MIN_SENSITIVITY, UiConstants.MAX_SENSITIVITY)
        pitchDetector?.setSensitivity(sensitivity)
    }

    /**
     * Determines the tuning status based on cents deviation.
     */
    fun getTuningStatus(result: PitchDetector.PitchResult): TuningStatus {
        val absCents = Math.abs(result.cents)
        return when {
            absCents < inTuneThresholdCents -> TuningStatus.IN_TUNE
            result.cents > 0 -> TuningStatus.SHARP
            else -> TuningStatus.FLAT
        }
    }

    private fun resetTimingState() {
        lastValidPitchResult = null
        lastValidPitchTime = UiConstants.INITIAL_TIME
        lastPitchUpdateTime = UiConstants.INITIAL_TIME
        consecutiveNullReads = UiConstants.INITIAL_NULL_READS
    }

    private suspend fun runTuningLoop() {
        Log.d(TAG, "Tuning loop started")
        var iteration = 0

        while (isTuning.get()) {
            // Check if the coroutine is still active
            currentCoroutineContext().ensureActive()
            
            iteration++
            val debug = DEBUG && (iteration <= UiConstants.DEBUG_ITERATION_THRESHOLD ||
                    iteration % UiConstants.DEBUG_ITERATION_MOD_100 == 0)

            if (debug) Log.d(TAG, "Loop iteration $iteration")

            val audioData = audioRecorder?.readNext()

            if (audioData == null) {
                handleNullAudioData(debug)
            } else {
                handleAudioData(audioData, debug)
            }

            delay(UiConstants.TUNING_LOOP_DELAY_MS)
        }
        Log.d(TAG, "Tuning loop ended")
    }

    private fun handleNullAudioData(debug: Boolean) {
        consecutiveNullReads++
        if (debug) Log.d(TAG, "No audio data, consecutive: $consecutiveNullReads")

        if (consecutiveNullReads >= UiConstants.MAX_CONSECUTIVE_NULL_READS) {
            Log.w(TAG, "Too many null reads, restarting audio recorder")
            restartAudioRecorder()
        }
        _currentRms.value = 0.0
    }

    private fun handleAudioData(audioData: FloatArray, debug: Boolean) {
        consecutiveNullReads = UiConstants.INITIAL_NULL_READS

        val rms = pitchDetector?.computeRMS(audioData) ?: 0.0
        _currentRms.value = rms

        if (debug) Log.d(TAG, "Audio size: ${audioData.size}, RMS: $rms")

        val pitchResult = pitchDetector?.detectPitch(audioData)

        if (pitchResult == null) {
            handleNoPitchDetected(debug)
        } else {
            handlePitchDetected(pitchResult, debug)
        }
    }

    private fun handleNoPitchDetected(debug: Boolean) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastValid = currentTime - lastValidPitchTime

        if (lastValidPitchResult != null && timeSinceLastValid <= displayDelayMs) {
            // Still within display delay, keep showing last result
            if (debug) Log.d(TAG, "Within display delay, showing last pitch")
            _tuningState.value = TuningState.Detected(lastValidPitchResult!!)
        } else {
            // Show listening state
            _tuningState.value = TuningState.Listening
        }
    }

    private fun handlePitchDetected(pitchResult: PitchDetector.PitchResult, debug: Boolean) {
        Log.d(TAG, "Pitch: ${pitchResult.noteName} at ${pitchResult.frequency} Hz")

        lastValidPitchResult = pitchResult
        lastValidPitchTime = System.currentTimeMillis()

        val currentTime = System.currentTimeMillis()
        val timeSinceLastUpdate = currentTime - lastPitchUpdateTime

        if (timeSinceLastUpdate >= pitchUpdateDelayMs) {
            lastPitchUpdateTime = currentTime
            _tuningState.value = TuningState.Detected(pitchResult)
        } else if (debug) {
            Log.d(TAG, "Skipping update (${timeSinceLastUpdate}ms < ${pitchUpdateDelayMs}ms)")
        }
    }

    private fun restartAudioRecorder() {
        synchronized(tuningLock) {
            try {
                audioRecorder?.stop()
                audioRecorder = AudioRecorder()
                audioRecorder?.start()
                consecutiveNullReads = UiConstants.INITIAL_NULL_READS
                Log.d(TAG, "Audio recorder restarted")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restart audio recorder", e)
                _tuningState.value = TuningState.Error("Audio error")
                stopTuning()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTuning()
    }
}
