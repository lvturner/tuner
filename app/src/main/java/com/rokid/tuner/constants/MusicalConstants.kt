package com.rokid.tuner.constants

/**
 * Musical theory constants used throughout the tuner application.
 */
object MusicalConstants {
    // Octave and note constants
    const val NOTES_PER_OCTAVE = 12
    const val SEMITONES_PER_OCTAVE = NOTES_PER_OCTAVE
    const val CENTS_PER_OCTAVE = 1200
    const val CENTS_PER_SEMITONE = CENTS_PER_OCTAVE / NOTES_PER_OCTAVE
    
    // A4 reference note constants
    const val A_NOTE_INDEX = 9  // A is the 9th note in C-based arrays (0=C, 1=C#, ..., 9=A)
    const val A4_OCTAVE = 4
    
    // Frequency ratio base for semitone calculations (2^(n/12))
    const val FREQUENCY_RATIO_BASE = 2.0
    
    // Guitar frequency range
    const val MIN_GUITAR_FREQUENCY = 80.0
    const val MAX_GUITAR_FREQUENCY = 1350.0
    
    // Tuning tolerance (default 10 cents, configurable via settings)
    const val IN_TUNE_THRESHOLD_CENTS = 10.0
    
    // Frequency generation constants
    const val OCTAVES_TO_GENERATE = 6
    const val MIN_GENERATED_OCTAVE = 2
    const val MAX_GENERATED_OCTAVE = 7
    
    // Rounding constant
    const val ROUNDING_OFFSET = 0.5
    
    // Standard note names (C-based)
    val NOTE_NAMES = arrayOf(
        "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
    )
}