package com.rokid.tuner.constants

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MusicalConstants object.
 * Verifies musical theory constants and note definitions.
 */
class MusicalConstantsTest {

    // ========== Octave and note constants tests ==========

    @Test
    fun `NOTES_PER_OCTAVE equals 12`() {
        assertEquals(12, MusicalConstants.NOTES_PER_OCTAVE)
    }

    @Test
    fun `SEMITONES_PER_OCTAVE equals NOTES_PER_OCTAVE`() {
        assertEquals(MusicalConstants.NOTES_PER_OCTAVE, MusicalConstants.SEMITONES_PER_OCTAVE)
    }

    @Test
    fun `CENTS_PER_OCTAVE equals 1200`() {
        assertEquals(1200, MusicalConstants.CENTS_PER_OCTAVE)
    }

    @Test
    fun `CENTS_PER_SEMITONE equals 100`() {
        assertEquals(100, MusicalConstants.CENTS_PER_SEMITONE)
    }

    @Test
    fun `CENTS_PER_OCTAVE equals CENTS_PER_SEMITONE times NOTES_PER_OCTAVE`() {
        assertEquals(
            MusicalConstants.CENTS_PER_OCTAVE,
            MusicalConstants.CENTS_PER_SEMITONE * MusicalConstants.NOTES_PER_OCTAVE
        )
    }

    // ========== A4 reference note constants tests ==========

    @Test
    fun `A_NOTE_INDEX equals 9`() {
        // A is the 10th note in C-based array (0-indexed = 9)
        assertEquals(9, MusicalConstants.A_NOTE_INDEX)
    }

    @Test
    fun `A_NOTE_INDEX corresponds to A in NOTE_NAMES array`() {
        assertEquals("A", MusicalConstants.NOTE_NAMES[MusicalConstants.A_NOTE_INDEX])
    }

    @Test
    fun `A4_OCTAVE equals 4`() {
        assertEquals(4, MusicalConstants.A4_OCTAVE)
    }

    // ========== Frequency ratio tests ==========

    @Test
    fun `FREQUENCY_RATIO_BASE equals 2`() {
        assertEquals(2.0, MusicalConstants.FREQUENCY_RATIO_BASE, 0.001)
    }

    @Test
    fun `frequency ratio base produces correct octave relationship`() {
        // One octave up should double the frequency
        val ratio = Math.pow(MusicalConstants.FREQUENCY_RATIO_BASE, 1.0)
        assertEquals(2.0, ratio, 0.001)
    }

    @Test
    fun `frequency ratio base produces correct semitone relationship`() {
        // One semitone = 2^(1/12)
        val semitonRatio = Math.pow(
            MusicalConstants.FREQUENCY_RATIO_BASE, 
            1.0 / MusicalConstants.SEMITONES_PER_OCTAVE
        )
        assertEquals(1.05946, semitonRatio, 0.0001) // ≈ 1.05946
    }

    // ========== Guitar frequency range tests ==========

    @Test
    fun `MIN_GUITAR_FREQUENCY is reasonable for low E string`() {
        // Low E on guitar is about 82.41 Hz
        assertTrue(MusicalConstants.MIN_GUITAR_FREQUENCY <= 82.41)
        assertTrue(MusicalConstants.MIN_GUITAR_FREQUENCY > 50) // Should not be too low
    }

    @Test
    fun `MAX_GUITAR_FREQUENCY covers high fret positions`() {
        // High E string 24th fret is about 1318.5 Hz
        assertTrue(MusicalConstants.MAX_GUITAR_FREQUENCY >= 1318.5)
    }

    @Test
    fun `guitar frequency range is valid`() {
        assertTrue(MusicalConstants.MAX_GUITAR_FREQUENCY > MusicalConstants.MIN_GUITAR_FREQUENCY)
    }

    // ========== Tuning tolerance tests ==========

    @Test
    fun `IN_TUNE_THRESHOLD_CENTS is positive`() {
        assertTrue(MusicalConstants.IN_TUNE_THRESHOLD_CENTS > 0)
    }

    @Test
    fun `IN_TUNE_THRESHOLD_CENTS is less than half semitone`() {
        // Should be less than 50 cents (half a semitone)
        assertTrue(MusicalConstants.IN_TUNE_THRESHOLD_CENTS < 50)
    }

