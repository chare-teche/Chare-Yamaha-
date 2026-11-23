package com.example

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ArrangerConsoleScreen
import com.example.ui.theme.EthioYamahaTheme
import com.example.viewmodel.ArrangerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        configureCutoutAndImmersiveFullscreen()

        setContent {
            EthioYamahaTheme {
                val vm: ArrangerViewModel = viewModel()
                val uiState by vm.uiState.collectAsStateWithLifecycle()

                // Dedicated SAF Folder Picker for Yamaha SET folders
                val yamahaSetLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { treeUri ->
                    if (treeUri != null) vm.importYamahaSetFolder(treeUri)
                }

                // Dedicated SAF Folder Picker for ORG.SET folders
                val orgSetLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { treeUri ->
                    if (treeUri != null) vm.importOrgSetFolder(treeUri)
                }

                // SAF Single File Picker for .sty, .prs, .sst, .mid, .wav samples
                val singleFileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { fileUri ->
                    if (fileUri != null) vm.importSingleStyleOrSample(fileUri)
                }

                ArrangerConsoleScreen(
                    state = uiState,
                    onOpenScreen = vm::openScreen,
                    onExitToPerformance = vm::exitToPerformance,
                    onSetMainMenuPage = vm::setMainMenuPage,
                    onTogglePanelLock = vm::togglePanelLock,
                    onPianoKeyDown = vm::onPianoKeyDown,
                    onPianoKeyUp = vm::onPianoKeyUp,
                    onScrollKeyboardToMidi = vm::scrollKeyboardToMidi,
                    onShiftKeyboardOctaveView = vm::shiftVisibleKeyboardOctave,
                    onPitchBend = vm::onPitchBend,
                    onStartStop = vm::onStartStopPressed,
                    onSyncStart = vm::onSyncStartPressed,
                    onSyncStop = vm::onSyncStopPressed,
                    onAutoFill = vm::onAutoFillPressed,
                    onIntro = vm::onIntroPressed,
                    onMainVariation = vm::onMainVariationPressed,
                    onBreak = vm::onBreakPressed,
                    onEnding = vm::onEndingPressed,
                    onFadeInOut = vm::onFadeInOutPressed,
                    onAdjustTempo = vm::adjustTempo,
                    onTapTempo = vm::onTapTempo,
                    onResetTempo = vm::resetTempoToStyleDefault,
                    onAdjustOctave = vm::adjustOctave,
                    onAdjustTranspose = vm::adjustTranspose,
                    onToggleAcmp = vm::toggleAcmp,
                    onToggleMetronome = vm::toggleMetronome,
                    onToggleSustain = vm::toggleSustain,
                    onToggleSplit = vm::toggleSplit,
                    onToggleTempoLock = vm::toggleTempoLock,
                    onTogglePartEnabled = vm::togglePartEnabled,
                    onToggleKeyboardPartSolo = vm::toggleKeyboardPartSolo,
                    onToggleHarmony = vm::toggleHarmonyEnabled,
                    onToggleDsp = vm::toggleDsp,
                    onToggleReverbQuick = vm::toggleReverbQuick,
                    onToggleChorusQuick = vm::toggleChorusQuick,
                    onSelectOts = vm::selectOtsSlot,
                    onToggleOtsLink = vm::toggleOtsLink,
                    onToggleMemoryStore = vm::toggleMemoryStoreMode,
                    onStepRegSequence = vm::stepRegistrationSequence,
                    onRegistrationPressed = vm::onRegistrationSlotPressed,
                    onRegistrationLongPressed = vm::saveRegistrationSlot,
                    onSelectRegBank = vm::selectRegistrationBank,
                    onToggleRegFreeze = vm::toggleRegistrationFreeze,
                    onToggleRegFreezeTarget = vm::toggleRegistrationFreezeTarget,
                    onToggleRegSequenceEnabled = vm::toggleRegistrationSequenceEnabled,
                    onAddRegSequenceStep = vm::addRegistrationSequenceStep,
                    onRemoveRegSequenceStepAt = vm::removeRegistrationSequenceStepAt,
                    onMoveRegSequenceStep = vm::moveRegistrationSequenceStep,
                    onClearRegSequence = vm::clearRegistrationSequence,
                    onJumpToRegSequenceStep = vm::jumpToRegistrationSequenceStep,
                    onMultiPadPressed = vm::onMultiPadPressed,
                    onStopAllMultiPads = vm::stopAllMultiPads,
                    onToggleChordLooperRecord = vm::toggleChordLooperRecord,
                    onToggleChordLooperPlay = vm::toggleChordLooperPlay,
                    onClearChordLoop = vm::clearChordLoop,
                    onSetMasterVolume = vm::setMasterVolume,
                    onSetAcmpBalance = vm::setAcmpBalance,
                    onSetStyleVolume = vm::setStyleVolume,
                    onSetChannelVolume = vm::setChannelVolume,
                    onSetChannelPan = vm::setChannelPan,
                    onToggleChannelMute = vm::toggleChannelMute,
                    onToggleChannelSolo = vm::toggleChannelSolo,
                    onClearAccompSolo = vm::clearAccompSolo,
                    onSetReverbDepth = vm::setReverbDepth,
                    onSetChorusDepth = vm::setChorusDepth,
                    onSetEqBass = vm::setEqBass,
                    onSetEqMid = vm::setEqMid,
                    onSetEqTreble = vm::setEqTreble,
                    onSetMasterTuneHz = vm::setMasterTuneHz,
                    onSetAudioBufferFrames = vm::setAudioBufferFrames,
                    onCycleStyle = vm::cycleStyle,
                    onSetStyleSearchQuery = vm::setStyleSearchQuery,
                    onToggleFavoriteStyle = vm::toggleFavoriteStyle,
                    onSetVoiceSearchQuery = vm::setVoiceSearchQuery,
                    onToggleFavoriteVoice = vm::toggleFavoriteVoice,
                    onImportYamahaSetClick = { yamahaSetLauncher.launch(null) },
                    onImportOrgSetClick = { orgSetLauncher.launch(null) },
                    onImportSingleFileClick = { singleFileLauncher.launch(arrayOf("*/*")) },
                    onSelectStyle = vm::selectStyleFromLibrary,
                    onRemoveSource = vm::removeImportedSource,
                    onRemoveStyle = vm::removeImportedStyle,
                    onSelectPartVoice = vm::selectPartVoice,
                    onSetPartVolume = vm::setPartVolume,
                    onSetPartPan = vm::setPartPan,
                    onSetPartOctave = vm::setPartOctave,
                    onSetPartNoteRange = vm::setPartNoteRange,
                    onSetKeyboardLayout = vm::setKeyboardLayout,
                    onSetKeyboardHeightRatio = vm::setKeyboardHeightRatio,
                    onSetVisibleWhiteKeys = vm::setVisibleWhiteKeys,
                    onSetVelocityCurve = vm::setVelocityCurve,
                    onSetChordMode = vm::setChordMode,
                    onSetSplitNote = vm::setSplitNote,
                    onSetShowKeyLabels = vm::setShowKeyLabels,
                    onSetShowScrollbar = vm::setShowKeyboardScrollbar,
                    onLoadWorshipSong = vm::loadWorshipSong,
                    onAddWorshipSong = vm::saveCurrentAsWorshipSong,
                    onDeleteWorshipSong = vm::deleteWorshipSong
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveSystemBars()
        }
    }

    private fun configureCutoutAndImmersiveFullscreen() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    } else {
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
            }
        }
        applyImmersiveSystemBars()
    }

    private fun applyImmersiveSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
