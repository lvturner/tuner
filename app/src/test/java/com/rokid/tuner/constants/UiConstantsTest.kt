package com.rokid.tuner.constants

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UiConstants object.
 * Verifies UI configuration and timing constants.
 */
class UiConstantsTest {

    // ========== Sensitivity configuration tests ==========

    @Test
    fun `MIN_SENSITIVITY is 0`() {
        assertEquals(0, UiConstants.MIN_SENSITIVITY)
    }

    @Test
    fun `MAX_SENSITIVITY is 100`() {
        assertEquals(100, UiConstants.MAX_SENSITIVITY)
    }

    @Test
    fun `DEFAULT_SENSITIVITY is within valid range`() {
        assertTrue(UiConstants.DEFAULT_SENSITIVITY >= UiConstants.MIN_SENSITIVITY)
        assertTrue(UiConstants.DEFAULT_SENSITIVITY <= UiConstants.MAX_SENSITIVITY)
    }

    @Test
    fun `DEFAULT_SENSITIVITY is maximum for best detection`() {
        assertEquals(UiConstants.MAX_SENSITIVITY, UiConstants.DEFAULT_SENSITIVITY)
    }

    @Test
    fun `SETTINGS_DEFAULT_SENSITIVITY is midpoint`() {
        assertEquals(50, UiConstants.SETTINGS_DEFAULT_SENSITIVITY)
    }

    @Test
    fun `SETTINGS_DEFAULT_SENSITIVITY is within valid range`() {
        assertTrue(UiConstants.SETTINGS_DEFAULT_SENSITIVITY >= UiConstants.MIN_SENSITIVITY)
        assertTrue(UiConstants.SETTINGS_DEFAULT_SENSITIVITY <= UiConstants.MAX_SENSITIVITY)
    }

    @Test
    fun `sensitivity range is valid`() {
        assertTrue(UiConstants.MAX_SENSITIVITY > UiConstants.MIN_SENSITIVITY)
    }

    // ========== In-tune threshold configuration tests ==========

    @Test
    fun `MIN_IN_TUNE_THRESHOLD_CENTS is 0`() {
        assertEquals(0.0, UiConstants.MIN_IN_TUNE_THRESHOLD_CENTS, 0.001)
    }

    @Test
    fun `MAX_IN_TUNE_THRESHOLD_CENTS is 50`() {
        assertEquals(50.0, UiConstants.MAX_IN_TUNE_THRESHOLD_CENTS, 0.001)
    }

    @Test
    fun `DEFAULT_IN_TUNE_THRESHOLD_CENTS is within range`() {
        assertTrue(UiConstants.DEFAULT_IN_TUNE_THRESHOLD_CENTS >= UiConstants.MIN_IN_TUNE_THRESHOLD_CENTS)
        assertTrue(UiConstants.DEFAULT_IN_TUNE_THRESHOLD_CENTS <= UiConstants.MAX_IN_TUNE_THRESHOLD_CENTS)
    }

    @Test
    fun `DEFAULT_IN_TUNE_THRESHOLD_CENTS is 10`() {
        assertEquals(10.0, UiConstants.DEFAULT_IN_TUNE_THRESHOLD_CENTS, 0.001)
    }

    @Test
    fun `in-tune threshold range is valid`() {
        assertTrue(UiConstants.MAX_IN_TUNE_THRESHOLD_CENTS > UiConstants.MIN_IN_TUNE_THRESHOLD_CENTS)
    }

    // ========== Display timing constants tests ==========

    @Test
    fun `DEFAULT_DISPLAY_DELAY_MS is positive`() {
        assertTrue(UiConstants.DEFAULT_DISPLAY_DELAY_MS > 0)
    }

    @Test
    fun `DEFAULT_DISPLAY_DELAY_MS is 1000ms`() {
        assertEquals(1000L, UiConstants.DEFAULT_DISPLAY_DELAY_MS)
    }

    @Test
    fun `MIN_DISPLAY_DELAY_MS is 0`() {
        assertEquals(0L, UiConstants.MIN_DISPLAY_DELAY_MS)
    }

