package com.rokid.tuner.utils

import com.rokid.tuner.audio.AudioConfig
import kotlin.math.sin
import kotlin.math.PI

object AudioTestUtils {

    fun generateSineWave(frequency: Double, durationMs: Int = 1000): FloatArray {
        val sampleRate = AudioConfig.SAMPLE_RATE
        val durationSamples = (durationMs * sampleRate) / 1000
        val samples = FloatArray(durationSamples)
        
        val angularFrequency = 2.0 * PI * frequency / sampleRate
        
        for (i in samples.indices) {
            samples[i] = sin(angularFrequency * i).toFloat()
        }
        
        return samples
    }

    fun generateGuitarStringWave(fundamental: Double, harmonics: List<Double> = emptyList()): FloatArray {
        val sampleRate = AudioConfig.SAMPLE_RATE
        val durationSamples = sampleRate // 1 second
        val samples = FloatArray(durationSamples)
        
        // Fundamental frequency
        val angularFreq = 2.0 * PI * fundamental / sampleRate
        
        // Add harmonics with decreasing amplitude
        val harmonicAmplitudes = listOf(1.0, 0.5, 0.3, 0.2, 0.1)
        
        for (i in samples.indices) {
            var sample = 0.0
            
            // Fundamental
            sample += sin(angularFreq * i)
            
            // Harmonics
            for ((harmonicIndex, amplitude) in harmonicAmplitudes.withIndex()) {
                val harmonicFreq = fundamental * (harmonicIndex + 2)
                val harmonicAngularFreq = 2.0 * PI * harmonicFreq / sampleRate
                sample += amplitude * sin(harmonicAngularFreq * i)
            }
            
            // Normalize
            samples[i] = (sample / 2.0).toFloat()
        }
        
        return samples
    }

    fun generateGuitarNotes(): Map<String, FloatArray> {
        val notes = mapOf(
            "E2" to 82.41,
            "A2" to 110.0,
            "D3" to 146.83,
            "G3" to 196.0,
            "B3" to 246.94,
            "E4" to 329.63
        )
        
        return notes.mapValues { generateGuitarStringWave(it.value) }
    }

    fun addNoise(samples: FloatArray, noiseLevel: Float = 0.1f): FloatArray {
        return samples.map { sample ->
            val noise = (Math.random() * 2 - 1) * noiseLevel
            (sample + noise.toFloat()).coerceIn(-1f, 1f)
        }.toFloatArray()
    }

    fun testPitchDetection(pitchDetector: Any, frequency: Double): Boolean {
        // This would test the pitch detector with synthetic audio
        // For now return true as placeholder
        return true
    }

    val GUITAR_STANDARD_TUNING = mapOf(
        "E2" to 82.41,
        "A2" to 110.0,
        "D3" to 146.83,
        "G3" to 196.0,
        "B3" to 246.94,
        "E4" to 329.63
    )
    
    val NOTE_FREQUENCIES = mapOf(
        "C" to 16.35, "C♯" to 17.32, "D" to 18.35, "D♯" to 19.45,
        "E" to 20.60, "F" to 21.83, "F♯" to 23.12, "G" to 24.50,
        "G♯" to 25.96, "A" to 27.50, "A♯" to 29.14, "B" to 30.87
    )
}