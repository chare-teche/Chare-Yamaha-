package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerSection
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.BuiltInVoices
import com.example.domain.model.DetectedChord
import com.example.domain.model.ImportedSourceUi
import com.example.domain.model.KeyboardLayoutRange
import com.example.domain.model.KeyboardPartTarget
import com.example.domain.model.LibraryStyleItemUi
import com.example.domain.model.RegistrationFreezeTarget
import com.example.domain.model.SplitChordMode
import com.example.domain.model.TouchVelocityCurve
import com.example.domain.model.VoiceCategory
import com.example.domain.model.VoicePreset
import com.example.domain.model.WorshipSongUi
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldBright
import com.example.ui.theme.ChassisBorder
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.ChassisPanel
import com.example.ui.theme.ChassisSurface
import com.example.ui.theme.ChassisVoid
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LcdBodyBottom
import com.example.ui.theme.LcdBodyTop
import com.example.ui.theme.LcdCardBg
import com.example.ui.theme.LcdCardBorder
import com.example.ui.theme.LedAmber
import com.example.ui.theme.LedBlue
import com.example.ui.theme.LedCrimson
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrNeonBlue
import com.example.ui.theme.PsrNeonBlueGlow
import com.example.ui.theme.SilkscreenMuted
import com.example.ui.theme.SilkscreenWhite
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TileRegistTop
import com.example.ui.theme.TileStyleTop
import com.example.ui.theme.TileVoiceBottom
import com.example.ui.theme.TileVoiceTop

