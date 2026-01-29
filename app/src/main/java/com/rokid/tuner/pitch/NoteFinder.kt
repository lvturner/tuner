package com.rokid.tuner.pitch

import com.rokid.tuner.audio.AudioConfig
import kotlin.math.log2
import kotlin.math.round

class NoteFinder {

    data class NoteInfo(
        val noteName: String,
        val cents: Float,
        val probability: Float,
        val targetFrequency: Double
    )

    private val noteNames = arrayOf(
        "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
    )

    fun findNote(frequency: Double, referenceFrequency: Double = AudioConfig.DEFAULT_REFERENCE_FREQUENCY): NoteInfo {
        if (frequency <= 0.0) {
            return NoteInfo("--", 0f, 0f, 0.0)
        }

        // Calculate semitones from A4 (440Hz)
        val semitonesFromA4 = 12.0 * log2(frequency / referenceFrequency)
        
        // Round to nearest semitone
        val roundedSemitones = round(semitonesFromA4).toInt()
        
        // Calculate cents deviation from nearest semitone
        val cents = ((semitonesFromA4 - roundedSemitones) * 100).toFloat()
        
        // Get note index (A4 = index 9, since A is 9th note in our array starting from C)
        // Add 9 because A is at index 9 in our array
        val noteIndex = ((9 + roundedSemitones) % 12 + 12) % 12
        
        // Get octave (A4 is octave 4)
        val octave = 4 + (roundedSemitones + 9) / 12
        
        val noteName = "${noteNames[noteIndex]}${octave}"
        
        // Calculate target frequency for this note
        val targetFrequency = referenceFrequency * Math.pow(2.0, roundedSemitones / 12.0)
        
        // Simple probability based on cents (closer to 0 cents = higher probability)
        val probability = 1.0f - (Math.abs(cents) / 50.0f).coerceIn(0f, 1f)
        
        return NoteInfo(noteName, cents, probability, targetFrequency)
    }

    fun getNoteNames(): List<String> = noteNames.toList()

    companion object {
        private const val NOTES_PER_OCTAVE = 12
        
        fun frequencyToCents(frequency: Double, targetFrequency: Double): Float {
            if (frequency <= 0.0 || targetFrequency <= 0.0) return 0f
            return (1200.0 * log2(frequency / targetFrequency)).toFloat()
        }
        
        fun centsToFrequencyRatio(cents: Float): Double {
            return Math.pow(2.0, cents / 1200.0)
        }
    }
}