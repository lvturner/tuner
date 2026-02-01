package com.rokid.tuner.audio

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AudioConfig object.
 * Verifies audio format and configuration constants.
 */
class AudioConfigTest {

    // ========== Audio format constants tests ==========

    @Test
    fun `SAMPLE_RATE is 44100 Hz`() {
        assertEquals(44100, AudioConfig.SAMPLE_RATE)
    }

    @Test
    fun `SAMPLE_RATE is standard CD quality`() {
        // 44100 Hz is CD quality and widely supported
        assertTrue(AudioConfig.SAMPLE_RATE >= 44100)
    }

    @Test
    fun `BUFFER_SIZE is power of 2`() {
        val bufferSize = AudioConfig.BUFFER_SIZE
        // Check if it's a power of 2
        assertTrue(bufferSize > 0 && (bufferSize and (bufferSize - 1)) == 0)
    }

    @Test
    fun `BUFFER_SIZE is 4096 samples`() {
        assertEquals(4096, AudioConfig.BUFFER_SIZE)
    }

    @Test
    fun `BUFFER_SIZE provides sufficient frequency resolution`() {
        // Buffer size should be large enough for low frequency detection
        // At 44100 Hz, 4096 samples = ~93ms of audio
        // This allows detection down to about 10-20 Hz
        val durationMs = 1000.0 * AudioConfig.BUFFER_SIZE / AudioConfig.SAMPLE_RATE
        assertTrue("Buffer should be at least 50ms for low freq detection", durationMs >= 50)
    }

    @Test
    fun `BITS_PER_SAMPLE is 16`() {
        assertEquals(16, AudioConfig.BITS_PER_SAMPLE)
    }

    @Test
    fun `BYTES_PER_SHORT is 2`() {
        assertEquals(2, AudioConfig.BYTES_PER_SHORT)
    }

    @Test
    fun `BITS_PER_SAMPLE and BYTES_PER_SHORT are consistent`() {
        assertEquals(AudioConfig.BITS_PER_SAMPLE / 8, AudioConfig.BYTES_PER_SHORT)
    }

    @Test
    fun `BUFFER_SIZE_MULTIPLIER is 2`() {
        assertEquals(2, AudioConfig.BUFFER_SIZE_MULTIPLIER)
    }

    // ========== AudioRecord state constants tests ==========

    @Test
    fun `AUDIO_RECORD_INITIALIZED equals 1`() {
        // Matches AudioRecord.STATE_INITIALIZED
        assertEquals(1, AudioConfig.AUDIO_RECORD_INITIALIZED)
    }

    @Test
    fun `AUDIO_RECORD_RECORDING equals 3`() {
        // Matches AudioRecord.RECORDSTATE_RECORDING
        assertEquals(3, AudioConfig.AUDIO_RECORD_RECORDING)
    }

    // ========== Audio processing constants tests ==========

    @Test
    fun `DEFAULT_REFERENCE_FREQUENCY is 440 Hz`() {
        assertEquals(440.0, AudioConfig.DEFAULT_REFERENCE_FREQUENCY, 0.001)
    }

    @Test
    fun `DEFAULT_REFERENCE_FREQUENCY is standard A4 pitch`() {
        // International standard pitch
        assertEquals(440.0, AudioConfig.DEFAULT_REFERENCE_FREQUENCY, 0.001)
    }

    @Test
    fun `MAX_CENTS_DEVIATION is 50`() {
        assertEquals(50.0, AudioConfig.MAX_CENTS_DEVIATION, 0.001)
    }

    @Test
    fun `MAX_CENTS_DEVIATION is half a semitone`() {
        // 50 cents = half way between two adjacent notes
        assertEquals(50.0, AudioConfig.MAX_CENTS_DEVIATION, 0.001)
    }

    // ========== Logging and debugging constants tests ==========

    @Test
    fun `INITIAL_LOG_THRESHOLD is positive`() {
        assertTrue(AudioConfig.INITIAL_LOG_THRESHOLD > 0)
    }

    @Test
    fun `LOG_FREQUENCY_MODULUS is positive`() {
        assertTrue(AudioConfig.LOG_FREQUENCY_MODULUS > 0)
    }

    @Test
    fun `SAMPLES_TO_LOG is reasonable`() {
        assertTrue(AudioConfig.SAMPLES_TO_LOG > 0)
        assertTrue(AudioConfig.SAMPLES_TO_LOG <= 20)
    }

    // ========== Buffer operations constants tests ==========

    @Test
    fun `BUFFER_READ_OFFSET is 0`() {
        assertEquals(0, AudioConfig.BUFFER_READ_OFFSET)
    }

    @Test
    fun `NO_DATA_READ is 0`() {
        assertEquals(0, AudioConfig.NO_DATA_READ)
    }

    @Test
    fun `INITIAL_READ_COUNTER is 0`() {
        assertEquals(0, AudioConfig.INITIAL_READ_COUNTER)
    }

    // ========== Initialization values tests ==========

    @Test
    fun `INITIAL_SUM is 0`() {
        assertEquals(0.0, AudioConfig.INITIAL_SUM, 0.001)
    }

    @Test
    fun `INITIAL_MAX_AMPLITUDE is 0`() {
        assertEquals(0, AudioConfig.INITIAL_MAX_AMPLITUDE)
    }

    @Test
    fun `INITIAL_SUM_SQUARES is 0`() {
        assertEquals(0.0, AudioConfig.INITIAL_SUM_SQUARES, 0.001)
    }

    // ========== Nyquist frequency tests ==========

    @Test
    fun `sample rate supports guitar frequency range`() {
        // Nyquist frequency = sample rate / 2
        val nyquist = AudioConfig.SAMPLE_RATE / 2.0
        
        // Should be able to detect up to at least 1350 Hz (max guitar frequency)
        assertTrue("Nyquist frequency should exceed max guitar frequency", nyquist > 1350)
    }

    // ========== Buffer size and sample rate relationship tests ==========

    @Test
    fun `buffer provides reasonable update rate`() {
        // Time for one buffer = buffer_size / sample_rate
        val bufferDurationSeconds = AudioConfig.BUFFER_SIZE.toDouble() / AudioConfig.SAMPLE_RATE
        val updateFrequency = 1.0 / bufferDurationSeconds
        
        // Should allow at least 10 updates per second
        assertTrue("Update frequency should be > 10 Hz", updateFrequency > 10)
    }

    @Test
    fun `buffer size in bytes is calculable`() {
        val bufferSizeBytes = AudioConfig.BUFFER_SIZE * AudioConfig.BYTES_PER_SHORT
        assertEquals(8192, bufferSizeBytes)
    }
}
