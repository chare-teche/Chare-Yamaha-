package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import com.example.domain.model.AccompPart
import com.example.domain.model.BuiltInVoices
import com.example.domain.model.DrumSound
import com.example.domain.model.SynthEngineType
import com.example.domain.model.VoicePreset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Real-time low-latency polyphonic PCM software synthesizer, sample player, 9-part mixer,
 * and stereo DSP processor (Reverb, Chorus, 3-Band EQ, Master Tune, Modulation, Soft Limiter).
 * Zero heap allocations occur inside the audio render loop to prevent GC glitches.
 */
class SynthAudioEngine {

    companion object {
        const val SAMPLE_RATE = 44100
        private const val TWO_PI = 2.0 * PI
        private const val MAX_MELODIC_VOICES = 64
        private const val MAX_DRUM_VOICES = 16
    }

    private enum class EnvStage { ATTACK, DECAY, SUSTAIN, RELEASE, OFF }

    private class MelodicVoice {
        @Volatile var active = false
        var keyTag = 0L
        var midiNote = 60
        var velocity = 0.8f
        var partGainScale = 1.0f
        var panOverride: Float? = null
        var part = AccompPart.RIGHT_VOICE
        var preset: VoicePreset = BuiltInVoices.all.first()
        var phase = 0.0
        var phase2 = 0.0
        var modPhase = 0.0
        var lfoPhase = 0.0
        var samplePos = 0.0
        var sampleRef: LoadedWavSample? = null
        var envStage = EnvStage.OFF
        var envValue = 0f
        var lowPassState = 0f
        var sustained = false
        var ageSamples = 0L
    }

    private class DrumVoice {
        @Volatile var active = false
        var sound: DrumSound = DrumSound.KICK
        var velocity = 0.8f
        var part = AccompPart.DRUMS
        var phase = 0.0
        var elapsedSec = 0f
        var durationSec = 0.25f
        var noiseSeed = 123456789
        var lastNoise = 0f
    }

    private val melodicVoices = Array(MAX_MELODIC_VOICES) { MelodicVoice() }
    private val drumVoices = Array(MAX_DRUM_VOICES) { DrumVoice() }
    private val loadedSamples = ConcurrentHashMap<String, LoadedWavSample>()
    private val voiceLock = Any()

    private val running = AtomicBoolean(false)
    private var audioThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    @Volatile var bufferFrames: Int = 512
    @Volatile var masterVolume: Float = 0.85f
    @Volatile var acmpBalance: Float = 0.55f
    @Volatile var styleVolume: Float = 0.82f
    @Volatile var fadeGain: Float = 1.0f
    @Volatile var pitchBendSemitones: Float = 0f
    @Volatile var modulationDepth: Float = 0f
    @Volatile var sustainPedalDown: Boolean = false
    @Volatile var masterTuneHz: Float = 440.0f

    // Real-time DSP Effect parameters
    @Volatile var isDspEnabled: Boolean = true
    @Volatile var reverbDepth: Float = 0.28f
    @Volatile var chorusDepth: Float = 0.20f
    @Volatile var eqBass: Float = 0.5f
    @Volatile var eqMid: Float = 0.5f
    @Volatile var eqTreble: Float = 0.55f

    // Lock-free output meter for top status bar
    @Volatile var currentPeakLevel: Float = 0f

    // Pre-allocated DSP delay lines (zero allocations inside renderLoop!)
    private val combBuf1 = FloatArray(1557)
    private val combBuf2 = FloatArray(1617)
    private val combBuf3 = FloatArray(1491)
    private val combBuf4 = FloatArray(1422)
    private val allpassBuf = FloatArray(556)
    private val chorusBufL = FloatArray(2048)
    private val chorusBufR = FloatArray(2048)

    private val partVolumes = ConcurrentHashMap<AccompPart, Float>().apply {
        AccompPart.entries.forEach { put(it, 0.82f) }
    }
    private val partPans = ConcurrentHashMap<AccompPart, Float>().apply {
        AccompPart.entries.forEach { put(it, defaultPartPan(it)) }
    }
    private val partMutes = ConcurrentHashMap<AccompPart, Boolean>().apply {
        AccompPart.entries.forEach { put(it, false) }
    }

