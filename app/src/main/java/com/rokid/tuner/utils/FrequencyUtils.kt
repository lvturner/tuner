package com.rokid.tuner.utils

import com.rokid.tuner.audio.AudioConfig
import com.rokid.tuner.constants.AlgorithmConstants
import com.rokid.tuner.constants.MusicalConstants
import kotlin.math.log2
import kotlin.math.pow

object FrequencyUtils {

    private const val A4_FREQUENCY = 440.0

    
    private val noteNames = MusicalConstants.NOTE_NAMES.toList()

    fun frequencyToNote(frequency: Double, referenceFrequency: Double = A4_FREQUENCY): Pair<String, Int> {
        if (frequency <= AlgorithmConstants.INVALID_FREQUENCY) return Pair("--", 0)
        
        val semitonesFromA4 = MusicalConstants.SEMITONES_PER_OCTAVE.toDouble() * log2(frequency / referenceFrequency)
        val roundedSemitones = semitonesFromA4.roundToNearestInteger()
        
        val noteIndex = ((roundedSemitones % MusicalConstants.NOTES_PER_OCTAVE + MusicalConstants.NOTES_PER_OCTAVE) % MusicalConstants.NOTES_PER_OCTAVE)
        val octave = MusicalConstants.A4_OCTAVE + (roundedSemitones + MusicalConstants.A_NOTE_INDEX) / MusicalConstants.NOTES_PER_OCTAVE
        
        val noteName = "${noteNames[noteIndex]}${octave}"
        return Pair(noteName, roundedSemitones)
    }

    fun calculateCents(frequency: Double, targetFrequency: Double): Float {
        if (frequency <= AlgorithmConstants.INVALID_FREQUENCY || targetFrequency <= AlgorithmConstants.INVALID_FREQUENCY) return 0f
        return (MusicalConstants.CENTS_PER_OCTAVE.toDouble() * log2(frequency / targetFrequency)).toFloat()
    }

    fun getTargetFrequency(semitonesFromA4: Int, referenceFrequency: Double = A4_FREQUENCY): Double {
        return referenceFrequency * MusicalConstants.FREQUENCY_RATIO_BASE.pow(semitonesFromA4 / MusicalConstants.SEMITONES_PER_OCTAVE.toDouble())
    }

    fun getNoteName(index: Int): String {
        return noteNames[(index % MusicalConstants.NOTES_PER_OCTAVE + MusicalConstants.NOTES_PER_OCTAVE) % MusicalConstants.NOTES_PER_OCTAVE]
    }

    fun getAllNoteFrequencies(referenceFrequency: Double = A4_FREQUENCY): Map<String, Double> {
        val frequencies = mutableMapOf<String, Double>()
        
        // Generate frequencies for 6 octaves (C2 to B7)
        for (octave in MusicalConstants.MIN_GENERATED_OCTAVE..MusicalConstants.MAX_GENERATED_OCTAVE) {
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