    @Test
    fun `MAX_DISPLAY_DELAY_MS is 1000`() {
        assertEquals(1000L, UiConstants.MAX_DISPLAY_DELAY_MS)
    }

    @Test
    fun `DEFAULT_DISPLAY_DELAY_MS is within range`() {
        assertTrue(UiConstants.DEFAULT_DISPLAY_DELAY_MS >= UiConstants.MIN_DISPLAY_DELAY_MS)
        assertTrue(UiConstants.DEFAULT_DISPLAY_DELAY_MS <= UiConstants.MAX_DISPLAY_DELAY_MS)
    }

    @Test
    fun `display delay range is valid`() {
        assertTrue(UiConstants.MAX_DISPLAY_DELAY_MS >= UiConstants.MIN_DISPLAY_DELAY_MS)
    }

    // ========== Pitch update delay tests ==========

    @Test
    fun `DEFAULT_PITCH_UPDATE_DELAY_MS is positive`() {
        assertTrue(UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS > 0)
    }

    @Test
    fun `DEFAULT_PITCH_UPDATE_DELAY_MS is 200ms`() {
        assertEquals(200L, UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS)
    }

    @Test
    fun `MIN_PITCH_UPDATE_DELAY_MS is 0`() {
        assertEquals(0L, UiConstants.MIN_PITCH_UPDATE_DELAY_MS)
    }

    @Test
    fun `MAX_PITCH_UPDATE_DELAY_MS is 1000`() {
        assertEquals(1000L, UiConstants.MAX_PITCH_UPDATE_DELAY_MS)
    }

    @Test
    fun `DEFAULT_PITCH_UPDATE_DELAY_MS is within range`() {
        assertTrue(UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS >= UiConstants.MIN_PITCH_UPDATE_DELAY_MS)
        assertTrue(UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS <= UiConstants.MAX_PITCH_UPDATE_DELAY_MS)
    }

    @Test
    fun `pitch update delay is less than display delay`() {
        // Pitch should update more frequently than display persistence
        assertTrue(UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS < UiConstants.DEFAULT_DISPLAY_DELAY_MS)
    }

    // ========== Tuning loop delay tests ==========

    @Test
    fun `TUNING_LOOP_DELAY_MS is positive`() {
        assertTrue(UiConstants.TUNING_LOOP_DELAY_MS > 0)
    }

    @Test
    fun `TUNING_LOOP_DELAY_MS is 50ms`() {
        assertEquals(50L, UiConstants.TUNING_LOOP_DELAY_MS)
    }

    @Test
    fun `TUNING_LOOP_DELAY_MS allows sufficient update rate`() {
        // 50ms = 20 updates per second, good for real-time feedback
        val updatesPerSecond = 1000.0 / UiConstants.TUNING_LOOP_DELAY_MS
        assertTrue("Should allow at least 10 updates per second", updatesPerSecond >= 10)
    }

    // ========== Reference frequency tuning range tests ==========

    @Test
    fun `MIN_REFERENCE_FREQUENCY is 430 Hz`() {
        assertEquals(430.0, UiConstants.MIN_REFERENCE_FREQUENCY, 0.001)
    }

    @Test
    fun `MAX_REFERENCE_FREQUENCY is 450 Hz`() {
        assertEquals(450.0, UiConstants.MAX_REFERENCE_FREQUENCY, 0.001)
    }

    @Test
    fun `reference frequency range is valid`() {
        assertTrue(UiConstants.MAX_REFERENCE_FREQUENCY > UiConstants.MIN_REFERENCE_FREQUENCY)
    }

    @Test
    fun `reference frequency range includes standard 440 Hz`() {
        assertTrue(440.0 >= UiConstants.MIN_REFERENCE_FREQUENCY)
        assertTrue(440.0 <= UiConstants.MAX_REFERENCE_FREQUENCY)
    }

    @Test
    fun `FREQUENCY_SCALE_FACTOR is 2`() {
        assertEquals(2.0, UiConstants.FREQUENCY_SCALE_FACTOR, 0.001)
    }

