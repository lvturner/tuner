package com.rokid.tuner.pitch

import android.util.Log
import com.rokid.tuner.audio.AudioConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PitchDetector {

    companion object {
        private const val TAG = "PitchDetector"
        private const val DEFAULT_MIN_RMS_THRESHOLD = 0.0005 // 0.05% of max amplitude (sensitive to short peaks but rejects constant low noise)
        private const val DEFAULT_CLARITY_THRESHOLD = 0.8 // Very tolerant clarity threshold (lower dPrime = clearer pitch)
        private const val DEFAULT_PROBABILITY_THRESHOLD = 0.05f // Very tolerant probability threshold
        private const val DEBUG = true
        
        /**
         * Sensitivity mapping (0-100 scale to thresholds)
         * - Higher sensitivity number = more sensitive detection
         * - Sensitivity 0: least sensitive (strict thresholds)
         * - Sensitivity 100: most sensitive (lenient thresholds)
         * Default sensitivity is 100 (maximum) in MainActivity.
         */
        
        // Sensitivity mapping (0-100 scale to thresholds)
        fun rmsThresholdFromSensitivity(sensitivity: Int): Double {
            // Higher sensitivity number = more sensitive = lower threshold
            // sensitivity 0 -> highest threshold (0.01), sensitivity 100 -> lowest threshold (0.0005)
            return 0.01 - (sensitivity / 100.0) * 0.0095
        }
        
        fun clarityThresholdFromSensitivity(sensitivity: Int): Double {
            // Higher sensitivity = more tolerant = higher clarity threshold
            // sensitivity 0 -> strict (0.1), sensitivity 100 -> tolerant (0.8)
            return 0.1 + (sensitivity / 100.0) * 0.7
        }
        
        fun probabilityThresholdFromSensitivity(sensitivity: Int): Float {
            // Higher sensitivity = more tolerant = lower probability threshold
            // sensitivity 0 -> strict (0.5), sensitivity 100 -> tolerant (0.05)
            return 0.5f - (sensitivity / 100.0f) * 0.45f
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

        // Simple YIN implementation for now (would integrate TarsosDSP fully)
        val estimatedFrequency = estimateFrequencyYIN(audioData)
        
        Log.d(TAG, "Raw estimated frequency: $estimatedFrequency Hz")
        
        if (estimatedFrequency <= 0.0) {
            Log.d(TAG, "Invalid frequency: $estimatedFrequency")
            return null
        }
        
        // Validate frequency range for guitar (80-1350 Hz)
        if (estimatedFrequency < 80.0 || estimatedFrequency > 1350.0) {
            Log.d(TAG, "Frequency out of guitar range (80-1350 Hz): $estimatedFrequency Hz")
            return null
        }
        
        val noteInfo = noteFinder.findNote(estimatedFrequency, referenceFrequency)
        
        // Validate probability (confidence)
        if (noteInfo.probability < probabilityThreshold) {
            Log.d(TAG, "Low confidence probability: ${noteInfo.probability} < $probabilityThreshold")
            return null
        }
        
        Log.d(TAG, "Detected: ${noteInfo.noteName} at $estimatedFrequency Hz, cents: ${noteInfo.cents}, prob: ${noteInfo.probability}")
        
        currentPitchResult = PitchResult(
            frequency = estimatedFrequency,
            noteName = noteInfo.noteName,
            cents = noteInfo.cents,
            probability = noteInfo.probability
        )
        
        return currentPitchResult
    }

    private fun estimateFrequencyYIN(audioData: FloatArray): Double {
        // Improved YIN pitch detection algorithm
        if (audioData.size < 2) return 0.0
        
        val sampleRate = AudioConfig.SAMPLE_RATE
        val buffer = audioData
        
        // Frequency range for guitar: 80Hz to 1350Hz
        val minFreq = 80.0
        val maxFreq = 1350.0
        val tauMin = (sampleRate / maxFreq).toInt()  // ~33 samples for 1350Hz
        val tauMax = Math.min(buffer.size / 2, (sampleRate / minFreq).toInt())  // ~551 samples for 80Hz
        
        if (tauMax <= tauMin) return 0.0
        
        if (DEBUG) Log.d(TAG, "YIN: buffer size=${buffer.size}, tauMin=$tauMin, tauMax=$tauMax, expected tau for 440Hz=${sampleRate/440.0}")
        
        // 1. Compute difference function d(t) for all t from 0 to tauMax-1
        val d = DoubleArray(tauMax)
        for (t in 0 until tauMax) {
            var sum = 0.0
            for (j in 0 until buffer.size - t) {
                val diff = buffer[j] - buffer[j + t]
                sum += diff * diff
            }
            d[t] = sum
        }
        
        // 2. Compute cumulative mean normalized difference d'(t)
        val dPrime = DoubleArray(tauMax)
        dPrime[0] = 1.0
        var runningSum = 0.0
        
        for (t in 1 until tauMax) {
            runningSum += d[t]
            // Avoid division by zero
            if (runningSum == 0.0) {
                dPrime[t] = 1.0
            } else {
                dPrime[t] = d[t] * t / runningSum
            }
        }
        
        // 3. Find first trough below threshold (scaled with clarity threshold for sensitivity)
        // Typical YIN threshold is 0.1-0.2. We use 0.08-0.25 range based on sensitivity.
        val threshold = 0.08 + clarityThreshold * 0.2125
        var tau = 0
        for (t in tauMin until tauMax) {
            if (dPrime[t] < threshold) {
                tau = t
                break
            }
        }
        
        // 4. If no trough below threshold, find global minimum
        if (tau == 0) {
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
        if (tau > 0 && dPrime[tau] > clarityThreshold) {
            if (DEBUG) Log.d(TAG, "YIN: pitch unclear (dPrime[tau]=${dPrime[tau]} > $clarityThreshold)")
            return 0.0
        }
        
        // 5. Parabolic interpolation for better precision
        if (tau > tauMin && tau < tauMax - 1) {
            val bestTau = parabolicInterpolation(dPrime, tau).toDouble()
            val freq = sampleRate.toDouble() / bestTau
            if (DEBUG) Log.d(TAG, "YIN: parabolic interpolation, bestTau=$bestTau, freq=$freq")
            return freq
        }
        
        val freq = if (tau >= tauMin) sampleRate.toDouble() / tau else 0.0
        if (DEBUG) Log.d(TAG, "YIN: final tau=$tau, freq=$freq")
        return freq
    }
    
    private fun parabolicInterpolation(data: DoubleArray, tau: Int): Float {
        // Parabolic interpolation around minimum
        val s0 = data[tau - 1]
        val s1 = data[tau]
        val s2 = data[tau + 1]
        
        val denominator = 2.0 * (2.0 * s1 - s2 - s0)
        if (DEBUG) Log.d(TAG, "Parabolic: s0=$s0, s1=$s1, s2=$s2, denominator=$denominator")
        
        if (Math.abs(denominator) < 1e-10) {
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
        val clampedSensitivity = sensitivity.coerceIn(0, 100)
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
        val duration = 0.1 // 100ms
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