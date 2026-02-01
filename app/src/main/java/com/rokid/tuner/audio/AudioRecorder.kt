package com.rokid.tuner.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private val lock = Any()
    private val readCounter = AtomicInteger(AudioConfig.INITIAL_READ_COUNTER)

    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = AudioConfig.SAMPLE_RATE
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_BYTES = AudioConfig.BUFFER_SIZE
        
        private const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        
        private val minBufferSizeBytes = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ).let { size ->
            if (size == AudioRecord.ERROR || size == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid audio parameters or sample rate not supported")
                BUFFER_SIZE_BYTES
            } else {
                size.coerceAtLeast(BUFFER_SIZE_BYTES)
            }
        }
        
        private val bufferSizeShorts = minBufferSizeBytes / AudioConfig.BYTES_PER_SHORT
        
        init {
            Log.d(TAG, "AudioRecorder config: sampleRate=$SAMPLE_RATE, minBufferSizeBytes=$minBufferSizeBytes, bufferSizeShorts=$bufferSizeShorts")
        }
    }

    fun start() {
        synchronized(lock) {
            if (isRecording.get()) return

            try {
                Log.d(TAG, "Creating AudioRecord: source=$AUDIO_SOURCE, sampleRate=$SAMPLE_RATE, channel=$CHANNEL_CONFIG, format=$AUDIO_FORMAT, bufferSize=${minBufferSizeBytes * AudioConfig.BUFFER_SIZE_MULTIPLIER}")
                
                audioRecord = AudioRecord(
                    AUDIO_SOURCE,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    minBufferSizeBytes * AudioConfig.BUFFER_SIZE_MULTIPLIER
                )
                
                val state = audioRecord?.state
                Log.d(TAG, "AudioRecord created, state=$state (${AudioConfig.AUDIO_RECORD_INITIALIZED}=initialized)")
                
                if (state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord not initialized properly")
                    throw AudioRecordingException("AudioRecord not initialized")
                }

                audioRecord?.startRecording()
                val recordingState = audioRecord?.recordingState
                Log.d(TAG, "Recording state=$recordingState (${AudioConfig.AUDIO_RECORD_RECORDING}=recording)")
                isRecording.set(true)
                readCounter.set(AudioConfig.INITIAL_READ_COUNTER)
                Log.d(TAG, "Audio buffer size: min=$minBufferSizeBytes, actual=${minBufferSizeBytes * AudioConfig.BUFFER_SIZE_MULTIPLIER}")
                Log.d(TAG, "Audio recording started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start audio recording", e)
                throw AudioRecordingException("Failed to start audio recording", e)
            }
        }
    }

    fun readNext(): FloatArray? {
        if (!isRecording.get()) {
            Log.d(TAG, "Not recording")
            return null
        }
        
        val recorder = synchronized(lock) { audioRecord } ?: run {
            Log.d(TAG, "audioRecord is null")
            return null
        }

        val buffer = ShortArray(bufferSizeShorts)
        val floatBuffer = FloatArray(bufferSizeShorts)
        
        val bytesRead = recorder.read(buffer, AudioConfig.BUFFER_READ_OFFSET, bufferSizeShorts)
        
        if (bytesRead <= AudioConfig.NO_DATA_READ) {
            Log.d(TAG, "No audio data read (bytesRead=$bytesRead)")
            return null
        }
        
        val currentCount = readCounter.incrementAndGet()
        val shouldLog = currentCount <= AudioConfig.INITIAL_LOG_THRESHOLD || currentCount % AudioConfig.LOG_FREQUENCY_MODULUS == 0
        
        // Compute RMS and statistics
        var sum = AudioConfig.INITIAL_SUM
        var maxAbs = AudioConfig.INITIAL_MAX_AMPLITUDE
        var sumSquares = AudioConfig.INITIAL_SUM_SQUARES
        for (i in 0 until bytesRead) {
            val sample = buffer[i].toInt()
            val absVal = abs(sample)
            sum += absVal.toDouble()
            sumSquares += sample.toDouble() * sample.toDouble()
            if (absVal > maxAbs) maxAbs = absVal
        }
        val avgAmplitude = sum / bytesRead
        val rms = Math.sqrt(sumSquares / bytesRead)
        
        if (shouldLog) {
            Log.d(TAG, "Read $bytesRead audio samples (shorts) (counter=$currentCount)")
            Log.d(TAG, "Average amplitude: $avgAmplitude, max amplitude: $maxAbs (max ${Short.MAX_VALUE})")
            Log.d(TAG, "RMS: $rms")
            if (bytesRead >= AudioConfig.SAMPLES_TO_LOG) {
                Log.d(TAG, "First 5 samples: ${buffer[0]}, ${buffer[1]}, ${buffer[2]}, ${buffer[3]}, ${buffer[4]}")
            }
        }
        
        // Convert to float array for pitch detection
        for (i in 0 until bytesRead) {
            floatBuffer[i] = buffer[i].toFloat() / Short.MAX_VALUE.toFloat()
        }
        
        return floatBuffer.copyOf(bytesRead)
    }

    fun stop() {
        synchronized(lock) {
            isRecording.set(false)
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
    }

    fun isRecording(): Boolean = isRecording.get()

    class AudioRecordingException(message: String, cause: Throwable? = null) : 
        Exception(message, cause)
}