package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ArrangerSequencer
import com.example.audio.BuiltInStyleBank
import com.example.audio.ChordDetector
import com.example.audio.MidiInputHandler
import com.example.audio.SynthAudioEngine
import com.example.data.local.AppDatabase
import com.example.data.local.LibraryRepository
import com.example.data.local.RegistrationSlotEntity
import com.example.data.local.WorshipSongEntity
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerSection
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.BuiltInVoices
import com.example.domain.model.ChordLoopStep
import com.example.domain.model.DetectedChord
import com.example.domain.model.KeyboardLayoutRange
import com.example.domain.model.KeyboardNoteRouter
import com.example.domain.model.KeyboardPartConfig
import com.example.domain.model.KeyboardPartTarget
import com.example.domain.model.RegistrationSlotUi
import com.example.domain.model.SplitChordMode
import com.example.domain.model.StyleDefinition
import com.example.domain.model.TouchVelocityCurve
import com.example.domain.model.VoicePreset
import com.example.domain.model.WorshipSongUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

class ArrangerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = LibraryRepository(application, database.appDao())
    private val synthEngine = SynthAudioEngine()

    private val initialStyle = BuiltInStyleBank.defaultStyle()

    private val _uiState = MutableStateFlow(
        ArrangerUiState(
            activeStyle = initialStyle,
            tempoBpm = initialStyle.tempoBpm,
            beatsPerBar = initialStyle.timeSigNumerator,
            multiPads = BuiltInStyleBank.multiPads
        )
    )
    val uiState: StateFlow<ArrangerUiState> = _uiState.asStateFlow()

    private var allRegistrationEntities: List<RegistrationSlotEntity> = emptyList()

    private val sequencer = ArrangerSequencer(
        synthEngine = synthEngine,
        scope = viewModelScope,
        onTransportTick = { playing, measure, beat, section, queued, fadeActive, fadeLbl, activePads, loopedChord, peak ->
            _uiState.update { s ->
                s.copy(
                    isPlaying = playing,
                    measureNumber = measure,
                    currentBeat = beat,
                    activeSection = section,
                    queuedSection = queued,
                    isFadeActive = fadeActive,
                    fadeLabel = fadeLbl,
                    activeMultiPadSlots = activePads,
                    detectedChord = loopedChord ?: s.detectedChord,
                    peakMeterLevel = peak
                )
            }
        }
    )

    private val midiHandler = MidiInputHandler(
        context = application,
        onNoteOn = { note, vel -> onPianoKeyDown(note, vel) },
        onNoteOff = { note -> onPianoKeyUp(note) },
        onSustainChanged = { down -> setSustain(down) },
        onPitchBendChanged = { semi -> onPitchBend(semi) },
        onDevicesChanged = { devices ->
            _uiState.update { it.copy(connectedMidiDevices = devices) }
        }
    )

    private val heldChordZoneNotes = mutableSetOf<Int>()
    private val tapTimestamps = ArrayDeque<Long>()
    private var bannerJob: Job? = null

    init {
        val started = synthEngine.start()
        _uiState.update { it.copy(audioEngineRunning = started) }
        sequencer.updateStyle(initialStyle)
        sequencer.updateTempo(initialStyle.tempoBpm)
        midiHandler.start()

        viewModelScope.launch {
            repository.ensureDefaultWorshipSongsSeeded()
        }
        viewModelScope.launch {
            repository.importedSourcesFlow.collect { sources ->
                _uiState.update { it.copy(importedSources = sources) }
            }
        }
        viewModelScope.launch {
            repository.allLibraryStylesFlow.collect { styles ->
                _uiState.update { it.copy(libraryStyles = styles) }
            }
        }
        viewModelScope.launch {
            repository.worshipSongsFlow.collect { songs ->
                _uiState.update { it.copy(worshipSongs = songs) }
            }
        }
        viewModelScope.launch {
            repository.allRegistrationSlotsFlow.collect { entities ->
                allRegistrationEntities = entities
                refreshVisibleRegistrationBank()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sequencer.stop()
        midiHandler.stop()
        synthEngine.stop()
    }

    private fun refreshVisibleRegistrationBank() {
        val state = _uiState.value
        val bank = state.activeRegistrationBank
        val bySlot = allRegistrationEntities.filter { it.bankNumber == bank }.associateBy { it.slotIndex }
        val slotsUi = (1..8).map { slotNum ->
            val e = bySlot[slotNum]
            if (e != null) {
                RegistrationSlotUi(
                    bankNumber = bank,
                    slotNumber = slotNum,
                    isPopulated = true,
                    label = e.label,
                    styleId = e.styleId,
                    tempoBpm = e.tempoBpm,
                    voiceName = BuiltInVoices.byId(e.right1VoiceId, state.userSampleVoices).name
                )
            } else {
                RegistrationSlotUi(bankNumber = bank, slotNumber = slotNum)
            }
        }
        _uiState.update { it.copy(registrationSlots = slotsUi) }
    }

    // --- Navigation & Panel Lock ---

    fun togglePanelLock() {
        _uiState.update { it.copy(isPanelLocked = !it.isPanelLocked) }
        showBanner(if (_uiState.value.isPanelLocked) "PANEL LOCK ON — live performance protected" else "Panel Unlocked")
    }

    fun openScreen(screen: ArrangerScreen) {
        if (_uiState.value.isPanelLocked && screen != ArrangerScreen.PERFORMANCE) {
            showBanner("Panel is LOCKED — tap PANEL LOCK to unlock")
            return
        }
        _uiState.update { it.copy(activeScreen = screen) }
    }

    fun exitToPerformance() {
        _uiState.update { it.copy(activeScreen = ArrangerScreen.PERFORMANCE) }
    }

    // --- Piano Keyboard, Velocity Curves, Multi-Part Voices & Chord Detection ---

    private fun applyVelocityCurve(rawVelocity: Float, curve: TouchVelocityCurve): Float {
        val v = rawVelocity.coerceIn(0.1f, 1.0f)
        return when (curve) {
            TouchVelocityCurve.SOFT -> v.pow(0.65f)
            TouchVelocityCurve.NORMAL -> v
            TouchVelocityCurve.HARD -> v.pow(1.45f)
            TouchVelocityCurve.FIXED -> 0.92f
        }.coerceIn(0.15f, 1.0f)
    }

    fun onPianoKeyDown(rawMidiNote: Int, rawVelocity: Float = 0.85f) {
        val state = _uiState.value
        val vel = applyVelocityCurve(rawVelocity, state.velocityCurve)

        _uiState.update {
            it.copy(
                pressedMidiNotes = it.pressedMidiNotes + rawMidiNote,
                peakMeterLevel = maxOf(it.peakMeterLevel, vel * 0.85f)
            )
        }

        // 1. Arranger Chord Detection & Sync Start/Stop (preserves exact split/full-keyboard behavior)
        val inChordZone = when (state.chordMode) {
            SplitChordMode.FULL_KEYBOARD -> true
            SplitChordMode.FINGERED, SplitChordMode.SINGLE_FINGER ->
                state.isSplitEnabled && rawMidiNote <= state.splitNoteMidi
        }

        if (inChordZone && state.isAcmpEnabled) {
            heldChordZoneNotes.add(rawMidiNote)
            val newChord = ChordDetector.detect(
                activeNotes = heldChordZoneNotes,
                mode = state.chordMode,
                previousChord = state.detectedChord
            )
            sequencer.updateChord(newChord)
            _uiState.update { s ->
                val updatedSteps = if (s.isChordLooperRecording) {
                    val last = s.chordLoopSteps.lastOrNull()
                    if (last == null || last.chord != newChord) {
                        (s.chordLoopSteps + ChordLoopStep(s.chordLoopSteps.size, newChord)).take(16)
                    } else s.chordLoopSteps
                } else s.chordLoopSteps
                s.copy(detectedChord = newChord, chordLoopSteps = updatedSteps)
            }

            if (state.isSyncStartArmed && !state.isPlaying) {
                _uiState.update { it.copy(isSyncStartArmed = false) }
                sequencer.start()
            } else if (state.isSyncStopArmed && !state.isPlaying) {
                sequencer.start()
            }
        }

        // 2. Four-Part Keyboard Voice Routing (LEFT, RIGHT 1, RIGHT 2, RIGHT 3)
        val routedNotes = KeyboardNoteRouter.routeKeyDown(
            rawMidiNote = rawMidiNote,
            splitNoteMidi = state.splitNoteMidi,
            isSplitEnabled = state.isSplitEnabled,
            globalOctaveShift = state.octaveShift,
            transposeSemitones = state.transposeSemitones,
            parts = state.allKeyboardParts
        )

        var primaryRightRouted: com.example.domain.model.RoutedPartNote? = null
        for (routed in routedNotes) {
            synthEngine.noteOn(
                midiNote = routed.midiNote,
                velocity = vel,
                preset = routed.voice,
                part = routed.audioPart,
                tag = routed.voiceTag,
                gainScale = routed.volume,
                panOverride = routed.pan
            )
            if (routed.part != KeyboardPartTarget.LEFT &&
                (primaryRightRouted == null || routed.part == KeyboardPartTarget.RIGHT_1)
            ) {
                primaryRightRouted = routed
            }
        }

        // 3. Real-time Chord-Aware Harmony voicing (uses distinct tag namespace so it never cuts off R1/R2/R3/LEFT)
        if (state.isHarmonyEnabled && primaryRightRouted != null) {
            val latestChord = _uiState.value.detectedChord
            val harmonyNote = computeHarmonyNote(primaryRightRouted.midiNote, latestChord)
            synthEngine.noteOn(
                midiNote = harmonyNote,
                velocity = vel * 0.72f,
                preset = primaryRightRouted.voice,
                part = AccompPart.RIGHT_VOICE,
                tag = KeyboardNoteRouter.harmonyTagFor(rawMidiNote),
                gainScale = primaryRightRouted.volume * 0.75f,
                panOverride = primaryRightRouted.pan
            )
        }
    }

    private fun computeHarmonyNote(melodyMidi: Int, chord: DetectedChord): Int {
        // Choose closest chord tone 3..5 semitones below melody note
        val chordPcs = chord.quality.intervals.map { (chord.rootPitchClass + it) % 12 }.toSet()
        for (drop in 3..5) {
            val candidate = melodyMidi - drop
            if (candidate >= 36 && (((candidate % 12) + 12) % 12) in chordPcs) {
                return candidate
            }
        }
        return (melodyMidi - 4).coerceAtLeast(36)
    }

    fun onPianoKeyUp(rawMidiNote: Int) {
        val state = _uiState.value
        _uiState.update { it.copy(pressedMidiNotes = it.pressedMidiNotes - rawMidiNote) }

        // Release each keyboard part independently by its dedicated tag so no other part is cut off
        synthEngine.noteOff(KeyboardNoteRouter.voiceTagFor(KeyboardPartTarget.RIGHT_1, rawMidiNote), AccompPart.RIGHT_VOICE)
        synthEngine.noteOff(KeyboardNoteRouter.voiceTagFor(KeyboardPartTarget.RIGHT_2, rawMidiNote), AccompPart.RIGHT_VOICE)
        synthEngine.noteOff(KeyboardNoteRouter.voiceTagFor(KeyboardPartTarget.RIGHT_3, rawMidiNote), AccompPart.RIGHT_VOICE)
        synthEngine.noteOff(KeyboardNoteRouter.voiceTagFor(KeyboardPartTarget.LEFT, rawMidiNote), AccompPart.PAD)
        synthEngine.noteOff(KeyboardNoteRouter.harmonyTagFor(rawMidiNote), AccompPart.RIGHT_VOICE)

        if (heldChordZoneNotes.remove(rawMidiNote)) {
            if (heldChordZoneNotes.isEmpty() && state.isSyncStopArmed && state.isPlaying) {
                sequencer.stop()
            }
        }
    }

    fun onPitchBend(semitones: Float) {
        val clamped = semitones.coerceIn(-2f, 2f)
        synthEngine.pitchBendSemitones = clamped
        _uiState.update { it.copy(pitchBendSemitones = clamped) }
    }

    fun onModulationChange(depth: Float) {
        val clamped = depth.coerceIn(0f, 1f)
        synthEngine.modulationDepth = clamped
        _uiState.update { it.copy(modulationDepth = clamped) }
    }

    // --- Keyboard Layout (49/61/76/88), Scroll, Height, Width, Velocity Curve ---

    fun setKeyboardLayout(layout: KeyboardLayoutRange) {
        _uiState.update { s ->
            val clampedStart = s.keyboardStartMidi.coerceIn(layout.lowestMidi, (layout.highestMidi - 18).coerceAtLeast(layout.lowestMidi))
            s.copy(keyboardLayout = layout, keyboardStartMidi = clampedStart)
        }
    }

    fun scrollKeyboardToMidi(startMidi: Int) {
        val layout = _uiState.value.keyboardLayout
        val maxStart = (layout.highestMidi - 16).coerceAtLeast(layout.lowestMidi)
        _uiState.update { it.copy(keyboardStartMidi = startMidi.coerceIn(layout.lowestMidi, maxStart)) }
    }

    fun shiftVisibleKeyboardOctave(deltaOctaves: Int) {
        scrollKeyboardToMidi(_uiState.value.keyboardStartMidi + deltaOctaves * 12)
    }

    fun setVisibleWhiteKeys(count: Int) {
        _uiState.update { it.copy(visibleWhiteKeyCount = count.coerceIn(10, 22)) }
    }

    fun setKeyboardHeightRatio(ratio: Float) {
        _uiState.update { it.copy(keyboardHeightRatio = ratio.coerceIn(0.36f, 0.54f)) }
    }

    fun setVelocityCurve(curve: TouchVelocityCurve) {
        _uiState.update { it.copy(velocityCurve = curve) }
    }

    fun setShowKeyLabels(show: Boolean) {
        _uiState.update { it.copy(showKeyLabels = show) }
    }

    fun setShowKeyboardScrollbar(show: Boolean) {
        _uiState.update { it.copy(showKeyboardScrollbar = show) }
    }

    // --- Arranger Transport & Style Sections ---

    fun onStartStopPressed() {
        val state = _uiState.value
        if (state.isPlaying) {
            sequencer.stop()
        } else {
            _uiState.update { it.copy(isSyncStartArmed = false) }
            sequencer.start()
        }
    }

    fun onSyncStartPressed() {
        if (_uiState.value.isPanelLocked) return
        _uiState.update { it.copy(isSyncStartArmed = !it.isSyncStartArmed) }
    }

    fun onSyncStopPressed() {
        if (_uiState.value.isPanelLocked) return
        _uiState.update { it.copy(isSyncStopArmed = !it.isSyncStopArmed) }
    }

    fun onAutoFillPressed() {
        if (_uiState.value.isPanelLocked) return
        _uiState.update { it.copy(isAutoFillEnabled = !it.isAutoFillEnabled) }
    }

    fun onIntroPressed(intro: ArrangerSection) {
        if (!intro.isIntro || _uiState.value.isPanelLocked) return
        val state = _uiState.value
        if (!state.isPlaying) {
            sequencer.setPreStartSection(intro, state.targetMainSection)
            _uiState.update { it.copy(activeSection = intro, queuedSection = state.targetMainSection) }
            showBanner("${intro.displayLabel} ready — press START or play a chord")
        } else {
            sequencer.queueSectionTransition(intro, state.targetMainSection)
            _uiState.update { it.copy(queuedSection = intro) }
        }
    }

    fun onMainVariationPressed(main: ArrangerSection) {
        if (!main.isMain || _uiState.value.isPanelLocked) return
        val state = _uiState.value
        val otsIndex = when (main) {
            ArrangerSection.MAIN_A -> 1
            ArrangerSection.MAIN_B -> 2
            ArrangerSection.MAIN_C -> 3
            ArrangerSection.MAIN_D -> 4
            else -> state.activeOtsSlot
        }
        if (state.isOtsLinkEnabled) selectOtsSlot(otsIndex)

        if (!state.isPlaying) {
            sequencer.setPreStartSection(main, main)
            _uiState.update { it.copy(activeSection = main, targetMainSection = main, queuedSection = null) }
            return
        }

        if (state.isAutoFillEnabled) {
            val fill = main.correspondingFill()
            sequencer.triggerImmediateFillOrBreak(fill, main)
            _uiState.update { it.copy(activeSection = fill, targetMainSection = main, queuedSection = main) }
        } else {
            sequencer.queueSectionTransition(main, main)
            _uiState.update { it.copy(targetMainSection = main, queuedSection = main) }
        }
    }

    fun onBreakPressed() {
        if (_uiState.value.isPanelLocked) return
        val state = _uiState.value
        if (!state.isPlaying) {
            showBanner("Start playback to trigger BREAK")
            return
        }
        sequencer.triggerImmediateFillOrBreak(ArrangerSection.BREAK, state.targetMainSection)
        _uiState.update { it.copy(activeSection = ArrangerSection.BREAK, queuedSection = state.targetMainSection) }
    }

    fun onEndingPressed(ending: ArrangerSection) {
        if (!ending.isEnding || _uiState.value.isPanelLocked) return
        val state = _uiState.value
        if (!state.isPlaying) {
            sequencer.setPreStartSection(ending, state.targetMainSection)
            _uiState.update { it.copy(activeSection = ending, queuedSection = null) }
            sequencer.start()
        } else {
            sequencer.queueSectionTransition(ending)
            _uiState.update { it.copy(queuedSection = ending) }
        }
    }

    fun onFadeInOutPressed() {
        if (_uiState.value.isPanelLocked) return
        sequencer.toggleFade()
    }

    // --- Tempo & Tap Tempo ---

    fun adjustTempo(delta: Int) {
        setTempo(_uiState.value.tempoBpm + delta)
    }

    fun setTempo(bpm: Int) {
        val clamped = bpm.coerceIn(40, 240)
        sequencer.updateTempo(clamped)
        _uiState.update { it.copy(tempoBpm = clamped) }
    }

    fun resetTempoToStyleDefault() {
        setTempo(_uiState.value.activeStyle.tempoBpm)
        showBanner("Tempo reset to ${_uiState.value.activeStyle.tempoBpm} BPM")
    }

    fun onTapTempo() {
        val now = SystemClock.elapsedRealtime()
        if (tapTimestamps.isNotEmpty() && now - tapTimestamps.last() > 2200L) tapTimestamps.clear()
        tapTimestamps.addLast(now)
        if (tapTimestamps.size > 5) tapTimestamps.removeFirst()
        if (tapTimestamps.size >= 2) {
            val intervals = (1 until tapTimestamps.size).map { tapTimestamps[it] - tapTimestamps[it - 1] }
            val avgMs = intervals.average()
            if (avgMs > 200.0) setTempo((60_000.0 / avgMs).toInt())
        }
    }

    fun toggleTempoLock() { _uiState.update { it.copy(isTempoLocked = !it.isTempoLocked) } }

    // --- Octave, Transpose, Split, Sustain, Metronome, ACMP, Parts, Harmony ---

    fun adjustOctave(delta: Int) {
        _uiState.update { it.copy(octaveShift = (it.octaveShift + delta).coerceIn(-2, 2)) }
    }

    fun adjustTranspose(delta: Int) {
        val next = (_uiState.value.transposeSemitones + delta).coerceIn(-12, 12)
        sequencer.updateTranspose(next)
        _uiState.update { it.copy(transposeSemitones = next) }
    }

    fun toggleSplit() { _uiState.update { it.copy(isSplitEnabled = !it.isSplitEnabled) } }

    fun toggleAcmp() {
        val next = !_uiState.value.isAcmpEnabled
        sequencer.updateAcmpEnabled(next)
        _uiState.update { it.copy(isAcmpEnabled = next) }
    }

    fun toggleMetronome() {
        val next = !_uiState.value.isMetronomeEnabled
        sequencer.updateMetronome(next)
        _uiState.update { it.copy(isMetronomeEnabled = next) }
    }

    fun toggleSustain() { setSustain(!_uiState.value.isSustainEnabled) }

    fun setSustain(enabled: Boolean) {
        synthEngine.setSustain(enabled)
        _uiState.update { it.copy(isSustainEnabled = enabled) }
    }

    private fun updateKeyboardPart(
        target: KeyboardPartTarget,
        transform: (KeyboardPartConfig) -> KeyboardPartConfig
    ) {
        _uiState.update { s ->
            when (target) {
                KeyboardPartTarget.LEFT -> s.copy(leftPart = transform(s.leftPart))
                KeyboardPartTarget.RIGHT_1 -> s.copy(right1Part = transform(s.right1Part))
                KeyboardPartTarget.RIGHT_2 -> s.copy(right2Part = transform(s.right2Part))
                KeyboardPartTarget.RIGHT_3 -> s.copy(right3Part = transform(s.right3Part))
            }
        }
    }

    fun togglePartEnabled(part: KeyboardPartTarget) {
        val nextEnabled = !_uiState.value.partConfig(part).isEnabled
        if (!nextEnabled) {
            // Release only voices belonging to this part so other active layers keep sounding
            synthEngine.releaseTagRange(part.tagBase, part.tagBase + 999L)
        }
        updateKeyboardPart(part) { it.copy(isEnabled = nextEnabled) }
        val voiceName = _uiState.value.partConfig(part).voice.name
        showBanner(if (nextEnabled) "${part.label} ON: $voiceName" else "${part.label} OFF")
    }

    fun selectPartVoice(part: KeyboardPartTarget, voice: VoicePreset) {
        updateKeyboardPart(part) { it.copy(voice = voice, isEnabled = true) }
        if (_uiState.value.activeScreen == ArrangerScreen.VOICE_SELECT) auditionVoice(voice)
    }

    fun setPartVolume(part: KeyboardPartTarget, volume: Float) {
        updateKeyboardPart(part) { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }

    fun setPartPan(part: KeyboardPartTarget, pan: Float) {
        updateKeyboardPart(part) { it.copy(pan = pan.coerceIn(-1f, 1f)) }
    }

    fun setPartOctave(part: KeyboardPartTarget, octave: Int) {
        updateKeyboardPart(part) { it.copy(octaveShift = octave.coerceIn(-2, 2)) }
    }

    fun setPartNoteRange(part: KeyboardPartTarget, lowMidi: Int, highMidi: Int) {
        val clampedLow = lowMidi.coerceIn(21, 108)
        val clampedHigh = highMidi.coerceIn(21, 108)
        val lo = minOf(clampedLow, clampedHigh)
        val hi = maxOf(clampedLow, clampedHigh)
        updateKeyboardPart(part) { it.copy(lowNoteMidi = lo, highNoteMidi = hi) }
    }

    // Backward-compatible convenience helpers for all 4 parts
    fun toggleRight1Enabled() = togglePartEnabled(KeyboardPartTarget.RIGHT_1)
    fun toggleRight2Enabled() = togglePartEnabled(KeyboardPartTarget.RIGHT_2)
    fun toggleRight3Enabled() = togglePartEnabled(KeyboardPartTarget.RIGHT_3)
    fun toggleLeftVoiceEnabled() = togglePartEnabled(KeyboardPartTarget.LEFT)

    fun toggleHarmonyEnabled() {
        _uiState.update { it.copy(isHarmonyEnabled = !it.isHarmonyEnabled) }
        showBanner(if (_uiState.value.isHarmonyEnabled) "Chord Harmony ON" else "Chord Harmony OFF")
    }

    fun setRight1Volume(vol: Float) = setPartVolume(KeyboardPartTarget.RIGHT_1, vol)
    fun setRight2Volume(vol: Float) = setPartVolume(KeyboardPartTarget.RIGHT_2, vol)
    fun setRight3Volume(vol: Float) = setPartVolume(KeyboardPartTarget.RIGHT_3, vol)
    fun setLeftVoiceVolume(vol: Float) = setPartVolume(KeyboardPartTarget.LEFT, vol)

    fun setChordMode(mode: SplitChordMode) { _uiState.update { it.copy(chordMode = mode) } }
    fun setSplitNote(midiNote: Int) { _uiState.update { it.copy(splitNoteMidi = midiNote.coerceIn(48, 72)) } }

    // --- Voices & OTS 1-4 ---

    fun selectRight1Voice(voice: VoicePreset) = selectPartVoice(KeyboardPartTarget.RIGHT_1, voice)
    fun selectRight2Voice(voice: VoicePreset) = selectPartVoice(KeyboardPartTarget.RIGHT_2, voice)
    fun selectRight3Voice(voice: VoicePreset) = selectPartVoice(KeyboardPartTarget.RIGHT_3, voice)
    fun selectLeftVoice(voice: VoicePreset) = selectPartVoice(KeyboardPartTarget.LEFT, voice)

    private fun auditionVoice(voice: VoicePreset) {
        viewModelScope.launch {
            val tag = 999_999L
            synthEngine.noteOn(64, 0.8f, voice, AccompPart.RIGHT_VOICE, tag)
            delay(280)
            synthEngine.noteOff(tag, AccompPart.RIGHT_VOICE)
        }
    }

    fun selectOtsSlot(slot: Int) {
        val idx = slot.coerceIn(1, 4)
        val otsIds = _uiState.value.activeStyle.otsVoiceIds
        val voiceId = otsIds.getOrNull(idx - 1) ?: BuiltInVoices.all.first().id
        val preset = BuiltInVoices.byId(voiceId, _uiState.value.userSampleVoices)
        _uiState.update {
            it.copy(
                activeOtsSlot = idx,
                right1Part = it.right1Part.copy(voice = preset, isEnabled = true)
            )
        }
        showBanner("OTS $idx -> ${preset.name}")
    }

    fun toggleOtsLink() { _uiState.update { it.copy(isOtsLinkEnabled = !it.isOtsLinkEnabled) } }

    // --- Multi-Pads & Chord Looper ---

    fun onMultiPadPressed(slot: Int) {
        val preset = _uiState.value.multiPads.find { it.slot == slot } ?: return
        sequencer.triggerMultiPad(preset)
        _uiState.update { it.copy(activeMultiPadSlots = it.activeMultiPadSlots + slot) }
    }

    fun stopAllMultiPads() {
        sequencer.stopAllMultiPads()
        _uiState.update { it.copy(activeMultiPadSlots = emptySet()) }
    }

    fun toggleChordLooperRecord() {
        _uiState.update { s ->
            val nextRec = !s.isChordLooperRecording
            if (nextRec) {
                sequencer.updateChordLooper(false, emptyList())
                s.copy(isChordLooperRecording = true, isChordLooperPlaying = false, chordLoopSteps = emptyList())
            } else {
                s.copy(isChordLooperRecording = false)
            }
        }
    }

    fun toggleChordLooperPlay() {
        val s = _uiState.value
        if (s.chordLoopSteps.isEmpty()) {
            showBanner("Record a chord progression first in Chord Looper")
            return
        }
        val nextPlay = !s.isChordLooperPlaying
        sequencer.updateChordLooper(nextPlay, s.chordLoopSteps)
        _uiState.update { it.copy(isChordLooperPlaying = nextPlay, isChordLooperRecording = false) }
    }

    fun clearChordLoop() {
        sequencer.updateChordLooper(false, emptyList())
        _uiState.update { it.copy(isChordLooperRecording = false, isChordLooperPlaying = false, chordLoopSteps = emptyList()) }
    }

    // --- Registration Banks 1-4 × Slots 1-8 + Sequence + Freeze ---

    fun selectRegistrationBank(bankNumber: Int) {
        val clamped = bankNumber.coerceIn(1, 4)
        _uiState.update { it.copy(activeRegistrationBank = clamped) }
        refreshVisibleRegistrationBank()
    }

    fun toggleRegistrationFreeze() {
        _uiState.update { it.copy(isRegistrationFreezeEnabled = !it.isRegistrationFreezeEnabled) }
        showBanner(if (_uiState.value.isRegistrationFreezeEnabled) "Registration FREEZE ON (Tempo/Transpose locked)" else "Registration FREEZE OFF")
    }

    fun stepRegistrationSequence(delta: Int) {
        val current = _uiState.value.activeRegistrationSlot ?: 1
        val next = ((current - 1 + delta + 8) % 8) + 1
        recallRegistrationSlot(next)
    }

    fun toggleMemoryStoreMode() {
        if (_uiState.value.isPanelLocked) return
        _uiState.update { it.copy(isMemoryStoreArmed = !it.isMemoryStoreArmed) }
        if (_uiState.value.isMemoryStoreArmed) {
            showBanner("MEMORY armed — tap Registration 1–8 to save Bank ${_uiState.value.activeRegistrationBank}")
        }
    }

    fun onRegistrationSlotPressed(slotNumber: Int) {
        if (_uiState.value.isPanelLocked) return
        if (_uiState.value.isMemoryStoreArmed) saveRegistrationSlot(slotNumber)
        else recallRegistrationSlot(slotNumber)
    }

    fun saveRegistrationSlot(slotNumber: Int) {
        val state = _uiState.value
        val bank = state.activeRegistrationBank
        val r1 = state.right1Part
        val r2 = state.right2Part
        val r3 = state.right3Part
        val left = state.leftPart
        val entity = RegistrationSlotEntity(
            slotKey = bank * 10 + slotNumber,
            bankNumber = bank,
            slotIndex = slotNumber,
            label = "${state.activeStyle.name.take(12)} • ${r1.voice.name.take(10)}",
            styleId = state.activeStyle.id,
            sectionName = state.targetMainSection.name,
            tempoBpm = state.tempoBpm,
            right1VoiceId = r1.voice.id,
            right2VoiceId = r2.voice.id,
            isRight2Enabled = r2.isEnabled,
            leftVoiceId = left.voice.id,
            splitNoteMidi = state.splitNoteMidi,
            transposeSemitones = state.transposeSemitones,
            octaveShift = state.octaveShift,
            activeOtsSlot = state.activeOtsSlot,
            acmpEnabled = state.isAcmpEnabled,
            splitEnabled = state.isSplitEnabled,
            isRight1Enabled = r1.isEnabled,
            isLeftEnabled = left.isEnabled,
            right3VoiceId = r3.voice.id,
            isRight3Enabled = r3.isEnabled,
            right1Volume = r1.volume,
            right2Volume = r2.volume,
            right3Volume = r3.volume,
            leftVolume = left.volume,
            right1Pan = r1.pan,
            right2Pan = r2.pan,
            right3Pan = r3.pan,
            leftPan = left.pan,
            right1Octave = r1.octaveShift,
            right2Octave = r2.octaveShift,
            right3Octave = r3.octaveShift,
            leftOctave = left.octaveShift,
            right1LowMidi = r1.lowNoteMidi,
            right1HighMidi = r1.highNoteMidi,
            right2LowMidi = r2.lowNoteMidi,
            right2HighMidi = r2.highNoteMidi,
            right3LowMidi = r3.lowNoteMidi,
            right3HighMidi = r3.highNoteMidi,
            leftLowMidi = left.lowNoteMidi,
            leftHighMidi = left.highNoteMidi
        )
        viewModelScope.launch {
            repository.saveRegistrationSlot(entity)
            _uiState.update { it.copy(isMemoryStoreArmed = false, activeRegistrationSlot = slotNumber) }
            showBanner("Saved Bank $bank • Regist $slotNumber (4-Part)")
        }
    }

    private fun recallRegistrationSlot(slotNumber: Int) {
        val bank = _uiState.value.activeRegistrationBank
        viewModelScope.launch {
            val saved = repository.getRegistrationSlot(bank, slotNumber)
            if (saved == null) {
                showBanner("Bank $bank Slot $slotNumber empty — press MEMORY + $slotNumber (or long press) to save")
                return@launch
            }
            val state = _uiState.value
            val (loadedStyle, err) = repository.loadStyleDefinition(saved.styleId)
            val styleToUse = loadedStyle ?: BuiltInStyleBank.defaultStyle()
            val section = runCatching { ArrangerSection.valueOf(saved.sectionName) }.getOrDefault(ArrangerSection.MAIN_A)

            val freeze = state.isRegistrationFreezeEnabled
            val nextTempo = if (freeze && state.freezeTempo) state.tempoBpm else saved.tempoBpm
            val nextTranspose = if (freeze && state.freezeTranspose) state.transposeSemitones else saved.transposeSemitones

            applyStyle(styleToUse, overrideTempo = nextTempo)
            sequencer.updateTranspose(nextTranspose)
            sequencer.updateAcmpEnabled(saved.acmpEnabled)

            _uiState.update { s ->
                s.copy(
                    activeRegistrationSlot = slotNumber,
                    right1Part = KeyboardPartConfig(
                        part = KeyboardPartTarget.RIGHT_1,
                        voice = BuiltInVoices.byId(saved.right1VoiceId, s.userSampleVoices),
                        isEnabled = saved.isRight1Enabled,
                        volume = saved.right1Volume.coerceIn(0f, 1f),
                        pan = saved.right1Pan.coerceIn(-1f, 1f),
                        octaveShift = saved.right1Octave.coerceIn(-2, 2),
                        lowNoteMidi = saved.right1LowMidi.coerceIn(21, 108),
                        highNoteMidi = saved.right1HighMidi.coerceIn(21, 108)
                    ),
                    right2Part = KeyboardPartConfig(
                        part = KeyboardPartTarget.RIGHT_2,
                        voice = BuiltInVoices.byId(saved.right2VoiceId, s.userSampleVoices),
                        isEnabled = saved.isRight2Enabled,
                        volume = saved.right2Volume.coerceIn(0f, 1f),
                        pan = saved.right2Pan.coerceIn(-1f, 1f),
                        octaveShift = saved.right2Octave.coerceIn(-2, 2),
                        lowNoteMidi = saved.right2LowMidi.coerceIn(21, 108),
                        highNoteMidi = saved.right2HighMidi.coerceIn(21, 108)
                    ),
                    right3Part = KeyboardPartConfig(
                        part = KeyboardPartTarget.RIGHT_3,
                        voice = BuiltInVoices.byId(saved.right3VoiceId, s.userSampleVoices),
                        isEnabled = saved.isRight3Enabled,
                        volume = saved.right3Volume.coerceIn(0f, 1f),
                        pan = saved.right3Pan.coerceIn(-1f, 1f),
                        octaveShift = saved.right3Octave.coerceIn(-2, 2),
                        lowNoteMidi = saved.right3LowMidi.coerceIn(21, 108),
                        highNoteMidi = saved.right3HighMidi.coerceIn(21, 108)
                    ),
                    leftPart = KeyboardPartConfig(
                        part = KeyboardPartTarget.LEFT,
                        voice = BuiltInVoices.byId(saved.leftVoiceId, s.userSampleVoices),
                        isEnabled = saved.isLeftEnabled,
                        volume = saved.leftVolume.coerceIn(0f, 1f),
                        pan = saved.leftPan.coerceIn(-1f, 1f),
                        octaveShift = saved.leftOctave.coerceIn(-2, 2),
                        lowNoteMidi = saved.leftLowMidi.coerceIn(21, 108),
                        highNoteMidi = saved.leftHighMidi.coerceIn(21, 108)
                    ),
                    splitNoteMidi = saved.splitNoteMidi,
                    transposeSemitones = nextTranspose,
                    octaveShift = saved.octaveShift,
                    activeOtsSlot = saved.activeOtsSlot,
                    isAcmpEnabled = saved.acmpEnabled,
                    isSplitEnabled = saved.splitEnabled,
                    targetMainSection = section,
                    activeSection = if (s.isPlaying) s.activeSection else section
                )
            }
            showBanner(err ?: "Loaded Bank $bank Regist $slotNumber: ${styleToUse.name}")
        }
    }

    // --- Church Worship Playlist ---

    fun loadWorshipSong(song: WorshipSongUi) {
        viewModelScope.launch {
            val (styleDef, _) = repository.loadStyleDefinition(song.styleId)
            val resolvedStyle = styleDef ?: BuiltInStyleBank.defaultStyle()
            applyStyle(resolvedStyle, overrideTempo = song.tempoBpm)
            sequencer.updateTranspose(song.transposeSemitones)
            val voice = BuiltInVoices.byId(song.voiceId, _uiState.value.userSampleVoices)
            _uiState.update {
                it.copy(
                    right1Part = it.right1Part.copy(voice = voice, isEnabled = true),
                    transposeSemitones = song.transposeSemitones,
                    activeScreen = ArrangerScreen.PERFORMANCE
                )
            }
            showBanner("Worship Song Loaded: ${song.title} (${song.keySignature}, ${song.tempoBpm} BPM)")
        }
    }

    fun saveCurrentAsWorshipSong(title: String, category: String, keySig: String, notes: String) {
        if (title.isBlank()) return
        val s = _uiState.value
        viewModelScope.launch {
            repository.addWorshipSong(
                WorshipSongEntity(
                    title = title.trim(),
                    category = category.ifBlank { "Church Worship" },
                    keySignature = keySig.ifBlank { s.detectedChord.displayName },
                    styleId = s.activeStyle.id,
                    voiceId = s.right1Voice.id,
                    tempoBpm = s.tempoBpm,
                    transposeSemitones = s.transposeSemitones,
                    notes = notes.trim()
                )
            )
            showBanner("Added '$title' to Worship Setlist")
        }
    }

    fun deleteWorshipSong(songId: Long) {
        viewModelScope.launch {
            repository.deleteWorshipSong(songId)
            showBanner("Removed song from Worship Setlist")
        }
    }

    // --- Mixer, DSP Reverb/Chorus, 3-Band EQ & Audio Latency ---

    fun setMasterVolume(volume: Float) {
        val c = volume.coerceIn(0f, 1f)
        synthEngine.masterVolume = c
        _uiState.update { it.copy(masterVolume = c) }
    }

    fun setAcmpBalance(balance: Float) {
        val c = balance.coerceIn(0f, 1f)
        synthEngine.acmpBalance = c
        _uiState.update { it.copy(acmpBalance = c) }
    }

    fun setStyleVolume(vol: Float) {
        val c = vol.coerceIn(0f, 1f)
        synthEngine.styleVolume = c
        _uiState.update { it.copy(styleVolume = c) }
    }

    fun toggleDsp() {
        val next = !_uiState.value.isDspEnabled
        synthEngine.isDspEnabled = next
        _uiState.update { it.copy(isDspEnabled = next) }
    }

    fun toggleReverbQuick() {
        val next = if (_uiState.value.reverbDepth > 0.05f) 0f else 0.35f
        setReverbDepth(next)
    }

    fun toggleChorusQuick() {
        val next = if (_uiState.value.chorusDepth > 0.05f) 0f else 0.28f
        setChorusDepth(next)
    }

    fun setReverbDepth(depth: Float) {
        val c = depth.coerceIn(0f, 1f)
        synthEngine.reverbDepth = c
        _uiState.update { it.copy(reverbDepth = c) }
    }

    fun setChorusDepth(depth: Float) {
        val c = depth.coerceIn(0f, 1f)
        synthEngine.chorusDepth = c
        _uiState.update { it.copy(chorusDepth = c) }
    }

    fun setEqBass(v: Float) {
        val c = v.coerceIn(0f, 1f); synthEngine.eqBass = c; _uiState.update { it.copy(eqBass = c) }
    }

    fun setEqMid(v: Float) {
        val c = v.coerceIn(0f, 1f); synthEngine.eqMid = c; _uiState.update { it.copy(eqMid = c) }
    }

    fun setEqTreble(v: Float) {
        val c = v.coerceIn(0f, 1f); synthEngine.eqTreble = c; _uiState.update { it.copy(eqTreble = c) }
    }

    fun setMasterTuneHz(hz: Float) {
        val c = hz.coerceIn(415f, 466f)
        synthEngine.masterTuneHz = c
        _uiState.update { it.copy(masterTuneHz = c) }
    }

    fun setAudioBufferFrames(frames: Int) {
        val ok = synthEngine.restartWithBufferFrames(frames)
        _uiState.update { it.copy(audioBufferFrames = frames, audioEngineRunning = ok) }
        showBanner("Audio buffer set to $frames frames (~${(frames * 1000) / SynthAudioEngine.SAMPLE_RATE} ms)")
    }

    fun setChannelVolume(part: AccompPart, volume: Float) {
        val c = volume.coerceIn(0f, 1f)
        synthEngine.setPartVolume(part, c)
        _uiState.update { s ->
            s.copy(mixerChannels = s.mixerChannels.map { if (it.part == part) it.copy(volume = c) else it })
        }
    }

    fun setChannelPan(part: AccompPart, pan: Float) {
        val c = pan.coerceIn(-1f, 1f)
        synthEngine.setPartPan(part, c)
        _uiState.update { s ->
            s.copy(mixerChannels = s.mixerChannels.map { if (it.part == part) it.copy(pan = c) else it })
        }
    }

    fun toggleChannelMute(part: AccompPart) {
        _uiState.update { s ->
            s.copy(
                mixerChannels = s.mixerChannels.map { ch ->
                    if (ch.part == part) {
                        val muted = !ch.isMuted
                        synthEngine.setPartMuted(part, muted)
                        ch.copy(isMuted = muted)
                    } else ch
                }
            )
        }
    }

    // --- Separate Yamaha SET & ORG.SET Library Importers ---

    fun importYamahaSetFolder(treeUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val outcome = repository.importFolderTree(treeUri, isOrgSet = false)
            registerLoadedSamples(outcome.loadedSamples)
            _uiState.update { it.copy(isImporting = false) }
            showBanner(outcome.statusMessage)
        }
    }

    fun importOrgSetFolder(treeUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val outcome = repository.importFolderTree(treeUri, isOrgSet = true)
            registerLoadedSamples(outcome.loadedSamples)
            _uiState.update { it.copy(isImporting = false) }
            showBanner(outcome.statusMessage)
        }
    }

    fun importSingleStyleOrSample(fileUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val outcome = repository.importSingleFile(fileUri)
            registerLoadedSamples(outcome.loadedSamples)
            _uiState.update { it.copy(isImporting = false) }
            showBanner(outcome.statusMessage)
        }
    }

    private fun registerLoadedSamples(samples: List<Pair<com.example.audio.LoadedWavSample, VoicePreset>>) {
        if (samples.isEmpty()) return
        samples.forEach { (wav, _) -> synthEngine.registerWavSample(wav) }
        val newVoices = samples.map { it.second }
        _uiState.update { s ->
            val merged = (s.userSampleVoices + newVoices).distinctBy { it.id }
            s.copy(userSampleVoices = merged)
        }
    }

    fun selectStyleFromLibrary(styleId: String) {
        viewModelScope.launch {
            val (styleDef, error) = repository.loadStyleDefinition(styleId)
            if (styleDef != null) {
                applyStyle(styleDef)
                _uiState.update { it.copy(activeScreen = ArrangerScreen.PERFORMANCE) }
                showBanner("Loaded Style: ${styleDef.name} (${styleDef.tempoBpm} BPM)")
            } else {
                showBanner(error ?: "Unable to load selected style")
            }
        }
    }

    fun removeImportedSource(sourceId: Long) {
        viewModelScope.launch {
            repository.removeImportedSource(sourceId)
            showBanner("Removed imported SET source")
        }
    }

    fun removeImportedStyle(styleId: String) {
        viewModelScope.launch {
            repository.removeStyleEntry(styleId)
            showBanner("Removed item from library")
        }
    }

    fun cycleStyle(direction: Int) {
        val playable = _uiState.value.libraryStyles.filter { it.compatibilityStatus == "PLAYABLE" }
        if (playable.isEmpty()) return
        val currentIdx = playable.indexOfFirst { it.id == _uiState.value.activeStyle.id }
        val nextIdx = ((if (currentIdx < 0) 0 else currentIdx) + direction + playable.size) % playable.size
        selectStyleFromLibrary(playable[nextIdx].id)
    }

    private fun applyStyle(style: StyleDefinition, overrideTempo: Int? = null) {
        val state = _uiState.value
        val newTempo = overrideTempo ?: if (state.isTempoLocked) state.tempoBpm else style.tempoBpm
        sequencer.updateStyle(style)
        sequencer.updateTempo(newTempo)
        _uiState.update { s ->
            s.copy(
                activeStyle = style,
                tempoBpm = newTempo,
                beatsPerBar = style.timeSigNumerator,
                activeOtsSlot = 1
            )
        }
    }

    private fun showBanner(message: String) {
        bannerJob?.cancel()
        _uiState.update { it.copy(statusBanner = message) }
        bannerJob = viewModelScope.launch {
            delay(3800)
            _uiState.update { it.copy(statusBanner = null) }
        }
    }
}
