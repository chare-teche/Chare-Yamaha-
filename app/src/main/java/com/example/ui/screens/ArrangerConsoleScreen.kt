package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerSection
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.KeyboardLayoutRange
import com.example.domain.model.KeyboardPartTarget
import com.example.domain.model.RegistrationFreezeTarget
import com.example.domain.model.SplitChordMode
import com.example.domain.model.TouchVelocityCurve
import com.example.domain.model.VoicePreset
import com.example.domain.model.WorshipSongUi
import com.example.ui.components.PsrFullReferenceConsole
import com.example.ui.components.PsrFullReferenceGeometry
import com.example.ui.theme.GraphiteVoid

/**
 * ETHIO YAMAHA Master Workstation Screen:
 * Renders the single unified 1280x591 master PSR-SX900 full-design console
 * (`PsrFullReferenceConsole`) combining both upper panel and 29-key piano keyboard in one
 * locked coordinate system, plus the hardware `ExpandedWorkspaceOverlay` modals when a screen
 * destination is active.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun ArrangerConsoleScreen(
    state: ArrangerUiState,
    onOpenScreen: (ArrangerScreen) -> Unit,
    onExitToPerformance: () -> Unit,
    onSetMainMenuPage: (Int) -> Unit,
    onTogglePanelLock: () -> Unit,
    onPianoKeyDown: (Int, Float) -> Unit,
    onPianoKeyUp: (Int) -> Unit,
    onScrollKeyboardToMidi: (Int) -> Unit,
    onShiftKeyboardOctaveView: (Int) -> Unit,
    onPitchBend: (Float) -> Unit,
    onStartStop: () -> Unit,
    onSyncStart: () -> Unit,
    onSyncStop: () -> Unit,
    onAutoFill: () -> Unit,
    onIntro: (ArrangerSection) -> Unit,
    onMainVariation: (ArrangerSection) -> Unit,
    onBreak: () -> Unit,
    onEnding: (ArrangerSection) -> Unit,
    onFadeInOut: () -> Unit,
    onAdjustTempo: (Int) -> Unit,
    onTapTempo: () -> Unit,
    onResetTempo: () -> Unit,
    onAdjustOctave: (Int) -> Unit,
    onAdjustTranspose: (Int) -> Unit,
    onToggleAcmp: () -> Unit,
    onToggleMetronome: () -> Unit,
    onToggleSustain: () -> Unit,
    onToggleSplit: () -> Unit,
    onToggleTempoLock: () -> Unit,
    onTogglePartEnabled: (KeyboardPartTarget) -> Unit,
    onToggleKeyboardPartSolo: (KeyboardPartTarget) -> Unit,
    onToggleHarmony: () -> Unit,
    onToggleDsp: () -> Unit,
    onToggleReverbQuick: () -> Unit,
    onToggleChorusQuick: () -> Unit,
    onSelectOts: (Int) -> Unit,
    onToggleOtsLink: () -> Unit,
    onToggleMemoryStore: () -> Unit,
    onStepRegSequence: (Int) -> Unit,
    onRegistrationPressed: (Int) -> Unit,
    onRegistrationLongPressed: (Int) -> Unit,
    onSelectRegBank: (Int) -> Unit,
    onToggleRegFreeze: () -> Unit,
    onToggleRegFreezeTarget: (RegistrationFreezeTarget) -> Unit,
    onToggleRegSequenceEnabled: () -> Unit,
    onAddRegSequenceStep: (Int, Int) -> Unit,
    onRemoveRegSequenceStepAt: (Int) -> Unit,
    onMoveRegSequenceStep: (Int, Int) -> Unit,
    onClearRegSequence: () -> Unit,
    onJumpToRegSequenceStep: (Int) -> Unit,
    onMultiPadPressed: (Int) -> Unit,
    onStopAllMultiPads: () -> Unit,
    onToggleChordLooperRecord: () -> Unit,
    onToggleChordLooperPlay: () -> Unit,
    onClearChordLoop: () -> Unit,
    onSetMasterVolume: (Float) -> Unit,
    onSetAcmpBalance: (Float) -> Unit,
    onSetStyleVolume: (Float) -> Unit,
    onSetChannelVolume: (AccompPart, Float) -> Unit,
    onSetChannelPan: (AccompPart, Float) -> Unit,
    onToggleChannelMute: (AccompPart) -> Unit,
    onToggleChannelSolo: (AccompPart) -> Unit,
    onClearAccompSolo: () -> Unit,
    onSetReverbDepth: (Float) -> Unit,
    onSetChorusDepth: (Float) -> Unit,
    onSetEqBass: (Float) -> Unit,
    onSetEqMid: (Float) -> Unit,
    onSetEqTreble: (Float) -> Unit,
    onSetMasterTuneHz: (Float) -> Unit,
    onSetAudioBufferFrames: (Int) -> Unit,
    onCycleStyle: (Int) -> Unit,
    onSetStyleSearchQuery: (String) -> Unit,
    onToggleFavoriteStyle: (String) -> Unit,
    onSetVoiceSearchQuery: (String) -> Unit,
    onToggleFavoriteVoice: (String) -> Unit,
    onImportYamahaSetClick: () -> Unit,
    onImportOrgSetClick: () -> Unit,
    onImportSingleFileClick: () -> Unit,
    onSelectStyle: (String) -> Unit,
    onRemoveSource: (Long) -> Unit,
    onRemoveStyle: (String) -> Unit,
    onSelectPartVoice: (KeyboardPartTarget, VoicePreset) -> Unit,
    onSetPartVolume: (KeyboardPartTarget, Float) -> Unit,
    onSetPartPan: (KeyboardPartTarget, Float) -> Unit,
    onSetPartOctave: (KeyboardPartTarget, Int) -> Unit,
    onSetPartNoteRange: (KeyboardPartTarget, Int, Int) -> Unit,
    onSetKeyboardLayout: (KeyboardLayoutRange) -> Unit,
    onSetKeyboardHeightRatio: (Float) -> Unit,
    onSetVisibleWhiteKeys: (Int) -> Unit,
    onSetVelocityCurve: (TouchVelocityCurve) -> Unit,
    onSetChordMode: (SplitChordMode) -> Unit,
    onSetSplitNote: (Int) -> Unit,
    onSetShowKeyLabels: (Boolean) -> Unit,
    onSetShowScrollbar: (Boolean) -> Unit,
    onLoadWorshipSong: (WorshipSongUi) -> Unit,
    onAddWorshipSong: (String, String, String, String) -> Unit,
    onDeleteWorshipSong: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isPortrait = maxHeight > maxWidth
            val unifiedScrollState = rememberScrollState()
            var hasCenteredPortrait by remember { mutableStateOf(false) }

            LaunchedEffect(isPortrait, unifiedScrollState.maxValue) {
                if (isPortrait && !hasCenteredPortrait && unifiedScrollState.maxValue > 0) {
                    unifiedScrollState.scrollTo((unifiedScrollState.maxValue * 0.45f).toInt())
                    hasCenteredPortrait = true
                }
            }

            if (isPortrait) {
                // Portrait: Keep upper panel and lower keyboard locked in one unified container
                // so both scale proportionally and scroll synchronously together.
                val portraitHeight = minOf(maxHeight, maxWidth * 1.45f)
                val portraitWidth = portraitHeight * PsrFullReferenceGeometry.FULL_ASPECT_RATIO

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(unifiedScrollState),
                    contentAlignment = Alignment.CenterStart
                ) {
                    PsrFullReferenceConsole(
                        state = state,
                        onOpenScreen = onOpenScreen,
                        onExitToPerformance = onExitToPerformance,
                        onTogglePanelLock = onTogglePanelLock,
                        onPianoKeyDown = onPianoKeyDown,
                        onPianoKeyUp = onPianoKeyUp,
                        onStartStop = onStartStop,
                        onSyncStart = onSyncStart,
                        onSyncStop = onSyncStop,
                        onAutoFill = onAutoFill,
                        onIntro = onIntro,
                        onMainVariation = onMainVariation,
                        onBreak = onBreak,
                        onEnding = onEnding,
                        onFadeInOut = onFadeInOut,
                        onAdjustTempo = onAdjustTempo,
                        onTapTempo = onTapTempo,
                        onResetTempo = onResetTempo,
                        onAdjustOctave = onAdjustOctave,
                        onAdjustTranspose = onAdjustTranspose,
                        onToggleAcmp = onToggleAcmp,
                        onToggleMetronome = onToggleMetronome,
                        onToggleSustain = onToggleSustain,
                        onToggleSplit = onToggleSplit,
                        onToggleTempoLock = onToggleTempoLock,
                        onTogglePartEnabled = onTogglePartEnabled,
                        onSelectOts = onSelectOts,
                        onToggleOtsLink = onToggleOtsLink,
                        onToggleMemoryStore = onToggleMemoryStore,
                        onStepRegSequence = onStepRegSequence,
                        onRegistrationPressed = onRegistrationPressed,
                        onRegistrationLongPressed = onRegistrationLongPressed,
                        onMultiPadPressed = onMultiPadPressed,
                        onStopAllMultiPads = onStopAllMultiPads,
                        onToggleChannelMute = onToggleChannelMute,
                        onSetMasterVolume = onSetMasterVolume,
                        onSetAcmpBalance = onSetAcmpBalance,
                        onSetStyleVolume = onSetStyleVolume,
                        onCycleStyle = onCycleStyle,
                        onPitchBend = onPitchBend,
                        modifier = Modifier
                            .width(portraitWidth)
                            .height(portraitHeight)
                    )
                }
            } else {
                // Landscape & Tablets: Fit the complete 1280x591 master design proportionally
                val fitWidth = minOf(maxWidth, maxHeight * PsrFullReferenceGeometry.FULL_ASPECT_RATIO)
                val fitHeight = fitWidth / PsrFullReferenceGeometry.FULL_ASPECT_RATIO

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PsrFullReferenceConsole(
                        state = state,
                        onOpenScreen = onOpenScreen,
                        onExitToPerformance = onExitToPerformance,
                        onTogglePanelLock = onTogglePanelLock,
                        onPianoKeyDown = onPianoKeyDown,
                        onPianoKeyUp = onPianoKeyUp,
                        onStartStop = onStartStop,
                        onSyncStart = onSyncStart,
                        onSyncStop = onSyncStop,
                        onAutoFill = onAutoFill,
                        onIntro = onIntro,
                        onMainVariation = onMainVariation,
                        onBreak = onBreak,
                        onEnding = onEnding,
                        onFadeInOut = onFadeInOut,
                        onAdjustTempo = onAdjustTempo,
                        onTapTempo = onTapTempo,
                        onResetTempo = onResetTempo,
                        onAdjustOctave = onAdjustOctave,
                        onAdjustTranspose = onAdjustTranspose,
                        onToggleAcmp = onToggleAcmp,
                        onToggleMetronome = onToggleMetronome,
                        onToggleSustain = onToggleSustain,
                        onToggleSplit = onToggleSplit,
                        onToggleTempoLock = onToggleTempoLock,
                        onTogglePartEnabled = onTogglePartEnabled,
                        onSelectOts = onSelectOts,
                        onToggleOtsLink = onToggleOtsLink,
                        onToggleMemoryStore = onToggleMemoryStore,
                        onStepRegSequence = onStepRegSequence,
                        onRegistrationPressed = onRegistrationPressed,
                        onRegistrationLongPressed = onRegistrationLongPressed,
                        onMultiPadPressed = onMultiPadPressed,
                        onStopAllMultiPads = onStopAllMultiPads,
                        onToggleChannelMute = onToggleChannelMute,
                        onSetMasterVolume = onSetMasterVolume,
                        onSetAcmpBalance = onSetAcmpBalance,
                        onSetStyleVolume = onSetStyleVolume,
                        onCycleStyle = onCycleStyle,
                        onPitchBend = onPitchBend,
                        modifier = Modifier
                            .width(fitWidth)
                            .height(fitHeight)
                    )
                }
            }
        }

        // Expanded Interactive Hardware Workspaces
        AnimatedVisibility(
            visible = state.activeScreen != ArrangerScreen.PERFORMANCE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ExpandedWorkspaceOverlay(
                state = state,
                onDismiss = onExitToPerformance,
                onSwitchScreen = onOpenScreen,
                onSetMainMenuPage = onSetMainMenuPage,
                onTogglePanelLock = onTogglePanelLock,
                onImportYamahaSetClick = onImportYamahaSetClick,
                onImportOrgSetClick = onImportOrgSetClick,
                onImportSingleFileClick = onImportSingleFileClick,
                onSelectStyle = onSelectStyle,
                onRemoveSource = onRemoveSource,
                onRemoveStyle = onRemoveStyle,
                onSetStyleSearchQuery = onSetStyleSearchQuery,
                onToggleFavoriteStyle = onToggleFavoriteStyle,
                onSelectPartVoice = onSelectPartVoice,
                onTogglePartEnabled = onTogglePartEnabled,
                onToggleKeyboardPartSolo = onToggleKeyboardPartSolo,
                onSetPartVolume = onSetPartVolume,
                onSetPartPan = onSetPartPan,
                onSetPartOctave = onSetPartOctave,
                onSetPartNoteRange = onSetPartNoteRange,
                onSetVoiceSearchQuery = onSetVoiceSearchQuery,
                onToggleFavoriteVoice = onToggleFavoriteVoice,
                onToggleHarmony = onToggleHarmony,
                onSetChannelVolume = onSetChannelVolume,
                onSetChannelPan = onSetChannelPan,
                onToggleChannelMute = onToggleChannelMute,
                onToggleChannelSolo = onToggleChannelSolo,
                onClearAccompSolo = onClearAccompSolo,
                onSetMasterVolume = onSetMasterVolume,
                onSetAcmpBalance = onSetAcmpBalance,
                onSetStyleVolume = onSetStyleVolume,
                onToggleDsp = onToggleDsp,
                onSetReverbDepth = onSetReverbDepth,
                onSetChorusDepth = onSetChorusDepth,
                onSetEqBass = onSetEqBass,
                onSetEqMid = onSetEqMid,
                onSetEqTreble = onSetEqTreble,
                onSetMasterTuneHz = onSetMasterTuneHz,
                onSetAudioBufferFrames = onSetAudioBufferFrames,
                onSetKeyboardLayout = onSetKeyboardLayout,
                onSetKeyboardHeightRatio = onSetKeyboardHeightRatio,
                onSetVisibleWhiteKeys = onSetVisibleWhiteKeys,
                onSetVelocityCurve = onSetVelocityCurve,
                onSetChordMode = onSetChordMode,
                onSetSplitNote = onSetSplitNote,
                onSetShowKeyLabels = onSetShowKeyLabels,
                onSetShowScrollbar = onSetShowScrollbar,
                onToggleAcmp = onToggleAcmp,
                onToggleSplit = onToggleSplit,
                onAdjustOctave = onAdjustOctave,
                onAdjustTranspose = onAdjustTranspose,
                onSelectRegBank = onSelectRegBank,
                onRecallRegSlot = onRegistrationPressed,
                onSaveRegSlot = onRegistrationLongPressed,
                onStepRegSequence = onStepRegSequence,
                onToggleRegFreeze = onToggleRegFreeze,
                onToggleRegFreezeTarget = onToggleRegFreezeTarget,
                onToggleRegSequenceEnabled = onToggleRegSequenceEnabled,
                onAddRegSequenceStep = onAddRegSequenceStep,
                onRemoveRegSequenceStepAt = onRemoveRegSequenceStepAt,
                onMoveRegSequenceStep = onMoveRegSequenceStep,
                onClearRegSequence = onClearRegSequence,
                onJumpToRegSequenceStep = onJumpToRegSequenceStep,
                onIntro = onIntro,
                onMainVariation = onMainVariation,
                onBreak = onBreak,
                onEnding = onEnding,
                onStartStop = onStartStop,
                onSyncStart = onSyncStart,
                onSyncStop = onSyncStop,
                onAutoFill = onAutoFill,
                onFadeInOut = onFadeInOut,
                onToggleTempoLock = onToggleTempoLock,
                onToggleOtsLink = onToggleOtsLink,
                onTapTempo = onTapTempo,
                onResetTempo = onResetTempo,
                onLoadWorshipSong = onLoadWorshipSong,
                onAddWorshipSong = onAddWorshipSong,
                onDeleteWorshipSong = onDeleteWorshipSong,
                onTriggerMultiPad = onMultiPadPressed,
                onStopAllMultiPads = onStopAllMultiPads,
                onToggleChordLooperRecord = onToggleChordLooperRecord,
                onToggleChordLooperPlay = onToggleChordLooperPlay,
                onClearChordLoop = onClearChordLoop
            )
        }
    }
}