    fun isRunning(): Boolean = running.get()

    fun registerWavSample(sample: LoadedWavSample) {
        loadedSamples[sample.key] = sample
    }

    fun restartWithBufferFrames(frames: Int): Boolean {
        bufferFrames = frames.coerceIn(256, 1024)
        stop()
        return start()
    }

    fun start(): Boolean {
        if (running.getAndSet(true)) return true
        return try {
            val frames = bufferFrames.coerceIn(256, 1024)
            val minBufBytes = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(frames * 4)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(minBufBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()

            audioTrack = track
            track.play()

            val thread = Thread({ renderLoop(track, frames) }, "EthioYamahaDspAudioThread")
            thread.start()
            audioThread = thread
            true
        } catch (_: Throwable) {
            running.set(false)
            false
        }
    }

    fun stop() {
        running.set(false)
        try {
            audioThread?.join(350)
        } catch (_: InterruptedException) {
            // ignore
        }
        audioThread = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Throwable) {
            // ignore
        }
        audioTrack = null
        currentPeakLevel = 0f
    }

    fun setPartVolume(part: AccompPart, volume: Float) {
        partVolumes[part] = volume.coerceIn(0f, 1f)
    }

    fun setPartPan(part: AccompPart, pan: Float) {
        partPans[part] = pan.coerceIn(-1f, 1f)
    }

    fun setPartMuted(part: AccompPart, muted: Boolean) {
        partMutes[part] = muted
    }

    fun setSustain(enabled: Boolean) {
        sustainPedalDown = enabled
        if (!enabled) {
            synchronized(voiceLock) {
                for (v in melodicVoices) {
                    if (v.active && v.sustained && v.part == AccompPart.RIGHT_VOICE) {
                        v.sustained = false
                        v.envStage = EnvStage.RELEASE
                    }
                }
            }
        }
    }

    fun noteOn(
        midiNote: Int,
        velocity: Float,
        preset: VoicePreset,
        part: AccompPart = AccompPart.RIGHT_VOICE,
        tag: Long = midiNote.toLong(),
        gainScale: Float = 1.0f,
        panOverride: Float? = null
    ) {
        val sample = preset.sampleKey?.let { loadedSamples[it] }
        synchronized(voiceLock) {
            var chosen: MelodicVoice? = melodicVoices.firstOrNull {
                it.active && it.keyTag == tag && it.part == part
            }
            if (chosen == null) {
                chosen = melodicVoices.firstOrNull { !it.active }
            }
            if (chosen == null) {
                // Prefer stealing the oldest releasing voice first so held notes across 4 parts stay intact
                chosen = melodicVoices
                    .filter { it.envStage == EnvStage.RELEASE }
                    .maxByOrNull { it.ageSamples }
                    ?: if (part == AccompPart.RIGHT_VOICE) {
                        // Protect live multi-part player notes by stealing accompaniment voices before player voices
                        melodicVoices
                            .filter { it.part != AccompPart.RIGHT_VOICE }
                            .maxByOrNull { it.ageSamples }
                            ?: melodicVoices.maxByOrNull { it.ageSamples }
                    } else {
                        melodicVoices.maxByOrNull { it.ageSamples }
                    }
            }
            chosen?.apply {
                this.keyTag = tag
                this.midiNote = midiNote.coerceIn(12, 115)
                this.velocity = velocity.coerceIn(0.05f, 1.0f)
                this.partGainScale = gainScale.coerceIn(0.05f, 1.5f)
                this.panOverride = panOverride?.coerceIn(-1f, 1f)
                this.part = part
                this.preset = preset
                this.phase = 0.0
                this.phase2 = 0.0
                this.modPhase = 0.0
                this.lfoPhase = 0.0
                this.samplePos = 0.0
                this.sampleRef = sample
                this.envStage = EnvStage.ATTACK
                this.envValue = 0f
                this.lowPassState = 0f
                this.sustained = false
                this.ageSamples = 0L
                this.active = true
            }
        }
    }

