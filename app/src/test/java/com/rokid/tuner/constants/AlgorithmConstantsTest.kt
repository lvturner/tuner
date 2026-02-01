package com.rokid.tuner.constants

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AlgorithmConstants object.
 * Verifies algorithm threshold values and their relationships.
 */
class AlgorithmConstantsTest {

    // ========== Default threshold tests ==========

    @Test
    fun `DEFAULT_MIN_RMS_THRESHOLD is positive`() {
        assertTrue(AlgorithmConstants.DEFAULT_MIN_RMS_THRESHOLD > 0)
    }

    @Test
    fun `DEFAULT_MIN_RMS_THRESHOLD is small fraction`() {
        // Should be a small value for sensitivity
        assertTrue(AlgorithmConstants.DEFAULT_MIN_RMS_THRESHOLD < 0.01)
    }

    @Test
    fun `DEFAULT_CLARITY_THRESHOLD is between 0 and 1`() {
        assertTrue(AlgorithmConstants.DEFAULT_CLARITY_THRESHOLD > 0)
        assertTrue(AlgorithmConstants.DEFAULT_CLARITY_THRESHOLD <= 1.0)
    }

    @Test
    fun `DEFAULT_PROBABILITY_THRESHOLD is between 0 and 1`() {
        assertTrue(AlgorithmConstants.DEFAULT_PROBABILITY_THRESHOLD >= 0f)
        assertTrue(AlgorithmConstants.DEFAULT_PROBABILITY_THRESHOLD <= 1f)
    }

    // ========== Sensitivity mapping constants tests ==========

    @Test
    fun `MAX_RMS_THRESHOLD is greater than minimum threshold`() {
        assertTrue(AlgorithmConstants.MAX_RMS_THRESHOLD > AlgorithmConstants.DEFAULT_MIN_RMS_THRESHOLD)
    }

    @Test
    fun `RMS_THRESHOLD_SCALE allows full range mapping`() {
        // At sensitivity 100, threshold should be: MAX - SCALE
        val minThreshold = AlgorithmConstants.MAX_RMS_THRESHOLD - AlgorithmConstants.RMS_THRESHOLD_SCALE
        assertTrue("Minimum mapped threshold should be positive", minThreshold >= 0)
        assertTrue("Minimum mapped threshold should be <= DEFAULT_MIN_RMS_THRESHOLD", 
            minThreshold <= AlgorithmConstants.DEFAULT_MIN_RMS_THRESHOLD + 0.001)
    }

    @Test
    fun `MIN_CLARITY_THRESHOLD is positive`() {
        assertTrue(AlgorithmConstants.MIN_CLARITY_THRESHOLD > 0)
    }

    @Test
    fun `CLARITY_THRESHOLD_SCALE allows reasonable range`() {
        val maxClarityThreshold = AlgorithmConstants.MIN_CLARITY_THRESHOLD + AlgorithmConstants.CLARITY_THRESHOLD_SCALE
        assertTrue("Max clarity threshold should be <= 1.0", maxClarityThreshold <= 1.0)
    }

    @Test
    fun `MAX_PROBABILITY_THRESHOLD is between 0 and 1`() {
        assertTrue(AlgorithmConstants.MAX_PROBABILITY_THRESHOLD > 0f)
        assertTrue(AlgorithmConstants.MAX_PROBABILITY_THRESHOLD <= 1f)
    }

    @Test
    fun `PROBABILITY_THRESHOLD_SCALE allows reasonable range`() {
        val minProbThreshold = AlgorithmConstants.MAX_PROBABILITY_THRESHOLD - AlgorithmConstants.PROBABILITY_THRESHOLD_SCALE
        assertTrue("Min probability threshold should be >= 0", minProbThreshold >= 0f)
    }

    // ========== YIN algorithm constants tests ==========

    @Test
    fun `YIN_THRESHOLD_OFFSET is positive`() {
        assertTrue(AlgorithmConstants.YIN_THRESHOLD_OFFSET > 0)
    }

    @Test
    fun `YIN_THRESHOLD_MULTIPLIER is positive`() {
        assertTrue(AlgorithmConstants.YIN_THRESHOLD_MULTIPLIER > 0)
    }

    @Test
    fun `YIN threshold range is reasonable`() {
        // YIN threshold typically ranges from 0.1 to 0.2
        assertTrue(AlgorithmConstants.TYPICAL_MIN_YIN_THRESHOLD > 0)
        assertTrue(AlgorithmConstants.TYPICAL_MAX_YIN_THRESHOLD > AlgorithmConstants.TYPICAL_MIN_YIN_THRESHOLD)
        assertTrue(AlgorithmConstants.TYPICAL_MAX_YIN_THRESHOLD <= 1.0)
    }

