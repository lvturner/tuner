package com.rokid.tuner.pitch

import com.rokid.tuner.audio.AudioConfig
import com.rokid.tuner.constants.AlgorithmConstants
import com.rokid.tuner.constants.MusicalConstants
import kotlin.math.log2
import kotlin.math.round

class NoteFinder {

    data class NoteInfo(
        val noteName: String,
        val cents: Float,
        val probability: Float,
        val targetFrequency: Double
    )

    private val noteNames = MusicalConstants.NOTE_NAMES

    fun findNote(frequency: Double, referenceFrequency: Double = AudioConfig.DEFAULT_REFERENCE_FREQUENCY): NoteInfo {
        if (frequency <= AlgorithmConstants.INVALID_FREQUENCY) {
            return NoteInfo("--", 0f, 0f, AlgorithmConstants.INVALID_FREQUENCY)
        }

        // Calculate semitones from A4 (440Hz)
        val semitonesFromA4 = MusicalConstants.SEMITONES_PER_OCTAVE.toDouble() * log2(frequency / referenceFrequency)
        
        // Round to nearest semitone
        val roundedSemitones = round(semitonesFromA4).toInt()
        
        // Calculate cents deviation from nearest semitone
        val cents = ((semitonesFromA4 - roundedSemitones) * MusicalConstants.CENTS_PER_SEMITONE).toFloat()
        
        // Get note index (A4 = index 9, since A is 9th note in our array starting from C)
        // Add 9 because A is at index 9 in our array
        val noteIndex = ((MusicalConstants.A_NOTE_INDEX + roundedSemitones) % MusicalConstants.NOTES_PER_OCTAVE + MusicalConstants.NOTES_PER_OCTAVE) % MusicalConstants.NOTES_PER_OCTAVE
        
        // Get octave (A4 is octave 4)
        val octave = MusicalConstants.A4_OCTAVE + (roundedSemitones + MusicalConstants.A_NOTE_INDEX) / MusicalConstants.NOTES_PER_OCTAVE
        
        val noteName = "${noteNames[noteIndex]}${octave}"
        
        // Calculate target frequency for this note
        val targetFrequency = referenceFrequency * Math.pow(MusicalConstants.FREQUENCY_RATIO_BASE, roundedSemitones / MusicalConstants.SEMITONES_PER_OCTAVE.toDouble())
        
        // Simple probability based on cents (closer to 0 cents = higher probability)
        val probability = 1.0f - (Math.abs(cents) / AudioConfig.MAX_CENTS_DEVIATION.toFloat()).coerceIn(0f, 1f)
        
        return NoteInfo(noteName, cents, probability, targetFrequency)
    }

    fun getNoteNames(): List<String> = noteNames.toList()

    companion object {

        
        fun frequencyToCents(frequency: Double, targetFrequency: Double): Float {
            if (frequency <= 0.0 || targetFrequency <= 0.0) return 0f
            return (MusicalConstants.CENTS_PER_OCTAVE.toDouble() * log2(frequency / targetFrequency)).toFloat()
        }
        
        fun centsToFrequencyRatio(cents: Float): Double {
            return Math.pow(MusicalConstants.FREQUENCY_RATIO_BASE, cents / MusicalConstants.CENTS_PER_OCTAVE.toDouble())
        }
    }
}