package com.rokid.tuner.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs

class AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val mutex = Mutex()
    private var readCounter = 0

    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_BYTES = 4096
        
        // Try different audio sources: DEFAULT, MIC, VOICE_RECOGNITION, CAMCORDER, UNPROCESSED
        private const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        
        // Calculate minimum buffer size in bytes
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
        
        // Buffer size in shorts (16-bit samples)
        private val bufferSizeShorts = minBufferSizeBytes / 2
        
        init {
            Log.d(TAG, "AudioRecorder config: sampleRate=$SAMPLE_RATE, minBufferSizeBytes=$minBufferSizeBytes, bufferSizeShorts=$bufferSizeShorts")
        }
    }

    fun start() {
        if (isRecording) return

        try {
            Log.d(TAG, "Creating AudioRecord: source=$AUDIO_SOURCE, sampleRate=$SAMPLE_RATE, channel=$CHANNEL_CONFIG, format=$AUDIO_FORMAT, bufferSize=${minBufferSizeBytes * 2}")
            
            audioRecord = AudioRecord(
                AUDIO_SOURCE,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufferSizeBytes * 2
            )
            
            val state = audioRecord?.state
            Log.d(TAG, "AudioRecord created, state=$state (1=initialized)")
            
            if (state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized properly")
                throw AudioRecordingException("AudioRecord not initialized")
            }

            audioRecord?.startRecording()
            val recordingState = audioRecord?.recordingState
            Log.d(TAG, "Recording state=$recordingState (3=recording)")
            isRecording = true
            Log.d(TAG, "Audio buffer size: min=$minBufferSizeBytes, actual=${minBufferSizeBytes * 2}")
            Log.d(TAG, "Audio recording started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            throw AudioRecordingException("Failed to start audio recording", e)
        }
    }

    suspend fun readNext(): FloatArray? = mutex.withLock {
        if (!isRecording) {
            Log.d(TAG, "Not recording")
            return null
        }
        

        
        // Real audio recording
        if (audioRecord == null) {
            Log.d(TAG, "audioRecord is null")
            return null
        }

        val buffer = ShortArray(bufferSizeShorts)
        val floatBuffer = FloatArray(bufferSizeShorts)
        
        val bytesRead = audioRecord!!.read(buffer, 0, bufferSizeShorts)
        
        if (bytesRead <= 0) {
            Log.d(TAG, "No audio data read (bytesRead=$bytesRead)")
            return null
        }
        
        readCounter++
        val shouldLog = readCounter <= 10 || readCounter % 20 == 0
        
        // Compute RMS and statistics
        var sum = 0.0
        var maxAbs = 0
        var sumSquares = 0.0
        for (i in 0 until bytesRead) {
            val sample = buffer[i].toInt()
            val abs = abs(sample)
            sum += abs.toDouble()
            sumSquares += sample.toDouble() * sample.toDouble()
            if (abs > maxAbs) maxAbs = abs
        }
        val avgAmplitude = sum / bytesRead
        val rms = Math.sqrt(sumSquares / bytesRead)
        
        if (shouldLog) {
            Log.d(TAG, "Read $bytesRead audio samples (shorts) (counter=$readCounter)")
            Log.d(TAG, "Average amplitude: $avgAmplitude, max amplitude: $maxAbs (max ${Short.MAX_VALUE})")
            Log.d(TAG, "RMS: $rms")
            // Log first 5 samples
            if (bytesRead >= 5) {
                Log.d(TAG, "First 5 samples: ${buffer[0]}, ${buffer[1]}, ${buffer[2]}, ${buffer[3]}, ${buffer[4]}")
            }
        }
        
        // Convert to float array for TarsosDSP
        for (i in 0 until bytesRead) {
            floatBuffer[i] = buffer[i].toFloat() / Short.MAX_VALUE.toFloat()
        }
        
        return floatBuffer.copyOf(bytesRead)
    }

    suspend fun stop() = mutex.withLock {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }



    fun isRecording(): Boolean = isRecording

    class AudioRecordingException(message: String, cause: Throwable? = null) : 
        Exception(message, cause)
}