    fun noteOff(tag: Long, part: AccompPart = AccompPart.RIGHT_VOICE) {
        synchronized(voiceLock) {
            for (v in melodicVoices) {
                if (v.active && v.keyTag == tag && v.part == part) {
                    if (sustainPedalDown && part == AccompPart.RIGHT_VOICE) {
                        v.sustained = true
                    } else if (v.envStage != EnvStage.RELEASE) {
                        v.envStage = EnvStage.RELEASE
                    }
                }
            }
        }
    }

    fun releaseTagRange(minTag: Long, maxTag: Long) {
        synchronized(voiceLock) {
            for (v in melodicVoices) {
                if (v.active && v.keyTag in minTag..maxTag) {
                    v.sustained = false
                    if (v.envStage != EnvStage.RELEASE) {
                        v.envStage = EnvStage.RELEASE
                    }
                }
            }
        }
    }

    fun allAcmpNotesOff() {
        synchronized(voiceLock) {
            for (v in melodicVoices) {
                if (v.active && v.part != AccompPart.RIGHT_VOICE) {
                    v.envStage = EnvStage.RELEASE
                }
            }
        }
    }

    fun allNotesOff() {
        synchronized(voiceLock) {
            for (v in melodicVoices) {
                if (v.active) {
                    v.sustained = false
                    v.envStage = EnvStage.RELEASE
                }
            }
        }
    }

    fun triggerDrum(
        sound: DrumSound,
        velocity: Float = 0.85f,
        part: AccompPart = AccompPart.DRUMS
    ) {
        synchronized(voiceLock) {
            val slot = drumVoices.firstOrNull { !it.active }
                ?: drumVoices.maxByOrNull { it.elapsedSec }
            slot?.apply {
                this.sound = sound
                this.velocity = velocity.coerceIn(0.05f, 1.0f)
                this.part = part
                this.phase = 0.0
                this.elapsedSec = 0f
                this.durationSec = when (sound) {
                    DrumSound.KICK -> 0.28f
                    DrumSound.SNARE -> 0.22f
                    DrumSound.CLOSED_HAT -> 0.07f
                    DrumSound.OPEN_HAT -> 0.34f
                    DrumSound.KEBERO_LOW -> 0.34f
                    DrumSound.KEBERO_HIGH -> 0.16f
                    DrumSound.CLAP -> 0.19f
                    DrumSound.CRASH -> 0.78f
                    DrumSound.SHAKER -> 0.09f
                    DrumSound.RIM -> 0.06f
                    DrumSound.METRO_HIGH, DrumSound.METRO_LOW -> 0.055f
                }
                this.noiseSeed = (System.nanoTime().toInt() xor 0x5A3C91E7)
                this.lastNoise = 0f
                this.active = true
            }
        }
    }

    private fun renderLoop(track: AudioTrack, framesPerWrite: Int) {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        } catch (_: Throwable) {
            // ignore
        }

        val pcmBuffer = ShortArray(framesPerWrite * 2)
        val dt = 1.0f / SAMPLE_RATE

        // DSP state variables (zero heap allocations)
        var cIdx1 = 0
        var cIdx2 = 0
        var cIdx3 = 0
        var cIdx4 = 0
        var apIdx = 0
        var chWriteIdx = 0
        var chorusLfoPhase = 0.0
        var eqLowL = 0f
        var eqLowR = 0f
        var eqHighStateL = 0f
        var eqHighStateR = 0f