    @Test
    fun `IN_TUNE_THRESHOLD_CENTS is reasonable for guitar tuning`() {
        // Typically 5-15 cents is acceptable
        assertTrue(MusicalConstants.IN_TUNE_THRESHOLD_CENTS >= 5)
        assertTrue(MusicalConstants.IN_TUNE_THRESHOLD_CENTS <= 20)
    }

    // ========== Frequency generation constants tests ==========

    @Test
    fun `OCTAVES_TO_GENERATE is positive`() {
        assertTrue(MusicalConstants.OCTAVES_TO_GENERATE > 0)
    }

    @Test
    fun `octave range is valid`() {
        assertTrue(MusicalConstants.MAX_GENERATED_OCTAVE > MusicalConstants.MIN_GENERATED_OCTAVE)
        assertEquals(
            MusicalConstants.OCTAVES_TO_GENERATE,
            MusicalConstants.MAX_GENERATED_OCTAVE - MusicalConstants.MIN_GENERATED_OCTAVE + 1
        )
    }

    @Test
    fun `MIN_GENERATED_OCTAVE covers bass range`() {
        assertTrue(MusicalConstants.MIN_GENERATED_OCTAVE <= 2)
    }

    @Test
    fun `MAX_GENERATED_OCTAVE covers treble range`() {
        assertTrue(MusicalConstants.MAX_GENERATED_OCTAVE >= 6)
    }

    // ========== Rounding constant tests ==========

    @Test
    fun `ROUNDING_OFFSET equals 0_5`() {
        assertEquals(0.5, MusicalConstants.ROUNDING_OFFSET, 0.001)
    }

    // ========== NOTE_NAMES array tests ==========

    @Test
    fun `NOTE_NAMES contains 12 notes`() {
        assertEquals(12, MusicalConstants.NOTE_NAMES.size)
    }

    @Test
    fun `NOTE_NAMES starts with C`() {
        assertEquals("C", MusicalConstants.NOTE_NAMES[0])
    }

    @Test
    fun `NOTE_NAMES ends with B`() {
        assertEquals("B", MusicalConstants.NOTE_NAMES[11])
    }

    @Test
    fun `NOTE_NAMES contains all natural notes`() {
        val naturalNotes = listOf("C", "D", "E", "F", "G", "A", "B")
        naturalNotes.forEach { note ->
            assertTrue("Should contain $note", MusicalConstants.NOTE_NAMES.contains(note))
        }
    }

    @Test
    fun `NOTE_NAMES contains all sharp notes`() {
        val sharpNotes = listOf("C♯", "D♯", "F♯", "G♯", "A♯")
        sharpNotes.forEach { note ->
            assertTrue("Should contain $note", MusicalConstants.NOTE_NAMES.contains(note))
        }
    }

    @Test
    fun `NOTE_NAMES are in correct chromatic order`() {
        val expectedOrder = arrayOf(
            "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
        )
        assertArrayEquals(expectedOrder, MusicalConstants.NOTE_NAMES)
    }

    @Test
    fun `NOTE_NAMES uses sharp symbol not hash`() {
        // Should use proper musical sharp symbol ♯ (U+266F)
        assertTrue(MusicalConstants.NOTE_NAMES[1].contains("♯"))
        assertFalse(MusicalConstants.NOTE_NAMES[1].contains("#"))
    }

    @Test
    fun `NOTE_NAMES does not contain E sharp or B sharp`() {
        // E# = F and B# = C in equal temperament
        assertFalse(MusicalConstants.NOTE_NAMES.contains("E♯"))
        assertFalse(MusicalConstants.NOTE_NAMES.contains("B♯"))
    }

    // ========== Musical math verification tests ==========

    @Test
    fun `A4 can be calculated from C4`() {
        // A4 is 9 semitones above C4
        // A4 = C4 * 2^(9/12)
        val c4 = 261.63
        val expectedA4 = c4 * Math.pow(
            MusicalConstants.FREQUENCY_RATIO_BASE, 
            MusicalConstants.A_NOTE_INDEX.toDouble() / MusicalConstants.SEMITONES_PER_OCTAVE
        )
        assertEquals(440.0, expectedA4, 1.0)
    }

    @Test
    fun `octave doubles frequency`() {
        val baseFrequency = 440.0
        val octaveUp = baseFrequency * Math.pow(MusicalConstants.FREQUENCY_RATIO_BASE, 1.0)
        assertEquals(880.0, octaveUp, 0.01)
    }
}