@Composable
fun ExpandedWorkspaceOverlay(
    state: ArrangerUiState,
    onDismiss: () -> Unit,
    onSwitchScreen: (ArrangerScreen) -> Unit,
    onSetMainMenuPage: (Int) -> Unit,
    onTogglePanelLock: () -> Unit,
    onImportYamahaSetClick: () -> Unit,
    onImportOrgSetClick: () -> Unit,
    onImportSingleFileClick: () -> Unit,
    onSelectStyle: (String) -> Unit,
    onRemoveSource: (Long) -> Unit,
    onRemoveStyle: (String) -> Unit,
    onSetStyleSearchQuery: (String) -> Unit,
    onToggleFavoriteStyle: (String) -> Unit,
    onSelectPartVoice: (KeyboardPartTarget, VoicePreset) -> Unit,
    onTogglePartEnabled: (KeyboardPartTarget) -> Unit,
    onToggleKeyboardPartSolo: (KeyboardPartTarget) -> Unit,
    onSetPartVolume: (KeyboardPartTarget, Float) -> Unit,
    onSetPartPan: (KeyboardPartTarget, Float) -> Unit,
    onSetPartOctave: (KeyboardPartTarget, Int) -> Unit,
    onSetPartNoteRange: (KeyboardPartTarget, Int, Int) -> Unit,
    onSetVoiceSearchQuery: (String) -> Unit,
    onToggleFavoriteVoice: (String) -> Unit,
    onToggleHarmony: () -> Unit,
    onSetChannelVolume: (AccompPart, Float) -> Unit,
    onSetChannelPan: (AccompPart, Float) -> Unit,
    onToggleChannelMute: (AccompPart) -> Unit,
    onToggleChannelSolo: (AccompPart) -> Unit,
    onClearAccompSolo: () -> Unit,
    onSetMasterVolume: (Float) -> Unit,
    onSetAcmpBalance: (Float) -> Unit,
    onSetStyleVolume: (Float) -> Unit,
    onToggleDsp: () -> Unit,
    onSetReverbDepth: (Float) -> Unit,
    onSetChorusDepth: (Float) -> Unit,
    onSetEqBass: (Float) -> Unit,
    onSetEqMid: (Float) -> Unit,
    onSetEqTreble: (Float) -> Unit,
    onSetMasterTuneHz: (Float) -> Unit,
    onSetAudioBufferFrames: (Int) -> Unit,
    onSetKeyboardLayout: (KeyboardLayoutRange) -> Unit,
    onSetKeyboardHeightRatio: (Float) -> Unit,
    onSetVisibleWhiteKeys: (Int) -> Unit,
    onSetVelocityCurve: (TouchVelocityCurve) -> Unit,
    onSetChordMode: (SplitChordMode) -> Unit,
    onSetSplitNote: (Int) -> Unit,
    onSetShowKeyLabels: (Boolean) -> Unit,
    onSetShowScrollbar: (Boolean) -> Unit,
    onToggleAcmp: () -> Unit,
    onToggleSplit: () -> Unit,
    onAdjustOctave: (Int) -> Unit,
    onAdjustTranspose: (Int) -> Unit,
    onSelectRegBank: (Int) -> Unit,
    onRecallRegSlot: (Int) -> Unit,
    onSaveRegSlot: (Int) -> Unit,
    onStepRegSequence: (Int) -> Unit,
    onToggleRegFreeze: () -> Unit,
    onToggleRegFreezeTarget: (RegistrationFreezeTarget) -> Unit,
    onToggleRegSequenceEnabled: () -> Unit,
    onAddRegSequenceStep: (Int, Int) -> Unit,
    onRemoveRegSequenceStepAt: (Int) -> Unit,
    onMoveRegSequenceStep: (Int, Int) -> Unit,
    onClearRegSequence: () -> Unit,
    onJumpToRegSequenceStep: (Int) -> Unit,
    onIntro: (ArrangerSection) -> Unit,
    onMainVariation: (ArrangerSection) -> Unit,
    onBreak: () -> Unit,
    onEnding: (ArrangerSection) -> Unit,
    onStartStop: () -> Unit,
    onSyncStart: () -> Unit,
    onSyncStop: () -> Unit,
    onAutoFill: () -> Unit,
    onFadeInOut: () -> Unit,
    onToggleTempoLock: () -> Unit,
    onToggleOtsLink: () -> Unit,
    onTapTempo: () -> Unit,
    onResetTempo: () -> Unit,
    onLoadWorshipSong: (WorshipSongUi) -> Unit,
    onAddWorshipSong: (String, String, String, String) -> Unit,
    onDeleteWorshipSong: (Long) -> Unit,
    onTriggerMultiPad: (Int) -> Unit,
    onStopAllMultiPads: () -> Unit,
    onToggleChordLooperRecord: () -> Unit,
    onToggleChordLooperPlay: () -> Unit,
    onClearChordLoop: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) { onDismiss() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(LcdBodyTop, LcdBodyBottom)))
    ) {
        // PSR-SX900 Cobalt Blue Header Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(Brush.horizontalGradient(listOf(TileVoiceBottom, TileVoiceTop)))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("workspace_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
                }
                Text(
                    text = state.activeScreen.title.uppercase(),
                    color = Color.White,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderTab("MENU", state.activeScreen == ArrangerScreen.MAIN_MENU) { onSwitchScreen(ArrangerScreen.MAIN_MENU) }
                HeaderTab("STYLES / SET", state.activeScreen == ArrangerScreen.STYLE_LIBRARY) { onSwitchScreen(ArrangerScreen.STYLE_LIBRARY) }
                HeaderTab("VOICES", state.activeScreen == ArrangerScreen.VOICE_SELECT) { onSwitchScreen(ArrangerScreen.VOICE_SELECT) }
                HeaderTab("MIXER & FX", state.activeScreen == ArrangerScreen.MIXER) { onSwitchScreen(ArrangerScreen.MIXER) }
                HeaderTab("WORSHIP / REG", state.activeScreen == ArrangerScreen.PLAYLIST) { onSwitchScreen(ArrangerScreen.PLAYLIST) }
                HeaderTab("PADS / LOOP", state.activeScreen == ArrangerScreen.MULTI_PAD_LOOPER) { onSwitchScreen(ArrangerScreen.MULTI_PAD_LOOPER) }
                HeaderTab("UTILITY", state.activeScreen == ArrangerScreen.SETTINGS) { onSwitchScreen(ArrangerScreen.SETTINGS) }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.testTag("workspace_close_button")) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_exit), tint = Color.White)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state.activeScreen) {
                ArrangerScreen.MAIN_MENU -> MainMenuWorkspace(
                    state = state,
                    onSetMainMenuPage = onSetMainMenuPage,
                    onOpenScreen = onSwitchScreen
                )
                ArrangerScreen.CHANNEL_ON_OFF -> ChannelOnOffWorkspace(
                    state = state,
                    onToggleChannelMute = onToggleChannelMute,
                    onToggleChannelSolo = onToggleChannelSolo,
                    onClearAccompSolo = onClearAccompSolo,
                    onTogglePartEnabled = onTogglePartEnabled,
                    onOpenMixer = { onSwitchScreen(ArrangerScreen.MIXER) }
                )
                ArrangerScreen.VOICE_PART_SETUP -> VoicePartSetupWorkspace(
                    state = state,
                    onTogglePartEnabled = onTogglePartEnabled,
                    onToggleKeyboardPartSolo = onToggleKeyboardPartSolo,
                    onSetPartVolume = onSetPartVolume,
                    onSetPartPan = onSetPartPan,
                    onSetPartOctave = onSetPartOctave,
                    onSetPartNoteRange = onSetPartNoteRange,
                    onToggleHarmony = onToggleHarmony,
                    onOpenVoiceBrowser = { onSwitchScreen(ArrangerScreen.VOICE_SELECT) }
                )
                ArrangerScreen.SPLIT_FINGERING -> SplitAndFingeringWorkspace(
                    state = state,
                    onToggleAcmp = onToggleAcmp,
                    onToggleSplit = onToggleSplit,
                    onSetChordMode = onSetChordMode,
                    onSetSplitNote = onSetSplitNote,
                    onAdjustOctave = onAdjustOctave,
                    onAdjustTranspose = onAdjustTranspose
                )
                ArrangerScreen.REGISTRATION_MEMORY,
                ArrangerScreen.PLAYLIST -> WorshipAndRegistrationWorkspace(
                    state = state,
                    onSelectBank = onSelectRegBank,
                    onRecallSlot = onRecallRegSlot,
                    onSaveSlot = onSaveRegSlot,
                    onStepSequence = onStepRegSequence,
                    onToggleFreeze = onToggleRegFreeze,
                    onLoadWorshipSong = onLoadWorshipSong,
                    onAddCurrentAsWorshipSong = onAddWorshipSong,
                    onDeleteWorshipSong = onDeleteWorshipSong
                )
                ArrangerScreen.REGISTRATION_FREEZE -> RegistrationFreezeWorkspace(
                    state = state,
                    onToggleRegFreeze = onToggleRegFreeze,
                    onToggleFreezeTarget = onToggleRegFreezeTarget
                )
                ArrangerScreen.REGISTRATION_SEQUENCE -> RegistrationSequenceWorkspace(
                    state = state,
                    onToggleSequenceEnabled = onToggleRegSequenceEnabled,
                    onAddStep = onAddRegSequenceStep,
                    onRemoveStepAt = onRemoveRegSequenceStepAt,
                    onMoveStep = onMoveRegSequenceStep,
                    onClearSequence = onClearRegSequence,
                    onJumpToStep = onJumpToRegSequenceStep,
                    onStepSequence = onStepRegSequence
                )
                ArrangerScreen.STYLE_SETTINGS -> StyleSettingsWorkspace(
                    state = state,
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
                    onSetAcmpBalance = onSetAcmpBalance,
                    onSetStyleVolume = onSetStyleVolume
                )
                ArrangerScreen.STYLE_LIBRARY -> StyleAndSetInspectorWorkspace(
                    state = state,
                    onImportYamahaSetClick = onImportYamahaSetClick,
                    onImportOrgSetClick = onImportOrgSetClick,
                    onImportSingleFileClick = onImportSingleFileClick,
                    onSelectStyle = onSelectStyle,
                    onRemoveSource = onRemoveSource,
                    onRemoveStyle = onRemoveStyle,
                    onSetStyleSearchQuery = onSetStyleSearchQuery,
                    onToggleFavoriteStyle = onToggleFavoriteStyle
                )
                ArrangerScreen.VOICE_SELECT -> VoiceAndPartWorkspace(
                    state = state,
                    onSelectPartVoice = onSelectPartVoice,
                    onTogglePartEnabled = onTogglePartEnabled,
                    onSetPartVolume = onSetPartVolume,
                    onSetPartPan = onSetPartPan,
                    onSetPartOctave = onSetPartOctave,
                    onSetPartNoteRange = onSetPartNoteRange,
                    onImportWavSampleClick = onImportSingleFileClick,
                    onSetVoiceSearchQuery = onSetVoiceSearchQuery,
                    onToggleFavoriteVoice = onToggleFavoriteVoice
                )
                ArrangerScreen.MIXER -> MixerAndEffectsWorkspace(
                    state = state,
                    onSetChannelVolume = onSetChannelVolume,
                    onSetChannelPan = onSetChannelPan,
                    onToggleChannelMute = onToggleChannelMute,
                    onToggleChannelSolo = onToggleChannelSolo,
                    onSetMasterVolume = onSetMasterVolume,
                    onSetAcmpBalance = onSetAcmpBalance,
                    onSetStyleVolume = onSetStyleVolume,
                    onToggleDsp = onToggleDsp,
                    onSetReverbDepth = onSetReverbDepth,
                    onSetChorusDepth = onSetChorusDepth,
                    onSetEqBass = onSetEqBass,
                    onSetEqMid = onSetEqMid,
                    onSetEqTreble = onSetEqTreble
                )
                ArrangerScreen.MULTI_PAD_LOOPER -> MultiPadAndChordLooperWorkspace(
                    state = state,
                    onTriggerMultiPad = onTriggerMultiPad,
                    onStopAllMultiPads = onStopAllMultiPads,
                    onToggleChordLooperRecord = onToggleChordLooperRecord,
                    onToggleChordLooperPlay = onToggleChordLooperPlay,
                    onClearChordLoop = onClearChordLoop
                )
                ArrangerScreen.MIDI_SETTINGS -> MidiWorkspace(state = state)
                ArrangerScreen.FILE_MANAGER -> FileManagerWorkspace(
                    state = state,
                    onImportYamahaSetClick = onImportYamahaSetClick,
                    onImportOrgSetClick = onImportOrgSetClick,
                    onImportSingleFileClick = onImportSingleFileClick,
                    onRemoveSource = onRemoveSource,
                    onOpenStyleLibrary = { onSwitchScreen(ArrangerScreen.STYLE_LIBRARY) }
                )
                ArrangerScreen.SETTINGS -> UtilityAndKeyboardSettingsWorkspace(
                    state = state,
                    onSetKeyboardLayout = onSetKeyboardLayout,
                    onSetKeyboardHeightRatio = onSetKeyboardHeightRatio,
                    onSetVisibleWhiteKeys = onSetVisibleWhiteKeys,
                    onSetVelocityCurve = onSetVelocityCurve,
                    onSetChordMode = onSetChordMode,
                    onSetSplitNote = onSetSplitNote,
                    onSetShowKeyLabels = onSetShowKeyLabels,
                    onSetShowScrollbar = onSetShowScrollbar,
                    onSetMasterTuneHz = onSetMasterTuneHz,
                    onSetAudioBufferFrames = onSetAudioBufferFrames
                )
                ArrangerScreen.SYSTEM_SETTINGS -> SystemSettingsWorkspace(
                    state = state,
                    onSetMasterTuneHz = onSetMasterTuneHz,
                    onSetAudioBufferFrames = onSetAudioBufferFrames,
                    onTogglePanelLock = onTogglePanelLock
                )
                ArrangerScreen.SONG_PLAYER -> PlannedFeatureWorkspace(
                    title = "Song Player",
                    summary = "Status: Planned (Not yet implemented in this release)",
                    details = "Full Standard MIDI File (SMF) & audio backing-track transport deck with lyrics/score marker sync is planned for a future stage. Currently, SMF patterns imported via Style & SET Library can be auditioned inside the Style engine."
                )
                ArrangerScreen.SONG_RECORDING -> PlannedFeatureWorkspace(
                    title = "Song Recording",
                    summary = "Status: Planned (Not yet implemented in this release)",
                    details = "Live WAV audio capture and multi-track MIDI performance recording are planned for a future stage. Use Chord Looper or Registration Memory today to store chord progressions and panel setups offline."
                )
                ArrangerScreen.PERFORMANCE -> Unit
            }
        }
    }
}

