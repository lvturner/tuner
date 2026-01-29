package com.rokid.tuner.utils

import kotlin.math.log2
import kotlin.math.pow

object FrequencyUtils {

    private const val A4_FREQUENCY = 440.0
    private const val NOTES_PER_OCTAVE = 12
    
    private val noteNames = listOf(
        "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
    )

    fun frequencyToNote(frequency: Double, referenceFrequency: Double = A4_FREQUENCY): Pair<String, Int> {
        if (frequency <= 0.0) return Pair("--", 0)
        
        val semitonesFromA4 = 12.0 * log2(frequency / referenceFrequency)
        val roundedSemitones = semitonesFromA4.roundToNearestInteger()
        
        val noteIndex = ((roundedSemitones % 12 + 12) % 12)
        val octave = 4 + (roundedSemitones + 9) / 12
        
        val noteName = "${noteNames[noteIndex]}${octave}"
        return Pair(noteName, roundedSemitones)
    }

    fun calculateCents(frequency: Double, targetFrequency: Double): Float {
        if (frequency <= 0.0 || targetFrequency <= 0.0) return 0f
        return (1200.0 * log2(frequency / targetFrequency)).toFloat()
    }

    fun getTargetFrequency(semitonesFromA4: Int, referenceFrequency: Double = A4_FREQUENCY): Double {
        return referenceFrequency * 2.0.pow(semitonesFromA4 / 12.0)
    }

    fun getNoteName(index: Int): String {
        return noteNames[(index % 12 + 12) % 12]
    }

    fun getAllNoteFrequencies(referenceFrequency: Double = A4_FREQUENCY): Map<String, Double> {
        val frequencies = mutableMapOf<String, Double>()
        
        // Generate frequencies for 6 octaves (C2 to B7)
        for (octave in 2..7) {
            for ((noteIndex, noteName) in noteNames.withIndex()) {
                val semitonesFromA4 = noteIndex - 9 + (octave - 4) * 12
                val frequency = getTargetFrequency(semitonesFromA4, referenceFrequency)
                val fullNoteName = "$noteName$octave"
                frequencies[fullNoteName] = frequency
            }
        }
        
        return frequencies
    }

    fun isInTune(cents: Float, tolerance: Float = 5f): Boolean {
        return Math.abs(cents) <= tolerance
    }

    fun getTuningStatus(cents: Float): String {
        return when {
            cents > 5f -> "Sharp"
            cents < -5f -> "Flat"
            else -> "In Tune"
        }
    }

    private fun Double.roundToNearestInteger(): Int {
        return if (this >= 0) (this + 0.5).toInt() else (this - 0.5).toInt()
    }
}