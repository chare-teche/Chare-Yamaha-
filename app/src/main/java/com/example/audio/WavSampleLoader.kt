package com.example.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Parses standard PCM 16-bit or 8-bit WAV audio files into normalized mono FloatArray buffers
 * for real-time polyphonic pitch-shifted playback in SynthAudioEngine.
 * Always called off the real-time audio thread (Dispatchers.IO).
 */
data class LoadedWavSample(
    val key: String,
    val name: String,
    val sampleRate: Int,
    val samples: FloatArray,
    val rootMidiNote: Int = 60
)

object WavSampleLoader {

    fun parseWav(key: String, name: String, bytes: ByteArray): LoadedWavSample? {
        if (bytes.size < 44) return null
        val riff = String(bytes, 0, 4, Charsets.US_ASCII)
        val wave = String(bytes, 8, 4, Charsets.US_ASCII)
        if (riff != "RIFF" || wave != "WAVE") return null

        var pos = 12
        var audioFormat = 1
        var channels = 1
        var sampleRate = 44100
        var bitsPerSample = 16
        var dataOffset = -1
        var dataSize = 0

        while (pos + 8 <= bytes.size) {
            val chunkId = String(bytes, pos, 4, Charsets.US_ASCII)
            val chunkSize = readInt32LE(bytes, pos + 4)
            pos += 8
            if (chunkSize < 0 || pos + chunkSize > bytes.size) break

            when (chunkId) {
                "fmt " -> if (chunkSize >= 16) {
                    audioFormat = readUInt16LE(bytes, pos)
                    channels = readUInt16LE(bytes, pos + 2).coerceIn(1, 2)
                    sampleRate = readInt32LE(bytes, pos + 4).coerceIn(8000, 96000)
                    bitsPerSample = readUInt16LE(bytes, pos + 14)
                }
                "data" -> {
                    dataOffset = pos
                    dataSize = chunkSize
                }
            }
            pos += chunkSize + (chunkSize and 1)
        }

        if (audioFormat != 1 || dataOffset < 0 || dataSize <= 0) return null
        if (bitsPerSample != 16 && bitsPerSample != 8) return null

        val bytesPerSample = bitsPerSample / 8
        val frameSize = bytesPerSample * channels
        val maxFrames = minOf(dataSize / frameSize, sampleRate * 8) // Cap at 8 sec to protect RAM
        if (maxFrames <= 0) return null

        val out = FloatArray(maxFrames)
        val bb = ByteBuffer.wrap(bytes, dataOffset, maxFrames * frameSize).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until maxFrames) {
            var sum = 0f
            for (ch in 0 until channels) {
                sum += if (bitsPerSample == 16) {
                    bb.short / 32768f
                } else {
                    ((bb.get().toInt() and 0xFF) - 128) / 128f
                }
            }
            out[i] = (sum / channels).coerceIn(-1f, 1f)
        }

        return LoadedWavSample(
            key = key,
            name = name.substringBeforeLast('.').ifBlank { name },
            sampleRate = sampleRate,
            samples = out
        )
    }

    private fun readInt32LE(b: ByteArray, o: Int): Int {
        return (b[o].toInt() and 0xFF) or
            ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or
            ((b[o + 3].toInt() and 0xFF) shl 24)
    }

    private fun readUInt16LE(b: ByteArray, o: Int): Int {
        return (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)
    }
}
