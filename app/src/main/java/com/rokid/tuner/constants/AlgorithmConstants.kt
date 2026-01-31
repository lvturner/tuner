package com.rokid.tuner.constants

/**
 * Pitch detection algorithm constants, primarily for the YIN algorithm.
 */
object AlgorithmConstants {
    // Default threshold values (maximum sensitivity)
    const val DEFAULT_MIN_RMS_THRESHOLD = 0.0004  // 0.04% of max amplitude (balanced sensitivity)
    const val DEFAULT_CLARITY_THRESHOLD = 0.6     // Balanced clarity threshold to reject harmonics
    const val DEFAULT_PROBABILITY_THRESHOLD = 0.05f  // Very tolerant probability threshold
    
    // Sensitivity mapping formula constants (0-100 scale to thresholds)
    const val MAX_RMS_THRESHOLD = 0.01
    const val RMS_THRESHOLD_SCALE = 0.0096
    
    const val MIN_CLARITY_THRESHOLD = 0.05
    const val CLARITY_THRESHOLD_SCALE = 0.55
    
    const val MAX_PROBABILITY_THRESHOLD = 0.5f
    const val PROBABILITY_THRESHOLD_SCALE = 0.45f
    
    // YIN algorithm constants
    const val YIN_THRESHOLD_OFFSET = 0.08
    const val YIN_THRESHOLD_MULTIPLIER = 0.2125
    const val TYPICAL_MIN_YIN_THRESHOLD = 0.1
    const val TYPICAL_MAX_YIN_THRESHOLD = 0.2
    
    // Buffer and validation limits
    const val MIN_YIN_BUFFER_SIZE = 2
    const val DIVISOR_FOR_HALF_BUFFER = 2
    
    // Parabolic interpolation constants
    const val MIN_DENOMINATOR = 1e-10
    
    // Special values
    const val INVALID_FREQUENCY = 0.0
    const val INVALID_TAU = 0
    const val INITIAL_SUM = 0.0
    const val INITIAL_TAU = 0
    
    // Default synthetic audio generation
    const val SYNTHETIC_DURATION_SECONDS = 0.1  // 100ms
}