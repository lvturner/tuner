package com.rokid.tuner.constants

/**
 * UI and user configuration constants for the tuner application.
 */
object UiConstants {
    // Sensitivity configuration (0-100 scale)
    const val MIN_SENSITIVITY = 0
    const val MAX_SENSITIVITY = 100
    const val DEFAULT_SENSITIVITY = 100  // Maximum sensitivity by default (used in MainActivity)
    const val SETTINGS_DEFAULT_SENSITIVITY = 50  // Midpoint default for settings dialog
    
    // Display timing constants (milliseconds)
    const val DEFAULT_DISPLAY_DELAY_MS = 1000L
    const val MIN_DISPLAY_DELAY_MS = 0L
    const val MAX_DISPLAY_DELAY_MS = 1000L
    
    const val DEFAULT_PITCH_UPDATE_DELAY_MS = 200L
    const val MIN_PITCH_UPDATE_DELAY_MS = 0L
    const val MAX_PITCH_UPDATE_DELAY_MS = 1000L
    
    // Tuning loop delay (controls update frequency)
    const val TUNING_LOOP_DELAY_MS = 50L
    
    // Reference frequency tuning range (A4 calibration)
    const val MIN_REFERENCE_FREQUENCY = 430.0
    const val MAX_REFERENCE_FREQUENCY = 450.0
    const val FREQUENCY_SCALE_FACTOR = 2.0  // Used for seekbar scaling
    
    // Audio processing limits
    const val MAX_CONSECUTIVE_NULL_READS = 10
    
    // Initialization values
    const val INITIAL_TIME = 0L
    const val INITIAL_NULL_READS = 0
    const val INITIAL_RMS = 0.0
    const val INITIAL_CENTS = 0.0
    
    // Activity lifecycle
    const val MIN_ACTIVITY_LIFETIME_MS = 2000L
    
    // Permission request codes
    const val AUDIO_PERMISSION_REQUEST_CODE = 1001
    
    // Debug logging
    const val DEBUG = true
    const val DEBUG_ITERATION_THRESHOLD = 10
    const val DEBUG_ITERATION_MOD_20 = 20
    const val DEBUG_ITERATION_MOD_100 = 100
}