@Composable
private fun HeaderTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) PsrActiveOrange else Color(0x33000000))
            .border(1.dp, if (selected) Color.White else Color(0x55FFFFFF), RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
    }
}

// --- 1. YAMAHA SET & ORG.SET LIBRARY + DEEP FILE INSPECTOR ---

@Composable
private fun StyleAndSetInspectorWorkspace(
    state: ArrangerUiState,
    onImportYamahaSetClick: () -> Unit,
    onImportOrgSetClick: () -> Unit,
    onImportSingleFileClick: () -> Unit,
    onSelectStyle: (String) -> Unit,
    onRemoveSource: (Long) -> Unit,
    onRemoveStyle: (String) -> Unit,
    onSetStyleSearchQuery: (String) -> Unit,
    onToggleFavoriteStyle: (String) -> Unit
) {
    var filterTab by remember { mutableStateOf("ALL") } // ALL, FAV, YAMAHA, ORG, BUILTIN
    var selectedSourceId by remember { mutableStateOf<Long?>(null) }
    var inspectedItem by remember { mutableStateOf<LibraryStyleItemUi?>(null) }

    val filteredStyles = remember(state.libraryStyles, filterTab, selectedSourceId, state.styleSearchQuery, state.favoriteStyleIds) {
        val q = state.styleSearchQuery.trim().lowercase()
        state.libraryStyles.filter { item ->
            val tabMatch = when (filterTab) {
                "FAV" -> item.id in state.favoriteStyleIds
                "YAMAHA" -> item.setFamily == "YAMAHA_SET"
                "ORG" -> item.setFamily == "ORG_SET"
                "BUILTIN" -> item.isBuiltIn
                else -> true
            }
            val srcMatch = selectedSourceId == null || item.sourceId == selectedSourceId
            val queryMatch = q.isEmpty() || item.name.lowercase().contains(q) || item.category.lowercase().contains(q)
            tabMatch && srcMatch && queryMatch
        }
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left: Separate Yamaha SET & ORG.SET Importers + Source Folders
        Column(
            modifier = Modifier
                .weight(0.36f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text("SET & STYLE IMPORTER", color = PsrActiveOrange, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            Text(
                text = "Separate parsers for Yamaha SET (.sty/.prs/.sst/.mid) and ORG.SET (.ost/.wav/.pcg/.kmp). Original user files are never modified.",
                color = SilkscreenMuted, fontFamily = InterFamily, fontSize = 10.sp, lineHeight = 13.sp
            )

            Button(
                onClick = onImportYamahaSetClick,
                enabled = !state.isImporting,
                colors = ButtonDefaults.buttonColors(containerColor = TileStyleTop),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("import_yamaha_set_button")
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Import Yamaha SET Folder", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onImportOrgSetClick,
                enabled = !state.isImporting,
                colors = ButtonDefaults.buttonColors(containerColor = PsrNeonBlue),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("import_org_set_button")
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Import ORG.SET Folder", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onImportSingleFileClick,
                enabled = !state.isImporting,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth().height(34.dp).testTag("import_style_file_button")
            ) {
                Icon(Icons.Default.AudioFile, contentDescription = null, tint = LedEmerald, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Import Single .STY / .MID / .WAV", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            if (state.isImporting) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = PsrActiveOrange)
                    Text("Inspecting SET structure...", color = PsrActiveOrange, fontSize = 10.5.sp)
                }
            }

            Text("IMPORTED SOURCES (${state.importedSources.size})", color = SilkscreenWhite, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(state.importedSources, key = { it.id }) { src ->
                    ImportedSourceRow(
                        source = src,
                        isSelected = selectedSourceId == src.id,
                        onSelect = { selectedSourceId = if (selectedSourceId == src.id) null else src.id },
                        onDelete = { if (selectedSourceId == src.id) selectedSourceId = null; onRemoveSource(src.id) }
                    )
                }
            }
        }

        // Right: Style List + Live File Compatibility Inspector Panel
        Column(
            modifier = Modifier
                .weight(0.64f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterPill("All (${state.libraryStyles.size})", filterTab == "ALL") { filterTab = "ALL"; selectedSourceId = null }
                FilterPill("Favorites (${state.favoriteStyleIds.size})", filterTab == "FAV") { filterTab = "FAV"; selectedSourceId = null }
                FilterPill("Yamaha SET", filterTab == "YAMAHA") { filterTab = "YAMAHA" }
                FilterPill("ORG.SET", filterTab == "ORG") { filterTab = "ORG" }
                FilterPill("Built-In", filterTab == "BUILTIN") { filterTab = "BUILTIN"; selectedSourceId = null }
            }

            OutlinedTextField(
                value = state.styleSearchQuery,
                onValueChange = onSetStyleSearchQuery,
                placeholder = { Text(stringResource(R.string.label_search_styles), fontSize = 11.sp, color = SilkscreenMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PsrNeonBlueGlow,
                    unfocusedBorderColor = ChassisBorder
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            // Detailed File Inspection Banner when an item is inspected
            inspectedItem?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ChassisDark)
                        .border(1.dp, if (item.isSupported) LedEmerald else PsrActiveOrange, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FILE INSPECTOR: ${item.name} [${item.fileFormat}]", color = PsrNeonBlueGlow, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("CLOSE", color = LedCrimson, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { inspectedItem = null })
                    }
                    Text("Compatibility: ${item.compatibilityStatus} • Set Type: ${item.setFamily} • Sections: ${item.sectionSummary}", color = Color.White, fontSize = 10.sp)
                    Text("Inspection: ${item.parseMessage}", color = SilkscreenMuted, fontSize = 10.sp)
                    Text("Missing / Fallback Resources: ${item.missingResources}", color = LedAmber, fontSize = 10.sp)
                }
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(filteredStyles, key = { it.id }) { item ->
                    StyleRow(
                        item = item,
                        isActive = item.id == state.activeStyle.id,
                        isFavorite = item.id in state.favoriteStyleIds,
                        onToggleFavorite = { onToggleFavoriteStyle(item.id) },
                        onInspect = { inspectedItem = item },
                        onLoad = { onSelectStyle(item.id) },
                        onDelete = if (!item.isBuiltIn) ({ onRemoveStyle(item.id) }) else null
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportedSourceRow(
    source: ImportedSourceUi,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) LcdCardBg else ChassisDark)
            .border(1.dp, if (isSelected) PsrNeonBlueGlow else ChassisBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onSelect)
            .padding(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(source.displayName, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(source.setFamily, color = PsrActiveOrange, fontFamily = JetBrainsMonoFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "${source.supportedStyles} styles • ${source.supportedSamples} WAV • ${source.unsupportedFiles} unsupported",
                color = if (source.supportedStyles + source.supportedSamples > 0) LedEmerald else LedAmber,
                fontSize = 9.5.sp
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Remove source", tint = LedCrimson, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun StyleRow(
    item: LibraryStyleItemUi,
    isActive: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onInspect: () -> Unit,
    onLoad: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val playable = item.compatibilityStatus == "PLAYABLE"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    isActive -> TileStyleTop.copy(alpha = 0.32f)
                    playable -> LcdCardBg
                    else -> Color(0xFF221A1F)
                }
            )
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = when {
                    isActive -> PsrActiveOrange
                    playable -> LcdCardBorder
                    else -> LedCrimson.copy(alpha = 0.45f)
                },
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { if (playable) onLoad() else onInspect() }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(
                    imageVector = if (item.isSupported) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (item.isSupported) LedEmerald else LedAmber,
                    modifier = Modifier.size(13.dp)
                )
                Text(item.name, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (item.setFamily == "ORG_SET") PsrNeonBlue.copy(alpha = 0.45f) else Color(0xFF2B3E5E))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(item.fileFormat, color = Color.White, fontFamily = JetBrainsMonoFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = if (playable) "${item.category} • ${item.tempoBpm} BPM • ${item.timeSignature} • ${item.sectionSummary}" else item.parseMessage,
                color = if (playable) Color(0xFFBBD0EB) else Color(0xFFF5B7B1),
                fontFamily = InterFamily,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite Style",
                    tint = if (isFavorite) ChampagneGoldBright else SilkscreenMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onInspect, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Info, contentDescription = "Inspect file", tint = PsrNeonBlueGlow, modifier = Modifier.size(16.dp))
            }
            if (playable) {
                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isActive) LedEmerald else PsrActiveOrange),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(if (isActive) "LOADED" else "LOAD", color = if (isActive) ChassisVoid else Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LedCrimson, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

// --- 2. FOUR-PART KEYBOARD & VOICE SELECTION (LEFT, RIGHT 1, RIGHT 2, RIGHT 3 + USER WAV SAMPLES) ---

@Composable
private fun VoiceAndPartWorkspace(
    state: ArrangerUiState,
    onSelectPartVoice: (KeyboardPartTarget, VoicePreset) -> Unit,
    onTogglePartEnabled: (KeyboardPartTarget) -> Unit,
    onSetPartVolume: (KeyboardPartTarget, Float) -> Unit,
    onSetPartPan: (KeyboardPartTarget, Float) -> Unit,
    onSetPartOctave: (KeyboardPartTarget, Int) -> Unit,
    onSetPartNoteRange: (KeyboardPartTarget, Int, Int) -> Unit,
    onImportWavSampleClick: () -> Unit,
    onSetVoiceSearchQuery: (String) -> Unit,
    onToggleFavoriteVoice: (String) -> Unit
) {
    var activeTarget by remember { mutableStateOf(KeyboardPartTarget.RIGHT_1) }
    var selectedCategory by remember { mutableStateOf<VoiceCategory?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }

    val allVoices = remember(state.userSampleVoices) { BuiltInVoices.all + state.userSampleVoices }
    val filtered = remember(allVoices, selectedCategory, favoritesOnly, state.voiceSearchQuery, state.favoriteVoiceIds) {
        val q = state.voiceSearchQuery.trim().lowercase()
        allVoices.filter { v ->
            val catMatch = selectedCategory == null || v.category == selectedCategory
            val favMatch = !favoritesOnly || v.id in state.favoriteVoiceIds
            val queryMatch = q.isEmpty() || v.name.lowercase().contains(q) || v.description.lowercase().contains(q)
            catMatch && favMatch && queryMatch
        }
    }
    val activePartConfig = state.partConfig(activeTarget)

    Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        // 4 Independent Part Cards: LEFT | RIGHT 1 | RIGHT 2 | RIGHT 3
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                Triple(KeyboardPartTarget.LEFT, "LEFT (SPLIT)", LedBlue),
                Triple(KeyboardPartTarget.RIGHT_1, "RIGHT 1 (MAIN)", PsrNeonBlueGlow),
                Triple(KeyboardPartTarget.RIGHT_2, "RIGHT 2 (LAYER)", PsrActiveOrange),
                Triple(KeyboardPartTarget.RIGHT_3, "RIGHT 3 (LAYER)", LedEmerald)
            ).forEach { (target, title, accent) ->
                val cfg = state.partConfig(target)
                PartAssignCard(
                    title = title,
                    config = cfg,
                    accentColor = accent,
                    selected = activeTarget == target,
                    onSelect = { activeTarget = target },
                    onToggleEnabled = { onTogglePartEnabled(target) },
                    onVolumeChange = { onSetPartVolume(target, it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Focused Part Parameter Strip: Independent Pan, Octave, and Note-Range Routing for Selected Part
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${activeTarget.label} PARAMS",
                color = PsrNeonBlueGlow,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )

            // Independent Pan control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1.1f)
            ) {
                val panText = when {
                    activePartConfig.pan < -0.05f -> "L${(-activePartConfig.pan * 64).toInt()}"
                    activePartConfig.pan > 0.05f -> "R${(activePartConfig.pan * 64).toInt()}"
                    else -> "C"
                }
                Text("PAN $panText", color = SilkscreenWhite, fontFamily = JetBrainsMonoFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = activePartConfig.pan,
                    valueRange = -1f..1f,
                    onValueChange = { onSetPartPan(activeTarget, it) },
                    colors = SliderDefaults.colors(thumbColor = PsrActiveOrange, activeTrackColor = PsrNeonBlueGlow),
                    modifier = Modifier.weight(1f).height(22.dp).testTag("part_pan_slider")
                )
            }

            // Independent Octave shift (-2 .. +2)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("OCT", color = SilkscreenMuted, fontFamily = SpaceGroteskFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                SmallStepButton("-", "part_oct_down") {
                    onSetPartOctave(activeTarget, activePartConfig.octaveShift - 1)
                }
                Text(
                    text = if (activePartConfig.octaveShift > 0) "+${activePartConfig.octaveShift}" else "${activePartConfig.octaveShift}",
                    color = PsrActiveOrange,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
                SmallStepButton("+", "part_oct_up") {
                    onSetPartOctave(activeTarget, activePartConfig.octaveShift + 1)
                }
            }

            // Independent Note Range (Low - High) with split-preservation awareness
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("RANGE", color = SilkscreenMuted, fontFamily = SpaceGroteskFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                SmallStepButton("L-", "part_low_down") {
                    onSetPartNoteRange(activeTarget, activePartConfig.lowNoteMidi - 6, activePartConfig.highNoteMidi)
                }
                SmallStepButton("L+", "part_low_up") {
                    onSetPartNoteRange(activeTarget, (activePartConfig.lowNoteMidi + 6).coerceAtMost(activePartConfig.highNoteMidi), activePartConfig.highNoteMidi)
                }
                Text(
                    text = activePartConfig.rangeLabel,
                    color = LedEmerald,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
                SmallStepButton("H-", "part_high_down") {
                    onSetPartNoteRange(activeTarget, activePartConfig.lowNoteMidi, (activePartConfig.highNoteMidi - 6).coerceAtLeast(activePartConfig.lowNoteMidi))
                }
                SmallStepButton("H+", "part_high_up") {
                    onSetPartNoteRange(activeTarget, activePartConfig.lowNoteMidi, activePartConfig.highNoteMidi + 6)
                }
                SmallStepButton("FULL", "part_range_reset") {
                    onSetPartNoteRange(activeTarget, 21, 108)
                }
            }
        }

        // Category Filter Row + Search + Favorites + Import WAV Sample button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.voiceSearchQuery,
                onValueChange = onSetVoiceSearchQuery,
                placeholder = { Text(stringResource(R.string.label_search_voices), fontSize = 10.5.sp, color = SilkscreenMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PsrNeonBlueGlow,
                    unfocusedBorderColor = ChassisBorder
                ),
                modifier = Modifier.width(175.dp).height(48.dp)
            )
            LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                item { FilterPill("All (${allVoices.size})", selectedCategory == null && !favoritesOnly) { selectedCategory = null; favoritesOnly = false } }
                item { FilterPill("Favorites (${state.favoriteVoiceIds.size})", favoritesOnly) { favoritesOnly = !favoritesOnly } }
                items(VoiceCategory.entries) { cat ->
                    FilterPill(cat.displayName, selectedCategory == cat) { selectedCategory = cat; favoritesOnly = false }
                }
            }
            OutlinedButton(
                onClick = onImportWavSampleClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("+ WAV Sample", color = LedEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 175.dp),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(filtered, key = { it.id }) { voice ->
                val isSelected = voice.id == activePartConfig.voice.id
                val isFav = voice.id in state.favoriteVoiceIds
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (isSelected) Brush.verticalGradient(listOf(TileVoiceTop, TileVoiceBottom))
                            else Brush.verticalGradient(listOf(LcdCardBg, ChassisPanel))
                        )
                        .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) PsrNeonBlueGlow else LcdCardBorder, RoundedCornerShape(7.dp))
                        .testTag("voice_card_${voice.id}")
                        .clickable { onSelectPartVoice(activeTarget, voice) }
                        .padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(voice.category.displayName.uppercase(), color = if (isSelected) Color(0xFFD6EEFF) else PsrActiveOrange, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite Voice",
                            tint = if (isFav) ChampagneGoldBright else if (isSelected) Color.White else SilkscreenMuted,
                            modifier = Modifier.size(15.dp).clickable { onToggleFavoriteVoice(voice.id) }
                        )
                    }
                    Text(voice.name, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(voice.description, color = if (isSelected) Color(0xFFD8EEFF) else SilkscreenMuted, fontFamily = InterFamily, fontSize = 9.5.sp, lineHeight = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun SmallStepButton(label: String, tag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ChassisDark)
            .border(1.dp, ChassisBorder, RoundedCornerShape(4.dp))
            .testTag(tag)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp)
    }
}

@Composable
private fun PartAssignCard(
    title: String,
    config: com.example.domain.model.KeyboardPartConfig,
    accentColor: Color,
    selected: Boolean,
    onSelect: () -> Unit,
    onToggleEnabled: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val octLabel = if (config.octaveShift > 0) "+${config.octaveShift}" else "${config.octaveShift}"
    val panLabel = when {
        config.pan < -0.05f -> "L${(-config.pan * 64).toInt()}"
        config.pan > 0.05f -> "R${(config.pan * 64).toInt()}"
        else -> "C"
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) accentColor.copy(alpha = 0.22f) else ChassisPanel)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) accentColor else LcdCardBorder, RoundedCornerShape(7.dp))
            .testTag("part_card_${config.part.name.lowercase()}")
            .clickable(onClick = onSelect)
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = if (selected) accentColor else SilkscreenMuted, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 8.8.sp, maxLines = 1)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (config.isEnabled) TileRegistTop else ChassisDark)
                    .testTag("part_toggle_${config.part.name.lowercase()}")
                    .clickable(onClick = onToggleEnabled)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(if (config.isEnabled) "ON" else "OFF", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(config.voice.name, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            text = "VOL ${(config.volume * 100).toInt()} • $panLabel • OCT $octLabel • ${config.rangeLabel}",
            color = SilkscreenMuted,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 7.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Slider(
            value = config.volume,
            onValueChange = onVolumeChange,
            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor),
            modifier = Modifier.height(20.dp)
        )
    }
}

