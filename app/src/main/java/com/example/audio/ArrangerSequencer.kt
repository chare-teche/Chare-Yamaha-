package com.example.audio

import android.os.SystemClock
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerSection
import com.example.domain.model.BuiltInVoices
import com.example.domain.model.ChordLoopStep
import com.example.domain.model.DetectedChord
import com.example.domain.model.DrumSound
import com.example.domain.model.MultiPadPreset
import com.example.domain.model.StyleDefinition
import com.example.domain.model.StyleStepEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * High-precision real-time Arranger Transport & 8-Part Step Sequencer.
 * Supports 4/4, 3/4, 6/8 meters, Intro/Main/Fill/Break/Ending state transitions,
 * Chord Looper playback, Fade In/Out, Metronome, Multi-Pads, and live signal metering.
 */
class ArrangerSequencer(
    private val synthEngine: SynthAudioEngine,
    private val scope: CoroutineScope,
    private val onTransportTick: (
        isPlaying: Boolean,
        measure: Int,
        beat: Int,
        activeSection: ArrangerSection,
        queuedSection: ArrangerSection?,
        fadeActive: Boolean,
        fadeLabel: String,
        activePads: Set<Int>,
        loopedChord: DetectedChord?,
        peakLevel: Float
    ) -> Unit
) {
    private data class ScheduledNoteOff(val offAtGlobalStep: Long, val tag: Long, val part: AccompPart)
    private data class ActivePadPlayback(val preset: MultiPadPreset, var currentStep: Int = 0)

    private var clockJob: Job? = null

    @Volatile private var currentStyle: StyleDefinition = BuiltInStyleBank.defaultStyle()
    @Volatile private var tempoBpm: Int = 120
    @Volatile private var currentChord: DetectedChord = DetectedChord.DEFAULT_C_MAJOR
    @Volatile private var transposeSemitones: Int = 0
    @Volatile private var isMetronomeEnabled: Boolean = false
    @Volatile private var isAcmpEnabled: Boolean = true

    @Volatile private var isPlaying: Boolean = false
    @Volatile private var activeSection: ArrangerSection = ArrangerSection.MAIN_A
    @Volatile private var queuedSection: ArrangerSection? = null
    @Volatile private var targetMainSection: ArrangerSection = ArrangerSection.MAIN_A

    // Chord Looper state
    @Volatile private var chordLooperPlaying: Boolean = false
    @Volatile private var chordLoopSteps: List<ChordLoopStep> = emptyList()

    @Volatile private var fadeDirection: Int = 0
    @Volatile private var fadeStepProgress: Int = 0
    private val fadeDurationSteps = 32

    private val noteOffQueue = mutableListOf<ScheduledNoteOff>()
    private val activePads = ConcurrentHashMap<Int, ActivePadPlayback>()
    private val nextTag = AtomicInteger(20_000)

    private val partVoices = mapOf(
        AccompPart.BASS to BuiltInVoices.byId("bass_fingered"),
        AccompPart.CHORD_1 to BuiltInVoices.byId("guitar_nylon"),
        AccompPart.CHORD_2 to BuiltInVoices.byId("ep_suitcase"),
        AccompPart.PAD to BuiltInVoices.byId("strings_cinema"),
        AccompPart.PHRASE_1 to BuiltInVoices.byId("brass_addis"),
        AccompPart.PHRASE_2 to BuiltInVoices.byId("ethio_krar")
    )

    fun updateStyle(style: StyleDefinition) { currentStyle = style }
    fun updateTempo(bpm: Int) { tempoBpm = bpm.coerceIn(40, 240) }
    fun updateChord(chord: DetectedChord) { currentChord = chord }
    fun updateTranspose(semitones: Int) { transposeSemitones = semitones.coerceIn(-12, 12) }
    fun updateMetronome(enabled: Boolean) { isMetronomeEnabled = enabled }

    fun updateChordLooper(playing: Boolean, steps: List<ChordLoopStep>) {
        chordLooperPlaying = playing
        chordLoopSteps = steps
    }

    fun updateAcmpEnabled(enabled: Boolean) {
        isAcmpEnabled = enabled
        if (!enabled) synthEngine.allAcmpNotesOff()
    }

    fun setPreStartSection(section: ArrangerSection, targetMain: ArrangerSection) {
        activeSection = section
        targetMainSection = targetMain
        queuedSection = null
    }

    fun queueSectionTransition(nextSection: ArrangerSection, newTargetMain: ArrangerSection? = null) {
        if (newTargetMain != null) targetMainSection = newTargetMain
        queuedSection = nextSection
    }

    fun triggerImmediateFillOrBreak(section: ArrangerSection, nextMain: ArrangerSection) {
        targetMainSection = nextMain
        activeSection = section
        queuedSection = nextMain
    }

    fun triggerMultiPad(preset: MultiPadPreset) {
        activePads[preset.slot] = ActivePadPlayback(preset, 0)
        if (!isPlaying) {
            scope.launch(Dispatchers.Default) {
                val stepMs = stepDurationMs().toLong().coerceAtLeast(40L)
                for (step in 0 until preset.lengthSteps) {
                    if (isPlaying || !activePads.containsKey(preset.slot)) break
                    preset.events.filter { it.stepIndex == step }.forEach { fireStepEvent(it, step.toLong()) }
                    delay(stepMs)
                }
                activePads.remove(preset.slot)
                onTransportTick(
                    isPlaying, 1, 1, activeSection, queuedSection,
                    fadeDirection != 0, fadeLabel(), activePads.keys.toSet(), null,
                    synthEngine.currentPeakLevel
                )
            }
        }
    }

    fun stopAllMultiPads() { activePads.clear() }

    fun toggleFade() {
        if (!isPlaying) {
            fadeDirection = 1
            fadeStepProgress = 0
            synthEngine.fadeGain = 0.05f
            start(startWithFade = true)
        } else {
            fadeDirection = -1
            fadeStepProgress = 0
        }
    }

    fun start(startWithFade: Boolean = false) {
        if (isPlaying) return
        if (!startWithFade) {
            fadeDirection = 0
            synthEngine.fadeGain = 1.0f
        }
        isPlaying = true
        clockJob?.cancel()
        clockJob = scope.launch(Dispatchers.Default) { runSequencerLoop() }
    }

    fun stop() {
        isPlaying = false
        fadeDirection = 0
        synthEngine.fadeGain = 1.0f
        clockJob?.cancel()
        clockJob = null
        noteOffQueue.clear()
        activePads.clear()
        synthEngine.allAcmpNotesOff()
        activeSection = targetMainSection
        queuedSection = null
        onTransportTick(false, 1, 1, activeSection, null, false, "", emptySet(), null, 0f)
    }

    private fun fadeLabel(): String = when (fadeDirection) {
        1 -> "FADE IN"
        -1 -> "FADE OUT"
        else -> ""
    }

    private fun stepDurationMs(): Double {
        val style = currentStyle
        return if (style.timeSigDenominator == 8) {
            (60_000.0 / tempoBpm.coerceIn(40, 240)) / 3.0
        } else {
            (60_000.0 / tempoBpm.coerceIn(40, 240)) / 4.0
        }
    }

    private suspend fun runSequencerLoop() {
        var globalStep = 0L
        var sectionStep = 0
        var measure = 1
        var nextTickNanos = SystemClock.elapsedRealtimeNanos()

        while (scope.isActive && isPlaying) {
            val style = currentStyle
            val stepsPerBar = style.stepsPerBar.coerceAtLeast(8)
            val pattern = style.patternFor(activeSection)
            val patternTotalSteps = pattern.lengthBars.coerceAtLeast(1) * stepsPerBar

            val iter = noteOffQueue.iterator()
            while (iter.hasNext()) {
                val n = iter.next()
                if (globalStep >= n.offAtGlobalStep) {
                    synthEngine.noteOff(n.tag, n.part)
                    iter.remove()
                }
            }

            val stepInBar = sectionStep % stepsPerBar
            val stepsPerBeat = (stepsPerBar / style.timeSigNumerator.coerceAtLeast(1)).coerceAtLeast(1)
            val beatIndex = (stepInBar / stepsPerBeat) + 1

            // Apply Chord Looper on bar boundary if active
            var loopedChordUpdate: DetectedChord? = null
            if (stepInBar == 0 && chordLooperPlaying && chordLoopSteps.isNotEmpty()) {
                val loopIdx = (measure - 1) % chordLoopSteps.size
                val c = chordLoopSteps[loopIdx].chord
                currentChord = c
                loopedChordUpdate = c
            }

            if (isMetronomeEnabled && stepInBar % stepsPerBeat == 0) {
                synthEngine.triggerDrum(
                    if (beatIndex == 1) DrumSound.METRO_HIGH else DrumSound.METRO_LOW,
                    velocity = 0.8f
                )
            }

            if (fadeDirection == 1) {
                fadeStepProgress++
                synthEngine.fadeGain = (fadeStepProgress.toFloat() / fadeDurationSteps).coerceIn(0.05f, 1.0f)
                if (fadeStepProgress >= fadeDurationSteps) {
                    fadeDirection = 0
                    synthEngine.fadeGain = 1.0f
                }
            } else if (fadeDirection == -1) {
                fadeStepProgress++
                synthEngine.fadeGain = (1f - fadeStepProgress.toFloat() / fadeDurationSteps).coerceIn(0f, 1f)
                if (fadeStepProgress >= fadeDurationSteps) {
                    stop()
                    return
                }
            }

            val eventsAtStep = pattern.events.filter { it.stepIndex == sectionStep }
            for (ev in eventsAtStep) {
                if (ev.part == AccompPart.DRUMS || ev.part == AccompPart.PERCUSSION || isAcmpEnabled) {
                    fireStepEvent(ev, globalStep)
                }
            }

            val padIter = activePads.entries.iterator()
            while (padIter.hasNext()) {
                val (_, padState) = padIter.next()
                padState.preset.events.filter { it.stepIndex == padState.currentStep }
                    .forEach { fireStepEvent(it, globalStep) }
                padState.currentStep++
                if (padState.currentStep >= padState.preset.lengthSteps) padIter.remove()
            }

            if (stepInBar % stepsPerBeat == 0) {
                onTransportTick(
                    true,
                    measure,
                    beatIndex.coerceIn(1, style.timeSigNumerator),
                    activeSection,
                    queuedSection,
                    fadeDirection != 0,
                    fadeLabel(),
                    activePads.keys.toSet(),
                    loopedChordUpdate,
                    synthEngine.currentPeakLevel
                )
            }

            globalStep++
            sectionStep++

            if (sectionStep >= patternTotalSteps) {
                sectionStep = 0
                measure++

                when {
                    activeSection.isEnding -> {
                        delay(stepDurationMs().toLong().coerceAtLeast(60L))
                        stop()
                        return
                    }
                    activeSection.isIntro || activeSection.isFill || activeSection == ArrangerSection.BREAK -> {
                        activeSection = queuedSection ?: targetMainSection
                        queuedSection = null
                    }
                    queuedSection != null -> {
                        activeSection = queuedSection!!
                        if (activeSection.isMain) targetMainSection = activeSection
                        queuedSection = null
                    }
                }
            }

            val stepNanos = (stepDurationMs() * 1_000_000.0).toLong()
            nextTickNanos += stepNanos
            val waitMs = (nextTickNanos - SystemClock.elapsedRealtimeNanos()) / 1_000_000L
            if (waitMs > 1L) {
                delay(waitMs)
            } else if (waitMs < -80L) {
                nextTickNanos = SystemClock.elapsedRealtimeNanos()
            }
        }
    }

    private fun fireStepEvent(ev: StyleStepEvent, globalStep: Long) {
        if (ev.drumSound != null) {
            synthEngine.triggerDrum(ev.drumSound, ev.velocity, ev.part)
            return
        }
        val voice = partVoices[ev.part] ?: BuiltInVoices.byId("ep_suitcase")
        val midiNote = if (ev.absoluteMidiNote != null) {
            (ev.absoluteMidiNote + currentChord.rootPitchClass + transposeSemitones).coerceIn(24, 108)
        } else {
            val resolved = ChordDetector.resolveChordNote(
                chord = currentChord,
                chordToneIndex = ev.chordToneIndex,
                semitoneOffset = ev.semitoneOffset,
                octaveBase = ev.octaveBase
            )
            (resolved + transposeSemitones).coerceIn(24, 108)
        }

        val tag = nextTag.incrementAndGet().toLong()
        synthEngine.noteOn(midiNote, ev.velocity, voice, ev.part, tag)
        noteOffQueue.add(
            ScheduledNoteOff(
                offAtGlobalStep = globalStep + ev.durationSteps.coerceAtLeast(1),
                tag = tag,
                part = ev.part
            )
        )
    }
}