        while (running.get()) {
            val master = masterVolume
            val rightWeight = (0.45f + acmpBalance * 0.9f).coerceIn(0.25f, 1.35f)
            val acmpWeight = (1.35f - acmpBalance * 0.9f).coerceIn(0.25f, 1.35f) * styleVolume * fadeGain
            val bendRatio = 2.0.pow(pitchBendSemitones / 12.0)
            val tuneRef = masterTuneHz.toDouble()
            val modSemis = modulationDepth * 0.45f
            val dspOn = isDspEnabled
            val revMix = if (dspOn) reverbDepth * 0.42f else 0f
            val chMix = if (dspOn) chorusDepth * 0.38f else 0f
            val bassBoost = (eqBass - 0.5f) * 1.1f
            val trebleBoost = (eqTreble - 0.5f) * 0.95f
            val midScale = 0.85f + eqMid * 0.3f

            var blockPeak = 0f

            for (i in 0 until framesPerWrite) {
                var mixL = 0f
                var mixR = 0f

                // 1. Melodic polyphonic voices
                for (v in melodicVoices) {
                    if (!v.active) continue
                    val muted = partMutes[v.part] == true
                    val partGain = (partVolumes[v.part] ?: 0.82f) * v.partGainScale *
                        if (v.part == AccompPart.RIGHT_VOICE) rightWeight else acmpWeight

                    val sample = renderMelodicSample(
                        v = v,
                        dt = dt,
                        bendRatio = if (v.part == AccompPart.RIGHT_VOICE) bendRatio else 1.0,
                        tuneRefHz = tuneRef,
                        extraModSemitones = if (v.part == AccompPart.RIGHT_VOICE) modSemis else 0f
                    )
                    if (!muted) {
                        val scaled = sample * v.velocity * v.preset.defaultGain * partGain * 0.22f
                        val pan = v.panOverride ?: (partPans[v.part] ?: 0f)
                        mixL += scaled * (1f - pan * 0.4f)
                        mixR += scaled * (1f + pan * 0.4f)
                    }
                }

                // 2. Synthesized drums & Ethio percussion
                for (d in drumVoices) {
                    if (!d.active) continue
                    val isMetro = d.sound == DrumSound.METRO_HIGH || d.sound == DrumSound.METRO_LOW
                    val muted = !isMetro && (partMutes[d.part] == true)
                    val gain = if (isMetro) {
                        0.45f
                    } else {
                        (partVolumes[d.part] ?: 0.85f) * acmpWeight * 0.36f
                    }
                    val sample = renderDrumSample(d, dt)
                    if (!muted) {
                        val s = sample * d.velocity * gain
                        mixL += s
                        mixR += s
                    }
                }

                // 3. Stereo Chorus DSP
                if (chMix > 0.005f) {
                    chorusLfoPhase += TWO_PI * 0.85 * dt
                    if (chorusLfoPhase > TWO_PI) chorusLfoPhase -= TWO_PI
                    val delaySamplesL = 320 + (sin(chorusLfoPhase) * 140.0).toInt()
                    val delaySamplesR = 320 + (sin(chorusLfoPhase + PI * 0.5) * 140.0).toInt()
                    chorusBufL[chWriteIdx] = mixL
                    chorusBufR[chWriteIdx] = mixR
                    val readL = (chWriteIdx - delaySamplesL + chorusBufL.size) and (chorusBufL.size - 1)
                    val readR = (chWriteIdx - delaySamplesR + chorusBufR.size) and (chorusBufR.size - 1)
                    mixL += chorusBufL[readL] * chMix
                    mixR += chorusBufR[readR] * chMix
                    chWriteIdx = (chWriteIdx + 1) and (chorusBufL.size - 1)
                }

                // 4. Stereo Schroeder Reverb DSP
                if (revMix > 0.005f) {
                    val monoIn = (mixL + mixR) * 0.5f
                    val fb = 0.74f
                    val o1 = combBuf1[cIdx1]; combBuf1[cIdx1] = monoIn + o1 * fb
                    val o2 = combBuf2[cIdx2]; combBuf2[cIdx2] = monoIn + o2 * fb
                    val o3 = combBuf3[cIdx3]; combBuf3[cIdx3] = monoIn + o3 * fb
                    val o4 = combBuf4[cIdx4]; combBuf4[cIdx4] = monoIn + o4 * fb
                    cIdx1 = (cIdx1 + 1) % combBuf1.size
                    cIdx2 = (cIdx2 + 1) % combBuf2.size
                    cIdx3 = (cIdx3 + 1) % combBuf3.size
                    cIdx4 = (cIdx4 + 1) % combBuf4.size

                    val combSumL = (o1 + o3) * 0.5f
                    val combSumR = (o2 + o4) * 0.5f
                    val apDelayed = allpassBuf[apIdx]
                    val apIn = (combSumL + combSumR) * 0.5f
                    allpassBuf[apIdx] = apIn + apDelayed * 0.5f
                    val apOut = apDelayed - apIn * 0.5f
                    apIdx = (apIdx + 1) % allpassBuf.size

                    mixL += (combSumL * 0.65f + apOut * 0.35f) * revMix
                    mixR += (combSumR * 0.65f - apOut * 0.35f) * revMix
                }

                // 5. 3-Band Master EQ
                if (dspOn) {
                    eqLowL += 0.045f * (mixL - eqLowL)
                    eqLowR += 0.045f * (mixR - eqLowR)
                    val highL = mixL - eqHighStateL
                    val highR = mixR - eqHighStateR
                    eqHighStateL += 0.35f * (mixL - eqHighStateL)
                    eqHighStateR += 0.35f * (mixR - eqHighStateR)

                    mixL = mixL * midScale + eqLowL * bassBoost + highL * trebleBoost
                    mixR = mixR * midScale + eqLowR * bassBoost + highR * trebleBoost
                }

                // 6. Master Gain + Analog Soft-Clip Limiter
                val outL = tanh((mixL * master).toDouble()).toFloat()
                val outR = tanh((mixR * master).toDouble()).toFloat()
                val p = maxOf(abs(outL), abs(outR))
                if (p > blockPeak) blockPeak = p

                pcmBuffer[i * 2] = (outL * 32760f).toInt().coerceIn(-32767, 32767).toShort()
                pcmBuffer[i * 2 + 1] = (outR * 32760f).toInt().coerceIn(-32767, 32767).toShort()
            }

            currentPeakLevel = currentPeakLevel * 0.65f + blockPeak * 0.35f

            if (running.get()) {
                val written = track.write(pcmBuffer, 0, pcmBuffer.size, AudioTrack.WRITE_BLOCKING)
                if (written < 0) break
            }
        }
    }

    private fun renderMelodicSample(
        v: MelodicVoice,
        dt: Float,
        bendRatio: Double,
        tuneRefHz: Double,
        extraModSemitones: Float
    ): Float {
        v.ageSamples++
        val p = v.preset

        when (v.envStage) {
            EnvStage.ATTACK -> {
                val step = dt / p.attackSeconds.coerceAtLeast(0.002f)
                v.envValue += step
                if (v.envValue >= 1f) {
                    v.envValue = 1f
                    v.envStage = EnvStage.DECAY
                }
            }
            EnvStage.DECAY -> {
                val step = dt / p.decaySeconds.coerceAtLeast(0.02f)
                v.envValue -= (1f - p.sustainLevel) * step
                if (v.envValue <= p.sustainLevel) {
                    v.envValue = p.sustainLevel
                    v.envStage = EnvStage.SUSTAIN
                }
            }
            EnvStage.SUSTAIN -> {
                if (p.engineType == SynthEngineType.CONCERT_PIANO ||
                    p.engineType == SynthEngineType.ELECTRIC_PIANO ||
                    p.engineType == SynthEngineType.KRAR_PLUCKED ||
                    p.engineType == SynthEngineType.ACOUSTIC_GUITAR
                ) {
                    v.envValue *= (1f - dt * 0.42f)
                    if (v.envValue < 0.001f) {
                        v.active = false
                        v.envStage = EnvStage.OFF
                        return 0f
                    }
                }
            }
            EnvStage.RELEASE -> {
                val step = dt / p.releaseSeconds.coerceAtLeast(0.015f)
                v.envValue -= step
                if (v.envValue <= 0.0005f) {
                    v.envValue = 0f
                    v.active = false
                    v.envStage = EnvStage.OFF
                    return 0f
                }
            }
            EnvStage.OFF -> {
                v.active = false
                return 0f
            }
        }

        // User-imported WAV Sample playback with linear interpolation pitch-shifting
        if (p.engineType == SynthEngineType.USER_WAV_SAMPLE) {
            val wav = v.sampleRef
            if (wav == null || wav.samples.isEmpty()) {
                v.active = false
                return 0f
            }
            val pitchRatio = 2.0.pow((v.midiNote - p.sampleRootMidi) / 12.0) * bendRatio * (tuneRefHz / 440.0)
            val rateStep = (wav.sampleRate.toDouble() / SAMPLE_RATE) * pitchRatio
            val idx = v.samplePos.toInt()
            if (idx >= wav.samples.size - 1) {
                v.active = false
                v.envStage = EnvStage.OFF
                return 0f
            }
            val frac = (v.samplePos - idx).toFloat()
            val s = wav.samples[idx] * (1f - frac) + wav.samples[idx + 1] * frac
            v.samplePos += rateStep
            return s * v.envValue * 1.25f
        }

        var freq = tuneRefHz * 2.0.pow((v.midiNote - 69) / 12.0) * bendRatio
        val totalVibDepth = p.vibratoDepthSemitones + extraModSemitones
        if (totalVibDepth > 0.005f) {
            val vibRate = if (p.vibratoRateHz > 0f) p.vibratoRateHz else 5.5f
            v.lfoPhase += TWO_PI * vibRate * dt
            if (v.lfoPhase > TWO_PI) v.lfoPhase -= TWO_PI
            val vibFade = (v.ageSamples / (SAMPLE_RATE * 0.16f)).coerceIn(0f, 1f)
            val vibSemi = sin(v.lfoPhase) * totalVibDepth * vibFade
            freq *= 2.0.pow(vibSemi / 12.0)
        }

        val inc = TWO_PI * freq * dt
        v.phase += inc
        if (v.phase > TWO_PI) v.phase -= TWO_PI

        val raw = when (p.engineType) {
            SynthEngineType.CONCERT_PIANO -> {
                val h1 = sin(v.phase)
                val h2 = sin(v.phase * 2.0) * 0.44 * v.envValue
                val h3 = sin(v.phase * 3.0) * 0.22 * v.envValue * p.brightness
                val h4 = sin(v.phase * 4.0) * 0.12 * v.envValue * v.envValue
                ((h1 + h2 + h3 + h4) * 0.65).toFloat()
            }
            SynthEngineType.ELECTRIC_PIANO -> {
                v.modPhase += inc * 3.0
                if (v.modPhase > TWO_PI) v.modPhase -= TWO_PI
                val fmIndex = 2.4 * v.envValue * v.envValue * p.brightness
                val carrier = sin(v.phase + sin(v.modPhase) * fmIndex)
                val body = sin(v.phase) * 0.42
                ((carrier * 0.7 + body) * 0.72).toFloat()
            }
            SynthEngineType.CHURCH_ORGAN -> {
                val p16 = sin(v.phase * 0.5) * 0.35
                val p8 = sin(v.phase) * 0.6
                val p4 = sin(v.phase * 2.0) * 0.42 * p.brightness
                val p223 = sin(v.phase * 3.0) * 0.22 * p.brightness
                val p2 = sin(v.phase * 4.0) * 0.16 * p.brightness
                ((p16 + p8 + p4 + p223 + p2) * 0.52).toFloat()
            }
            SynthEngineType.ACOUSTIC_GUITAR -> {
                val decayBright = v.envValue * v.envValue * p.brightness
                val g1 = sin(v.phase)
                val g2 = sin(v.phase * 2.0) * 0.52 * decayBright
                val g3 = sin(v.phase * 3.0) * 0.30 * decayBright
                val g4 = sin(v.phase * 4.0) * 0.18 * decayBright
                ((g1 + g2 + g3 + g4) * 0.62).toFloat()
            }
            SynthEngineType.MASINKO_BOWED -> {
                val saw = v.phase / PI - 1.0
                val nasal = sin(v.phase * 3.0) * 0.35 + sin(v.phase * 2.0) * 0.2
                ((saw * 0.55 + nasal * 0.45) * 0.78).toFloat()
            }
            SynthEngineType.WASHINT_FLUTE -> {
                val sine = sin(v.phase)
                val overtone = sin(v.phase * 2.0) * 0.16
                val chiffEnv = exp((-v.ageSamples / (SAMPLE_RATE * 0.06f)).toDouble()).toFloat()
                val breath = (((v.ageSamples * 1103515245 + 12345) and 0x7fff) / 16384f - 1f) * (0.04f + chiffEnv * 0.18f)
                (sine * 0.8 + overtone).toFloat() + breath
            }
            SynthEngineType.KRAR_PLUCKED -> {
                val bright = v.envValue * p.brightness
                val h1 = sin(v.phase)
                val h2 = sin(v.phase * 2.0) * 0.55 * bright
                val h3 = sin(v.phase * 3.0) * 0.35 * bright
                val h5 = sin(v.phase * 5.0) * 0.2 * bright * bright
                ((h1 + h2 + h3 + h5) * 0.55).toFloat()
            }
            SynthEngineType.ACCORDION_REED -> {
                v.phase2 += inc * 1.0042
                if (v.phase2 > TWO_PI) v.phase2 -= TWO_PI
                val r1 = sin(v.phase) + sin(v.phase * 2.0) * 0.45 + sin(v.phase * 3.0) * 0.25
                val r2 = sin(v.phase2) + sin(v.phase2 * 2.0) * 0.45 + sin(v.phase2 * 3.0) * 0.25
                ((r1 + r2) * 0.28).toFloat()
            }
            SynthEngineType.BRASS_SECTION -> {
                v.phase2 += inc * 0.997
                if (v.phase2 > TWO_PI) v.phase2 -= TWO_PI
                val s1 = v.phase / PI - 1.0
                val s2 = v.phase2 / PI - 1.0
                ((s1 + s2) * 0.45).toFloat()
            }
            SynthEngineType.SAXOPHONE -> {
                val sq = if (sin(v.phase) >= 0.0) 0.55 else -0.55
                val warm = sin(v.phase) * 0.65 + sin(v.phase * 2.0) * 0.28
                ((sq * 0.35 + warm * 0.65) * 0.72).toFloat()
            }
            SynthEngineType.STRINGS_ENSEMBLE, SynthEngineType.WARM_PAD -> {
                v.phase2 += inc * 1.0055
                if (v.phase2 > TWO_PI) v.phase2 -= TWO_PI
                val s1 = v.phase / PI - 1.0
                val s2 = v.phase2 / PI - 1.0
                val sub = sin(v.phase * 0.5) * 0.32
                ((s1 * 0.34 + s2 * 0.34 + sub) * 0.72).toFloat()
            }
            SynthEngineType.SYNTH_LEAD -> {
                v.phase2 += inc * 1.003
                if (v.phase2 > TWO_PI) v.phase2 -= TWO_PI
                val saw = (v.phase / PI - 1.0) * 0.6
                val pulse = if (v.phase2 < PI * 0.7) 0.45 else -0.45
                (saw + pulse).toFloat() * 0.62f
            }
            SynthEngineType.ELECTRIC_BASS, SynthEngineType.SYNTH_BASS -> {
                val fund = sin(v.phase)
                val h2 = sin(v.phase * 2.0) * 0.48 * v.envValue
                val saw = (v.phase / PI - 1.0) * 0.25 * p.brightness
                ((fund * 0.8 + h2 + saw) * 0.75).toFloat()
            }
            SynthEngineType.USER_WAV_SAMPLE -> 0f
        }

        val cutoff = (0.14f + p.brightness * 0.58f * (0.35f + 0.65f * v.envValue)).coerceIn(0.08f, 0.92f)
        v.lowPassState += cutoff * (raw - v.lowPassState)
        return v.lowPassState * v.envValue
    }

    private fun renderDrumSample(d: DrumVoice, dt: Float): Float {
        d.elapsedSec += dt
        if (d.elapsedSec >= d.durationSec) {
            d.active = false
            return 0f
        }
        val t = d.elapsedSec
        d.noiseSeed = d.noiseSeed * 1664525 + 1013904223
        val noise = ((d.noiseSeed ushr 16) and 0x7FFF) / 16384f - 1f

        return when (d.sound) {
            DrumSound.KICK -> {
                val pitch = 52.0 + 135.0 * exp(-t * 36.0)
                d.phase += TWO_PI * pitch * dt
                sin(d.phase).toFloat() * exp(-t * 14.0).toFloat() * 1.15f
            }
            DrumSound.KEBERO_LOW -> {
                val pitch = 86.0 + 74.0 * exp(-t * 21.0)
                d.phase += TWO_PI * pitch * dt
                val skin = sin(d.phase).toFloat() + sin(d.phase * 1.54).toFloat() * 0.28f
                skin * exp(-t * 9.0).toFloat() * 0.95f
            }
            DrumSound.KEBERO_HIGH -> {
                val pitch = 245.0 + 160.0 * exp(-t * 48.0)
                d.phase += TWO_PI * pitch * dt
                (sin(d.phase).toFloat() * 0.72f + noise * exp(-t * 55.0).toFloat() * 0.45f) * exp(-t * 22.0).toFloat()
            }
            DrumSound.SNARE -> {
                d.phase += TWO_PI * 185.0 * dt
                sin(d.phase).toFloat() * exp(-t * 24.0).toFloat() * 0.55f + noise * exp(-t * 17.0).toFloat() * 0.75f
            }
            DrumSound.CLOSED_HAT -> {
                val hp = noise - d.lastNoise; d.lastNoise = noise
                hp * exp(-t * 62.0).toFloat() * 0.55f
            }
            DrumSound.OPEN_HAT -> {
                val hp = noise - d.lastNoise; d.lastNoise = noise
                hp * exp(-t * 11.0).toFloat() * 0.52f
            }
            DrumSound.CLAP -> {
                val burst = if (t < 0.032f && ((t * 120f).toInt() % 2 == 0)) 1.0f else 0.65f
                noise * exp(-t * 20.0).toFloat() * burst * 0.78f
            }
            DrumSound.CRASH -> {
                val hp = noise - d.lastNoise; d.lastNoise = noise
                d.phase += TWO_PI * 410.0 * dt
                (hp * 0.7f + sin(d.phase).toFloat() * 0.2f) * exp(-t * 4.2).toFloat() * 0.6f
            }
            DrumSound.SHAKER -> {
                val hp = noise - d.lastNoise; d.lastNoise = noise
                val swell = if (t < 0.02f) t / 0.02f else exp(-(t - 0.02f) * 35.0).toFloat()
                hp * swell * 0.4f
            }
            DrumSound.RIM -> {
                d.phase += TWO_PI * 820.0 * dt
                sin(d.phase).toFloat() * exp(-t * 55.0).toFloat() * 0.7f
            }
            DrumSound.METRO_HIGH -> {
                d.phase += TWO_PI * 1480.0 * dt
                sin(d.phase).toFloat() * exp(-t * 65.0).toFloat() * 0.85f
            }
            DrumSound.METRO_LOW -> {
                d.phase += TWO_PI * 980.0 * dt
                sin(d.phase).toFloat() * exp(-t * 65.0).toFloat() * 0.65f
            }
        }
    }

    private fun defaultPartPan(part: AccompPart): Float = when (part) {
        AccompPart.CHORD_1 -> -0.25f
        AccompPart.CHORD_2 -> 0.25f
        AccompPart.PAD -> 0.15f
        AccompPart.PHRASE_1 -> -0.2f
        AccompPart.PHRASE_2 -> 0.2f
        else -> 0f
    }
}