// --- 3. 9-CHANNEL MIXER + STEREO REVERB, CHORUS & 3-BAND MASTER EQ ---

@Composable
private fun MixerAndEffectsWorkspace(
    state: ArrangerUiState,
    onSetChannelVolume: (AccompPart, Float) -> Unit,
    onSetChannelPan: (AccompPart, Float) -> Unit,
    onToggleChannelMute: (AccompPart) -> Unit,
    onToggleChannelSolo: (AccompPart) -> Unit,
    onSetMasterVolume: (Float) -> Unit,
    onSetAcmpBalance: (Float) -> Unit,
    onSetStyleVolume: (Float) -> Unit,
    onToggleDsp: () -> Unit,
    onSetReverbDepth: (Float) -> Unit,
    onSetChorusDepth: (Float) -> Unit,
    onSetEqBass: (Float) -> Unit,
    onSetEqMid: (Float) -> Unit,
    onSetEqTreble: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Top DSP Effects + Master Bus Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(7.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (state.isDspEnabled) PsrNeonBlue else ChassisDark)
                    .clickable(onClick = onToggleDsp)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(if (state.isDspEnabled) "DSP ON" else "DSP OFF", color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            CompactSlider("MASTER", state.masterVolume, onSetMasterVolume, Modifier.weight(1f))
            CompactSlider("BALANCE", state.acmpBalance, onSetAcmpBalance, Modifier.weight(1f))
            CompactSlider("STYLE VOL", state.styleVolume, onSetStyleVolume, Modifier.weight(1f))
            CompactSlider("REVERB", state.reverbDepth, onSetReverbDepth, Modifier.weight(1f))
            CompactSlider("CHORUS", state.chorusDepth, onSetChorusDepth, Modifier.weight(1f))
            CompactSlider("EQ LOW", state.eqBass, onSetEqBass, Modifier.weight(0.85f))
            CompactSlider("EQ MID", state.eqMid, onSetEqMid, Modifier.weight(0.85f))
            CompactSlider("EQ HI", state.eqTreble, onSetEqTreble, Modifier.weight(0.85f))
        }

        // 9 Channel Strips: DRUM, PERC, BASS, CHD1, CHD2, PAD, PHR1, PHR2, RGT
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.mixerChannels.forEach { ch ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(7.dp))
                        .background(ChassisPanel)
                        .border(1.dp, if (ch.isMuted) LedCrimson.copy(alpha = 0.5f) else LcdCardBorder, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(ch.part.shortLabel, color = if (ch.part == AccompPart.RIGHT_VOICE) PsrActiveOrange else PsrNeonBlueGlow, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text("${(ch.volume * 127).toInt()}", color = Color.White, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .width(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ChassisVoid)
                            .border(1.dp, ChassisBorder, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(if (ch.isMuted) 0.04f else ch.volume.coerceIn(0.05f, 1f))
                                .background(
                                    Brush.verticalGradient(
                                        if (ch.isMuted) listOf(Color.Gray, Color.DarkGray)
                                        else listOf(PsrActiveOrange, PsrNeonBlueGlow, LedEmerald)
                                    )
                                )
                        )
                    }

                    Slider(
                        value = ch.volume,
                        onValueChange = { onSetChannelVolume(ch.part, it) },
                        colors = SliderDefaults.colors(thumbColor = PsrActiveOrange, activeTrackColor = PsrNeonBlueGlow),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )

                    // Pan quick toggle L / C / R
                    val panLabel = when {
                        ch.pan < -0.2f -> "PAN L"
                        ch.pan > 0.2f -> "PAN R"
                        else -> "PAN C"
                    }
                    Text(
                        text = panLabel,
                        color = SilkscreenMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 8.5.sp,
                        modifier = Modifier.clickable {
                            val nextPan = when {
                                ch.pan < -0.2f -> 0f
                                ch.pan > 0.2f -> -0.55f
                                else -> 0.55f
                            }
                            onSetChannelPan(ch.part, nextPan)
                        }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(25.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (ch.isMuted) LedCrimson else ChassisSurface)
                                .border(1.dp, if (ch.isMuted) LedCrimson else ChassisBorder, RoundedCornerShape(4.dp))
                                .testTag("mixer_mute_${ch.part.name.lowercase()}")
                                .clickable { onToggleChannelMute(ch.part) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (ch.isMuted) "MUTE" else "ON", color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(25.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (ch.isSoloed) ChampagneGold else ChassisDark)
                                .border(1.dp, if (ch.isSoloed) ChampagneGoldBright else ChassisBorder, RoundedCornerShape(4.dp))
                                .testTag("mixer_solo_${ch.part.name.lowercase()}")
                                .clickable { onToggleChannelSolo(ch.part) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("S", color = if (ch.isSoloed) Color.Black else ChampagneGoldBright, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactSlider(label: String, value: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = SilkscreenWhite, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 8.5.sp, maxLines = 1)
            Text("${(value * 100).toInt()}", color = PsrActiveOrange, fontFamily = JetBrainsMonoFamily, fontSize = 8.5.sp)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            colors = SliderDefaults.colors(thumbColor = PsrActiveOrange, activeTrackColor = PsrNeonBlueGlow),
            modifier = Modifier.height(24.dp)
        )
    }
}

// --- 4. UTILITY, KEYBOARD (49/61/76/88), TUNING, AUDIO LATENCY & MIDI SETTINGS ---

@Composable
private fun UtilityAndKeyboardSettingsWorkspace(
    state: ArrangerUiState,
    onSetKeyboardLayout: (KeyboardLayoutRange) -> Unit,
    onSetKeyboardHeightRatio: (Float) -> Unit,
    onSetVisibleWhiteKeys: (Int) -> Unit,
    onSetVelocityCurve: (TouchVelocityCurve) -> Unit,
    onSetChordMode: (SplitChordMode) -> Unit,
    onSetSplitNote: (Int) -> Unit,
    onSetShowKeyLabels: (Boolean) -> Unit,
    onSetShowScrollbar: (Boolean) -> Unit,
    onSetMasterTuneHz: (Float) -> Unit,
    onSetAudioBufferFrames: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Left: Keyboard Compass (49/61/76/88), Height, Width, Velocity Curve, Split & Chord Fingering
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("KEYBOARD & TOUCH RESPONSE", color = PsrActiveOrange, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Text("Keyboard Layout Range", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                KeyboardLayoutRange.entries.forEach { layout ->
                    FilterPill("${layout.totalKeys} Keys", state.keyboardLayout == layout) { onSetKeyboardLayout(layout) }
                }
            }

            Text("Touch Velocity Curve", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                TouchVelocityCurve.entries.forEach { curve ->
                    FilterPill(curve.displayName, state.velocityCurve == curve) { onSetVelocityCurve(curve) }
                }
            }
            Text(state.velocityCurve.description, color = SilkscreenMuted, fontSize = 10.sp)

            Text("Chord Fingering Recognition", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SplitChordMode.entries.forEach { mode ->
                    FilterPill(mode.displayName, state.chordMode == mode) { onSetChordMode(mode) }
                }
            }

            SettingSliderRow("Split Point", DetectedChord.midiNoteName(state.splitNoteMidi), state.splitNoteMidi.toFloat(), 48f..72f) {
                onSetSplitNote(it.toInt())
            }
            SettingSliderRow("Key Width (Visible White Keys)", "${state.visibleWhiteKeyCount} keys", state.visibleWhiteKeyCount.toFloat(), 10f..22f) {
                onSetVisibleWhiteKeys(it.toInt())
            }
            SettingSliderRow("Keyboard Height", "${(state.keyboardHeightRatio * 100).toInt()}%", state.keyboardHeightRatio, 0.36f..0.54f) {
                onSetKeyboardHeightRatio(it)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show Note Labels (C / Split)", color = Color.White, fontSize = 11.5.sp)
                Switch(checked = state.showKeyLabels, onCheckedChange = onSetShowKeyLabels, colors = SwitchDefaults.colors(checkedTrackColor = TileRegistTop))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show Horizontal Key-Range Scrollbar", color = Color.White, fontSize = 11.5.sp)
                Switch(checked = state.showKeyboardScrollbar, onCheckedChange = onSetShowScrollbar, colors = SwitchDefaults.colors(checkedTrackColor = TileRegistTop))
            }
        }

        // Right: Master Tune, Low-Latency Audio Buffer, MIDI Devices & Honest Status
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("AUDIO DSP, TUNING & MIDI", color = PsrNeonBlueGlow, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            SettingSliderRow("Master Tune (A4)", String.format("%.1f Hz", state.masterTuneHz), state.masterTuneHz, 415f..466f) {
                onSetMasterTuneHz(it)
            }

            Text("Audio Buffer Size (Latency vs Stability)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(256 to "256 (~6ms)", 512 to "512 (~12ms)", 1024 to "1024 (~23ms)").forEach { (frames, lbl) ->
                    FilterPill(lbl, state.audioBufferFrames == frames) { onSetAudioBufferFrames(frames) }
                }
            }

            InfoCard(
                icon = Icons.Default.GraphicEq,
                title = "Real-Time Audio Engine Status",
                body = if (state.audioEngineRunning) {
                    "RUNNING • 44.1kHz Stereo 16-bit PCM AudioTrack (Low-Latency mode) • Voice pool: 64 melodic + 16 drum slots (full simultaneous polyphony unverified on device) • Stereo Reverb, Chorus, 3-Band EQ & soft limiter."
                } else "Standby",
                accent = LedEmerald
            )

            InfoCard(
                icon = Icons.Default.Usb,
                title = "External USB / Bluetooth MIDI",
                body = if (state.connectedMidiDevices.isEmpty()) {
                    "No external MIDI keyboard detected. Connect a USB-OTG MIDI keyboard to play chords & voices live."
                } else "Connected: ${state.connectedMidiDevices.joinToString(", ")}",
                accent = if (state.connectedMidiDevices.isNotEmpty()) LedEmerald else PsrNeonBlueGlow
            )

            InfoCard(
                icon = Icons.Default.Info,
                title = "Feature Scope & Honest Status",
                body = "Implemented: PSR-SX900-inspired console, 49–88 scrollable keyboard, 4-part keyboard routing (LEFT, RIGHT 1, RIGHT 2, RIGHT 3 with independent enable/voice/volume/pan/octave/range), 8-part built-in arranger, Chord Harmony, Reverb/Chorus/3-Band EQ, Registration Banks 1–4 + Freeze, Worship Setlist, Multi-Pads & Chord Looper, USB/BT MIDI input.\nPartially implemented: Yamaha .sty/SMF & ORG.SET file inspection with root-transposition SMF playback, single-shot PCM WAV user samples (8s cap, no loop points), and procedural built-in synth voices.\nPlanned: SoundFont2 (.sf2) multi-zone sample engine, Audio/Song WAV Recorder, full Yamaha CASM NTR/NTT playback, Style Creator editor, and native Korg KORF decoder.",
                accent = PsrActiveOrange
            )

            Text(stringResource(R.string.disclaimer_notice), color = SilkscreenMuted, fontFamily = InterFamily, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun SettingSliderRow(label: String, valueText: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(valueText, color = PsrNeonBlueGlow, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = PsrActiveOrange, activeTrackColor = PsrNeonBlueGlow),
            modifier = Modifier.height(26.dp)
        )
    }
}

@Composable
private fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ChassisDark)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(body, color = SilkscreenMuted, fontFamily = InterFamily, fontSize = 10.sp, lineHeight = 13.5.sp)
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) PsrActiveOrange else ChassisDark)
            .border(1.dp, if (selected) Color.White else ChassisBorder, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
    }
}