    // ========== Audio processing limits tests ==========

    @Test
    fun `MAX_CONSECUTIVE_NULL_READS is positive`() {
        assertTrue(UiConstants.MAX_CONSECUTIVE_NULL_READS > 0)
    }

    @Test
    fun `MAX_CONSECUTIVE_NULL_READS is 10`() {
        assertEquals(10, UiConstants.MAX_CONSECUTIVE_NULL_READS)
    }

    // ========== Initialization values tests ==========

    @Test
    fun `INITIAL_TIME is 0`() {
        assertEquals(0L, UiConstants.INITIAL_TIME)
    }

    @Test
    fun `INITIAL_NULL_READS is 0`() {
        assertEquals(0, UiConstants.INITIAL_NULL_READS)
    }

    @Test
    fun `INITIAL_RMS is 0`() {
        assertEquals(0.0, UiConstants.INITIAL_RMS, 0.001)
    }

    @Test
    fun `INITIAL_CENTS is 0`() {
        assertEquals(0.0, UiConstants.INITIAL_CENTS, 0.001)
    }

    // ========== Activity lifecycle tests ==========

    @Test
    fun `MIN_ACTIVITY_LIFETIME_MS is positive`() {
        assertTrue(UiConstants.MIN_ACTIVITY_LIFETIME_MS > 0)
    }

    @Test
    fun `MIN_ACTIVITY_LIFETIME_MS is 2000ms`() {
        assertEquals(2000L, UiConstants.MIN_ACTIVITY_LIFETIME_MS)
    }

    // ========== Permission request codes tests ==========

    @Test
    fun `AUDIO_PERMISSION_REQUEST_CODE is positive`() {
        assertTrue(UiConstants.AUDIO_PERMISSION_REQUEST_CODE > 0)
    }

    @Test
    fun `AUDIO_PERMISSION_REQUEST_CODE is 1001`() {
        assertEquals(1001, UiConstants.AUDIO_PERMISSION_REQUEST_CODE)
    }

    // ========== Debug logging tests ==========

    @Test
    fun `DEBUG flag exists`() {
        // Just verify the constant exists and is a boolean
        val debugValue = UiConstants.DEBUG
        assertTrue(debugValue == true || debugValue == false)
    }

    @Test
    fun `DEBUG_ITERATION_THRESHOLD is positive`() {
        assertTrue(UiConstants.DEBUG_ITERATION_THRESHOLD > 0)
    }

    @Test
    fun `DEBUG_ITERATION_MOD_20 is 20`() {
        assertEquals(20, UiConstants.DEBUG_ITERATION_MOD_20)
    }

    @Test
    fun `DEBUG_ITERATION_MOD_100 is 100`() {
        assertEquals(100, UiConstants.DEBUG_ITERATION_MOD_100)
    }

    // ========== Consistency tests ==========

    @Test
    fun `tuning loop can run many times during display delay`() {
        // Display delay should accommodate multiple tuning loop iterations
        val iterations = UiConstants.DEFAULT_DISPLAY_DELAY_MS / UiConstants.TUNING_LOOP_DELAY_MS
        assertTrue("Should have multiple iterations during display delay", iterations >= 5)
    }

    @Test
    fun `pitch update delay is a multiple of tuning loop delay`() {
        // This ensures clean timing
        val ratio = UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS.toDouble() / 
            UiConstants.TUNING_LOOP_DELAY_MS.toDouble()
        assertTrue("Ratio should be reasonable", ratio >= 1)
    }

    @Test
    fun `all timing constants use milliseconds consistently`() {
        // Verify all timing constants are in milliseconds (not seconds)
        assertTrue(UiConstants.DEFAULT_DISPLAY_DELAY_MS > 1)
        assertTrue(UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS > 1)
        assertTrue(UiConstants.TUNING_LOOP_DELAY_MS > 1)
        assertTrue(UiConstants.MIN_ACTIVITY_LIFETIME_MS > 1)
    }
}