    @Test
    fun `YIN threshold calculation produces valid range`() {
        // With clarity threshold at max (DEFAULT_CLARITY_THRESHOLD)
        val threshold = AlgorithmConstants.YIN_THRESHOLD_OFFSET + 
            AlgorithmConstants.DEFAULT_CLARITY_THRESHOLD * AlgorithmConstants.YIN_THRESHOLD_MULTIPLIER
        assertTrue("Calculated YIN threshold should be positive", threshold > 0)
        assertTrue("Calculated YIN threshold should be reasonable", threshold < 1.0)
    }

    // ========== Buffer and validation constants tests ==========

    @Test
    fun `MIN_YIN_BUFFER_SIZE is reasonable`() {
        assertTrue(AlgorithmConstants.MIN_YIN_BUFFER_SIZE >= 2)
    }

    @Test
    fun `DIVISOR_FOR_HALF_BUFFER equals 2`() {
        assertEquals(2, AlgorithmConstants.DIVISOR_FOR_HALF_BUFFER)
    }

    // ========== Parabolic interpolation constants tests ==========

    @Test
    fun `MIN_DENOMINATOR is very small positive value`() {
        assertTrue(AlgorithmConstants.MIN_DENOMINATOR > 0)
        assertTrue(AlgorithmConstants.MIN_DENOMINATOR < 1e-6)
    }

    // ========== Special values tests ==========

    @Test
    fun `INVALID_FREQUENCY is zero`() {
        assertEquals(0.0, AlgorithmConstants.INVALID_FREQUENCY, 0.001)
    }

    @Test
    fun `INVALID_TAU is zero`() {
        assertEquals(0, AlgorithmConstants.INVALID_TAU)
    }

    @Test
    fun `INITIAL_SUM is zero`() {
        assertEquals(0.0, AlgorithmConstants.INITIAL_SUM, 0.001)
    }

    @Test
    fun `INITIAL_TAU is zero`() {
        assertEquals(0, AlgorithmConstants.INITIAL_TAU)
    }

    // ========== Synthetic audio constants tests ==========

    @Test
    fun `SYNTHETIC_DURATION_SECONDS is positive`() {
        assertTrue(AlgorithmConstants.SYNTHETIC_DURATION_SECONDS > 0)
    }

    @Test
    fun `SYNTHETIC_DURATION_SECONDS is reasonable for testing`() {
        // Should be long enough to contain multiple cycles of low frequencies
        assertTrue(AlgorithmConstants.SYNTHETIC_DURATION_SECONDS >= 0.05)
        // But not too long for efficiency
        assertTrue(AlgorithmConstants.SYNTHETIC_DURATION_SECONDS <= 1.0)
    }

    // ========== Consistency tests ==========

    @Test
    fun `sensitivity mapping is consistent`() {
        // At sensitivity 0: thresholds should be strictest
        // At sensitivity 100: thresholds should be most lenient for detection
        
        // RMS: higher threshold = less sensitive (need louder signal)
        // So at sensitivity 0: MAX threshold, at 100: lowest threshold
        val rmsAt0 = AlgorithmConstants.MAX_RMS_THRESHOLD
        val rmsAt100 = AlgorithmConstants.MAX_RMS_THRESHOLD - AlgorithmConstants.RMS_THRESHOLD_SCALE
        assertTrue("RMS threshold should decrease with higher sensitivity", rmsAt0 > rmsAt100)
        
        // Clarity: higher threshold = more tolerant (accept noisier signals)
        val clarityAt0 = AlgorithmConstants.MIN_CLARITY_THRESHOLD
        val clarityAt100 = AlgorithmConstants.MIN_CLARITY_THRESHOLD + AlgorithmConstants.CLARITY_THRESHOLD_SCALE
        assertTrue("Clarity threshold should increase with higher sensitivity", clarityAt100 > clarityAt0)
        
        // Probability: lower threshold = more tolerant (accept lower confidence)
        val probAt0 = AlgorithmConstants.MAX_PROBABILITY_THRESHOLD
        val probAt100 = AlgorithmConstants.MAX_PROBABILITY_THRESHOLD - AlgorithmConstants.PROBABILITY_THRESHOLD_SCALE
        assertTrue("Probability threshold should decrease with higher sensitivity", probAt0 > probAt100)
    }
}
