package com.rokid.tuner.audio

object AudioConfig {
    // Audio format constants
    const val SAMPLE_RATE = 44100
    const val BUFFER_SIZE = 4096
    const val BITS_PER_SAMPLE = 16
    const val BYTES_PER_SHORT = 2
    const val BUFFER_SIZE_MULTIPLIER = 2
    
    // AudioRecord state constants
    const val AUDIO_RECORD_INITIALIZED = 1
    const val AUDIO_RECORD_RECORDING = 3
    
    // Audio processing constants
    const val DEFAULT_REFERENCE_FREQUENCY = 440.0 // A4
    const val MAX_CENTS_DEVIATION = 50.0 // ±50 cents display range
    
    // Logging and debugging
    const val INITIAL_LOG_THRESHOLD = 10
    const val LOG_FREQUENCY_MODULUS = 20
    const val SAMPLES_TO_LOG = 5
    
    // Buffer operations
    const val BUFFER_READ_OFFSET = 0
    const val NO_DATA_READ = 0
    const val INITIAL_READ_COUNTER = 0
    
    // Initialization values
    const val INITIAL_SUM = 0.0
    const val INITIAL_MAX_AMPLITUDE = 0
    const val INITIAL_SUM_SQUARES = 0.0
}