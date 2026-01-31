package com.rokid.tuner.pitch

import android.util.Log
import com.rokid.tuner.audio.AudioConfig
import com.rokid.tuner.constants.AlgorithmConstants
import com.rokid.tuner.constants.MusicalConstants
import com.rokid.tuner.constants.UiConstants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PitchDetector {

    companion object {
        private const val TAG = "PitchDetector"
        private const val DEFAULT_MIN_RMS_THRESHOLD = AlgorithmConstants.DEFAULT_MIN_RMS_THRESHOLD // 0.055% of max amplitude (sensitive to short peaks but rejects constant low noise)
        private const val DEFAULT_CLARITY_THRESHOLD = AlgorithmConstants.DEFAULT_CLARITY_THRESHOLD // Very tolerant clarity threshold (lower dPrime = clearer pitch)
        private const val DEFAULT_PROBABILITY_THRESHOLD = AlgorithmConstants.DEFAULT_PROBABILITY_THRESHOLD // Very tolerant probability threshold
        private const val DEBUG = UiConstants.DEBUG
        
        /**
         * Sensitivity mapping (0-100 scale to thresholds)
         * - Higher sensitivity number = more sensitive detection
         * - Sensitivity 0: least sensitive (strict thresholds)
         * - Sensitivity 100: most sensitive (lenient thresholds)
         * Default sensitivity is ${UiConstants.DEFAULT_SENSITIVITY} (maximum) in MainActivity.
         */
        
        // Sensitivity mapping (0-100 scale to thresholds)
        fun rmsThresholdFromSensitivity(sensitivity: Int): Double {
            // Higher sensitivity number = more sensitive = lower threshold
            // sensitivity 0 -> highest threshold (MAX_RMS_THRESHOLD), sensitivity 100 -> lowest threshold (DEFAULT_MIN_RMS_THRESHOLD)
            return AlgorithmConstants.MAX_RMS_THRESHOLD - (sensitivity / UiConstants.MAX_SENSITIVITY.toDouble()) * AlgorithmConstants.RMS_THRESHOLD_SCALE
        }
        
        fun clarityThresholdFromSensitivity(sensitivity: Int): Double {
            // Higher sensitivity = more tolerant = higher clarity threshold
            // sensitivity 0 -> strict (MIN_CLARITY_THRESHOLD), sensitivity 100 -> tolerant (DEFAULT_CLARITY_THRESHOLD)
            return AlgorithmConstants.MIN_CLARITY_THRESHOLD + (sensitivity / UiConstants.MAX_SENSITIVITY.toDouble()) * AlgorithmConstants.CLARITY_THRESHOLD_SCALE
        }
        
        fun probabilityThresholdFromSensitivity(sensitivity: Int): Float {
            // Higher sensitivity = more tolerant = lower probability threshold
            // sensitivity 0 -> strict (MAX_PROBABILITY_THRESHOLD), sensitivity 100 -> tolerant (DEFAULT_PROBABILITY_THRESHOLD)
            return AlgorithmConstants.MAX_PROBABILITY_THRESHOLD - (sensitivity / UiConstants.MAX_SENSITIVITY.toFloat()) * AlgorithmConstants.PROBABILITY_THRESHOLD_SCALE
        }
    }

    private var currentPitchResult: PitchResult? = null
    private var referenceFrequency = AudioConfig.DEFAULT_REFERENCE_FREQUENCY
    private var minRmsThreshold = DEFAULT_MIN_RMS_THRESHOLD
    private var clarityThreshold = DEFAULT_CLARITY_THRESHOLD
    private var probabilityThreshold = DEFAULT_PROBABILITY_THRESHOLD
    private val mutex = Mutex()
    private val noteFinder = NoteFinder()
    
    private fun computeRMS(audioData: FloatArray): Double {
        var sum = 0.0
        for (sample in audioData) {
            sum += sample * sample
        }
        return Math.sqrt(sum / audioData.size)
    }

    data class PitchResult(
        val frequency: Double,
        val noteName: String,
        val cents: Float,
        val probability: Float
    )

    private data class YinResult(
        val frequency: Double,
        val clarity: Double  // dPrime[tau] - lower is clearer
    )

    private val frequencyHistory = mutableListOf<Double>()
    private val clarityHistory = mutableListOf<Double>()
    private val historySize = 5

    private fun addToHistory(frequency: Double, clarity: Double) {
        frequencyHistory.add(frequency)
        clarityHistory.add(clarity)
        if (frequencyHistory.size > historySize) {
            frequencyHistory.removeAt(0)
            clarityHistory.removeAt(0)
        }
    }

    private fun medianFrequency(): Double {
        if (frequencyHistory.isEmpty()) return 0.0
        val sorted = frequencyHistory.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
    }

    private fun medianClarity(): Double {
        if (clarityHistory.isEmpty()) return 1.0
        val sorted = clarityHistory.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
    }

    // Note locking to prevent jumping to harmonics
    private var lockedNoteName: String? = null
    private var lockedFrequency: Double = 0.0
    private var lockConfidence: Int = 0
    private val LOCK_THRESHOLD = 3
    private val FREQUENCY_TOLERANCE_RATIO = 0.08 // ±8% frequency tolerance for lock

    fun detectPitch(audioData: FloatArray): PitchResult? = synchronized(this) {
        if (audioData.isEmpty()) {
            Log.d(TAG, "Empty audio data")
            return null
        }
        
        val rms = computeRMS(audioData)
        if (DEBUG) Log.d(TAG, "RMS: $rms, threshold: $minRmsThreshold")
        if (rms < minRmsThreshold) {
            Log.d(TAG, "Signal too weak (RMS: $rms < $minRmsThreshold)")
            return null
        }

        // YIN pitch detection with clarity feedback
        val yinResult = estimateFrequencyYIN(audioData)
        val rawFrequency = yinResult.frequency
        val clarity = yinResult.clarity
        
        if (DEBUG) Log.d(TAG, "Raw estimated frequency: $rawFrequency Hz, clarity: $clarity")
        
        if (rawFrequency <= AlgorithmConstants.INVALID_FREQUENCY) {
            Log.d(TAG, "Invalid frequency: $rawFrequency")
            return null
        }
        
        // Validate frequency range for guitar (80-1350 Hz)
        if (rawFrequency < MusicalConstants.MIN_GUITAR_FREQUENCY || rawFrequency > MusicalConstants.MAX_GUITAR_FREQUENCY) {
            Log.d(TAG, "Frequency out of guitar range (${MusicalConstants.MIN_GUITAR_FREQUENCY}-${MusicalConstants.MAX_GUITAR_FREQUENCY} Hz): $rawFrequency Hz")
            return null
        }
        
        // Add valid detection to history for median filtering
        addToHistory(rawFrequency, clarity)
        
        // Compute median frequency and clarity from history
        val medianFreq = medianFrequency()
        val medianClarity = medianClarity()
        
        if (medianFreq <= AlgorithmConstants.INVALID_FREQUENCY) {
            Log.d(TAG, "Median frequency invalid")
            return null
        }
        
        // Optional: reject if median clarity is too poor (higher than threshold)
        // clarityThreshold is already used in YIN, but we can be stricter
        if (medianClarity > clarityThreshold * 0.8) {
            if (DEBUG) Log.d(TAG, "Median clarity too poor: $medianClarity > ${clarityThreshold * 0.8}")
            // Continue anyway, but log
        }
        
        // Find note based on median frequency
        val noteInfo = noteFinder.findNote(medianFreq, referenceFrequency)
        
        // Validate probability (confidence)
        if (noteInfo.probability < probabilityThreshold) {
            Log.d(TAG, "Low confidence probability: ${noteInfo.probability} < $probabilityThreshold")
            return null
        }
        
        Log.d(TAG, "Detected: ${noteInfo.noteName} at $medianFreq Hz (raw $rawFrequency), cents: ${noteInfo.cents}, prob: ${noteInfo.probability}, clarity: $medianClarity")
        
        // Note locking logic to prevent jumping to harmonics
        val noteName = noteInfo.noteName
        
        if (lockedNoteName == null) {
            // First detection, lock to this note
            lockedNoteName = noteName
            lockedFrequency = medianFreq
            lockConfidence = LOCK_THRESHOLD
            if (DEBUG) Log.d(TAG, "Note lock initialized: $noteName at $medianFreq Hz")
        } else {
            val frequencyDiff = Math.abs(medianFreq - lockedFrequency) / lockedFrequency
            val sameNote = noteName == lockedNoteName || frequencyDiff < FREQUENCY_TOLERANCE_RATIO
            
            if (sameNote) {
                // Same note, reinforce lock
                lockConfidence = Math.min(LOCK_THRESHOLD, lockConfidence + 1)
                lockedFrequency = medianFreq // update locked frequency
                if (DEBUG) Log.d(TAG, "Note lock reinforced: confidence=$lockConfidence")
            } else {
                // Different note, weaken lock
                lockConfidence--
                if (DEBUG) Log.d(TAG, "Note lock weakened: confidence=$lockConfidence")
                if (lockConfidence <= 0) {
                    // Switch lock to new note
                    lockedNoteName = noteName
                    lockedFrequency = medianFreq
                    lockConfidence = LOCK_THRESHOLD
                    if (DEBUG) Log.d(TAG, "Note lock switched to: $noteName at $medianFreq Hz")
                } else {
                    // Still locked to previous note, ignore this detection
                    Log.d(TAG, "Ignoring detection due to note lock (locked: $lockedNoteName, detected: $noteName)")
                    return null
                }
            }
        }
        
        // Use locked note for display
        val displayFrequency = lockedFrequency
        
        // Recompute note info for locked frequency (to get accurate cents)
        val displayNoteInfo = noteFinder.findNote(displayFrequency, referenceFrequency)
        
        currentPitchResult = PitchResult(
            frequency = displayFrequency,
            noteName = displayNoteInfo.noteName,
            cents = displayNoteInfo.cents,
            probability = displayNoteInfo.probability
        )
        
        return currentPitchResult
    }

    private fun estimateFrequencyYIN(audioData: FloatArray): YinResult {
        // Improved YIN pitch detection algorithm with clarity feedback
        if (audioData.size < AlgorithmConstants.MIN_YIN_BUFFER_SIZE) return YinResult(AlgorithmConstants.INVALID_FREQUENCY, 1.0)
        
        val sampleRate = AudioConfig.SAMPLE_RATE
        val buffer = audioData
        
        // Frequency range for guitar: ${MusicalConstants.MIN_GUITAR_FREQUENCY}Hz to ${MusicalConstants.MAX_GUITAR_FREQUENCY}Hz
        val minFreq = MusicalConstants.MIN_GUITAR_FREQUENCY
        val maxFreq = MusicalConstants.MAX_GUITAR_FREQUENCY
        val tauMin = (sampleRate / maxFreq).toInt()  // ~33 samples for 1350Hz
        val tauMax = Math.min(buffer.size / AlgorithmConstants.DIVISOR_FOR_HALF_BUFFER, (sampleRate / minFreq).toInt())  // ~551 samples for 80Hz
        
        if (tauMax <= tauMin) return YinResult(AlgorithmConstants.INVALID_FREQUENCY, 1.0)
        
        if (DEBUG) Log.d(TAG, "YIN: buffer size=${buffer.size}, tauMin=$tauMin, tauMax=$tauMax, expected tau for 440Hz=${sampleRate/AudioConfig.DEFAULT_REFERENCE_FREQUENCY}")
        
        // 1. Compute difference function d(t) for all t from 0 to tauMax-1
        val d = DoubleArray(tauMax)
        for (t in 0 until tauMax) {
        var sum = AlgorithmConstants.INITIAL_SUM
            for (j in 0 until buffer.size - t) {
                val diff = buffer[j] - buffer[j + t]
                sum += diff * diff
            }
            d[t] = sum
        }
        
        // 2. Compute cumulative mean normalized difference d'(t)
        val dPrime = DoubleArray(tauMax)
        dPrime[0] = 1.0
        var runningSum = AlgorithmConstants.INITIAL_SUM
        
        for (t in 1 until tauMax) {
            runningSum += d[t]
            // Avoid division by zero
            if (runningSum == AlgorithmConstants.INITIAL_SUM) {
                dPrime[t] = 1.0
            } else {
                dPrime[t] = d[t] * t / runningSum
            }
        }
        
        // 3. Find first trough below threshold (scaled with clarity threshold for sensitivity)
        // Typical YIN threshold is ${AlgorithmConstants.TYPICAL_MIN_YIN_THRESHOLD}-${AlgorithmConstants.TYPICAL_MAX_YIN_THRESHOLD}. We use ${AlgorithmConstants.YIN_THRESHOLD_OFFSET}-${AlgorithmConstants.YIN_THRESHOLD_OFFSET + AlgorithmConstants.DEFAULT_CLARITY_THRESHOLD * AlgorithmConstants.YIN_THRESHOLD_MULTIPLIER} range based on sensitivity.
        val threshold = AlgorithmConstants.YIN_THRESHOLD_OFFSET + clarityThreshold * AlgorithmConstants.YIN_THRESHOLD_MULTIPLIER
        var tau = AlgorithmConstants.INITIAL_TAU
        for (t in tauMin until tauMax) {
            if (dPrime[t] < threshold) {
                tau = t
                break
            }
        }
        
        // 4. If no trough below threshold, find global minimum
        if (tau == AlgorithmConstants.INITIAL_TAU) {
            var minVal = Double.MAX_VALUE
            for (t in tauMin until tauMax) {
                if (dPrime[t] < minVal) {
                    minVal = dPrime[t]
                    tau = t
                }
            }
        }
        
        if (DEBUG) Log.d(TAG, "YIN: tau=$tau, dPrime[tau]=${if (tau > 0) dPrime[tau] else "N/A"}, threshold=$threshold")
        
        // Check clarity of pitch detection
        if (tau > AlgorithmConstants.INVALID_TAU && dPrime[tau] > clarityThreshold) {
            if (DEBUG) Log.d(TAG, "YIN: pitch unclear (dPrime[tau]=${dPrime[tau]} > $clarityThreshold)")
            return YinResult(AlgorithmConstants.INVALID_FREQUENCY, dPrime[tau])
        }
        
        // 5. Parabolic interpolation for better precision
        if (tau > tauMin && tau < tauMax - 1) {
            val bestTau = parabolicInterpolation(dPrime, tau).toDouble()
            val freq = sampleRate.toDouble() / bestTau
            if (DEBUG) Log.d(TAG, "YIN: parabolic interpolation, bestTau=$bestTau, freq=$freq")
            return YinResult(freq, dPrime[tau])
        }
        
        val freq = if (tau >= tauMin) sampleRate.toDouble() / tau else AlgorithmConstants.INVALID_FREQUENCY
        if (DEBUG) Log.d(TAG, "YIN: final tau=$tau, freq=$freq")
        return YinResult(freq, dPrime[tau])
    }
    
    private fun parabolicInterpolation(data: DoubleArray, tau: Int): Float {
        // Parabolic interpolation around minimum
        val s0 = data[tau - 1]
        val s1 = data[tau]
        val s2 = data[tau + 1]
        
        val denominator = 2.0 * (2.0 * s1 - s2 - s0)
        if (DEBUG) Log.d(TAG, "Parabolic: s0=$s0, s1=$s1, s2=$s2, denominator=$denominator")
        
        if (Math.abs(denominator) < AlgorithmConstants.MIN_DENOMINATOR) {
            if (DEBUG) Log.d(TAG, "Parabolic denominator too small, returning tau")
            return tau.toFloat()
        }
        
        val adjustment = (s2 - s0) / denominator
        if (DEBUG) Log.d(TAG, "Parabolic adjustment=$adjustment")
        return tau + adjustment.toFloat()
    }

    fun setReferenceFrequency(frequency: Double) {
        referenceFrequency = frequency
    }

    fun setSensitivity(sensitivity: Int) {
        // Clamp sensitivity to 0-100 range
        val clampedSensitivity = sensitivity.coerceIn(UiConstants.MIN_SENSITIVITY, UiConstants.MAX_SENSITIVITY)
        minRmsThreshold = rmsThresholdFromSensitivity(clampedSensitivity)
        clarityThreshold = clarityThresholdFromSensitivity(clampedSensitivity)
        probabilityThreshold = probabilityThresholdFromSensitivity(clampedSensitivity)
        Log.d(TAG, "Set sensitivity to $clampedSensitivity: RMS threshold=$minRmsThreshold, clarity threshold=$clarityThreshold, probability threshold=$probabilityThreshold")
    }

    fun setThresholds(rmsThreshold: Double, clarityThresh: Double, probThresh: Float) {
        minRmsThreshold = rmsThreshold
        clarityThreshold = clarityThresh
        probabilityThreshold = probThresh
        Log.d(TAG, "Set thresholds: RMS=$minRmsThreshold, clarity=$clarityThreshold, probability=$probabilityThreshold")
    }

    fun getCurrentPitchResult(): PitchResult? = currentPitchResult

    fun startDispatcher() {
        // Full TarsosDSP integration would go here
        // For now using simplified YIN above
    }

    fun stopDispatcher() {
        // No dispatcher in simplified implementation
    }
    
    fun testWithSyntheticFrequency(frequency: Double): PitchResult? {
        val sampleRate = AudioConfig.SAMPLE_RATE
        val duration = AlgorithmConstants.SYNTHETIC_DURATION_SECONDS // 100ms
        val samples = (sampleRate * duration).toInt()
        val audioData = FloatArray(samples)
        val angularFreq = 2.0 * Math.PI * frequency / sampleRate
        for (i in 0 until samples) {
            audioData[i] = Math.sin(angularFreq * i).toFloat()
        }
        Log.d(TAG, "Generated synthetic sine wave at $frequency Hz")
        return detectPitch(audioData)
    }
}