package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerSection
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.KeyboardLayoutRange
import com.example.domain.model.KeyboardPartTarget
import com.example.domain.model.SplitChordMode
import com.example.domain.model.TouchVelocityCurve
import com.example.domain.model.VoicePreset
import com.example.domain.model.WorshipSongUi
import com.example.ui.components.CenterLcdDisplay
import com.example.ui.components.EthioFlagBadge
import com.example.ui.components.NeonHaloMasterKnob
import com.example.ui.components.PerforatedSpeakerGrille
import com.example.ui.components.PlayablePianoKeyboard
import com.example.ui.components.PsrDataWheelWithArrows
import com.example.ui.components.PsrHardwareButton
import com.example.ui.components.PsrStripKey
import com.example.ui.components.PsrStripSectionHeader
import com.example.ui.components.RotaryMetalKnob
import com.example.ui.components.SignalPeakMeter
import com.example.ui.theme.ChassisBorder
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.ChassisPanel
import com.example.ui.theme.ChassisVoid
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LedAmber
import com.example.ui.theme.LedCrimson
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrNeonBlue
import com.example.ui.theme.PsrNeonBlueGlow
import com.example.ui.theme.SilkscreenMuted
import com.example.ui.theme.SilkscreenWhite
import com.example.ui.theme.SpaceGroteskFamily

/**
 * Professional Yamaha PSR-SX900-inspired ETHIO YAMAHA Arranger Console matching the reference layout:
 * - Top Status Bar: ETHIO YAMAHA + Ethiopian Flag | Tempo, Time Sig, Measure.Beat, Live Signal Meter, Settings, SET Library, Help, Menu
 * - Upper Brushed-Aluminum Console: Left Speaker | Neon-Blue Halo Master Volume + 2x4 Matrix (SONG/STYLE/VOICE/MIDI/UTILITY/REGIST/MEMORY/MENU) + Balance/Mixer/Direct Access |
 *   Center PSR-SX900 Home Touchscreen LCD (Orange Style, Blue Voice, Violet Chord, Green Registration + 6-icon dock) |
 *   Data Wheel + EXIT/MENU/ENTER | PANEL LOCK + Voice Effect + LEFT/RIGHT1/RIGHT2 Part Select + Harmony + DSP/Auto Fill | Right Speaker
 * - Lower Full-Width Arranger Strip: STYLE CONTROL (INTRO 1-3, MAIN A-D, BREAK, ENDING 1-3, SYNC START/STOP, START/STOP, FADE IN/OUT, TAP TEMPO) |
 *   REGISTRATION MEMORY 1-8 | ONE TOUCH SETTING 1-4
 * - Bottom Playable Piano Keyboard (49/61/76/88 scrollable range, realistic tall white & shorter black keys).
 */
@Composable
fun ArrangerConsoleScreen(
    state: ArrangerUiState,
    onOpenScreen: (ArrangerScreen) -> Unit,
    onExitToPerformance: () -> Unit,
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
    onToggleSustain: () -> Unit,
    onTogglePartEnabled: (KeyboardPartTarget) -> Unit,
    onToggleHarmony: () -> Unit,
    onToggleDsp: () -> Unit,
    onToggleReverbQuick: () -> Unit,
    onToggleChorusQuick: () -> Unit,
    onSelectOts: (Int) -> Unit,
    onToggleMemoryStore: () -> Unit,
    onStepRegSequence: (Int) -> Unit,
    onRegistrationPressed: (Int) -> Unit,
    onRegistrationLongPressed: (Int) -> Unit,
    onSelectRegBank: (Int) -> Unit,
    onToggleRegFreeze: () -> Unit,
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
    onSetReverbDepth: (Float) -> Unit,
    onSetChorusDepth: (Float) -> Unit,
    onSetEqBass: (Float) -> Unit,
    onSetEqMid: (Float) -> Unit,
    onSetEqTreble: (Float) -> Unit,
    onSetMasterTuneHz: (Float) -> Unit,
    onSetAudioBufferFrames: (Int) -> Unit,
    onCycleStyle: (Int) -> Unit,
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
            .background(ChassisVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val showSpeakers = maxWidth >= 680.dp
            val isCompactHeight = maxHeight < 380.dp
            val keyboardWeight = state.keyboardHeightRatio.coerceIn(0.36f, 0.54f)
            val consoleWeight = (1f - keyboardWeight).coerceAtLeast(0.44f)

            Column(modifier = Modifier.fillMaxSize()) {
                // ===============================================================
                // 1. TOP PSR-SX900 STATUS BAR (ETHIO YAMAHA + FLAG + TELEMETRY)
                // ===============================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompactHeight) 24.dp else 28.dp)
                        .background(Color(0xFF06080C))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Identity: Musical Emblem + ETHIO YAMAHA + Ethiopian Flag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PsrNeonBlueGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ETHIO YAMAHA",
                            color = Color.White,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompactHeight) 11.sp else 13.sp,
                            letterSpacing = 0.8.sp
                        )
                        EthioFlagBadge()
                        Text(
                            text = "PSR-ARRANGER",
                            color = SilkscreenMuted,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 8.sp
                        )
                    }

                    // Right Live Telemetry + Quick Utility Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "♩ ${state.tempoBpm}",
                            color = Color.White,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable(onClick = onTapTempo)
                        )
                        Text(
                            text = state.activeStyle.timeSignatureLabel,
                            color = SilkscreenWhite,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "M: ${state.measureNumber.toString().padStart(3, '0')}.${state.currentBeat}",
                            color = PsrNeonBlueGlow,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        SignalPeakMeter(peakLevel = state.peakMeterLevel)

                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SilkscreenWhite,
                            modifier = Modifier.size(16.dp).testTag("topbar_settings_icon").clickable { onOpenScreen(ArrangerScreen.SETTINGS) }
                        )
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Style and SET Library",
                            tint = SilkscreenWhite,
                            modifier = Modifier.size(16.dp).testTag("topbar_library_icon").clickable { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
                        )
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Church Worship Playlist",
                            tint = SilkscreenWhite,
                            modifier = Modifier.size(16.dp).clickable { onOpenScreen(ArrangerScreen.PLAYLIST) }
                        )
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mixer and DSP",
                            tint = SilkscreenWhite,
                            modifier = Modifier.size(16.dp).clickable { onOpenScreen(ArrangerScreen.MIXER) }
                        )
                    }
                }

                // ===============================================================
                // 2. PSR-SX900 BRUSHED ALUMINUM ARRANGER CONSOLE
                // ===============================================================
                Column(
                    modifier = Modifier
                        .weight(consoleWeight)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF1A1E27), ChassisPanel, ChassisDark))
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // --- UPPER CONSOLE ROW ---
                    Row(
                        modifier = Modifier.weight(0.68f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // A. LEFT SPEAKER GRILLE
                        if (showSpeakers) {
                            PerforatedSpeakerGrille(modifier = Modifier.weight(0.11f).fillMaxHeight())
                        }

                        // B. LEFT MASTER VOLUME HALO + 2x4 MODE MATRIX + BALANCE/MIXER/DIRECT ACCESS
                        Row(
                            modifier = Modifier
                                .weight(if (showSpeakers) 0.255f else 0.305f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(ChassisDark.copy(alpha = 0.85f))
                                .border(1.dp, ChassisBorder.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Neon-Blue Halo MASTER VOLUME Knob
                            Box(
                                modifier = Modifier
                                    .width(if (isCompactHeight) 48.dp else 56.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                NeonHaloMasterKnob(
                                    value = state.masterVolume,
                                    onValueChange = onSetMasterVolume,
                                    knobSize = if (isCompactHeight) 38.dp else 46.dp,
                                    tag = "knob_master_volume"
                                )
                            }

                            // 2x4 Hardware Matrix + Bottom Balance/Mixer/Direct Access
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    PsrHardwareButton(
                                        label = "SONG",
                                        isActive = state.activeScreen == ArrangerScreen.PLAYLIST,
                                        tag = "btn_song",
                                        fontSize = 7.5.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenScreen(ArrangerScreen.PLAYLIST) }
                                    )
                                    PsrHardwareButton(
                                        label = "STYLE",
                                        isActive = state.activeScreen == ArrangerScreen.STYLE_LIBRARY,
                                        tag = "btn_style",
                                        fontSize = 7.5.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
                                    )
                                    PsrHardwareButton(
                                        label = "VOICE",
                                        isActive = state.activeScreen == ArrangerScreen.VOICE_SELECT,
                                        tag = "btn_voice",
                                        fontSize = 7.5.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
                                    )
                                    PsrHardwareButton(
                                        label = "MIDI",
                                        isActive = state.connectedMidiDevices.isNotEmpty(),
                                        tag = "btn_midi",
                                        fontSize = 7.5.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenScreen(ArrangerScreen.SETTINGS) }
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    PsrHardwareButton(
                                        label = "UTILITY",
                                        isActive = state.activeScreen == ArrangerScreen.SETTINGS,
                                        tag = "btn_utility",
                                        fontSize = 7.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenScreen(ArrangerScreen.SETTINGS) }
                                    )
                                    PsrHardwareButton(
                                        label = "REGIST",
                                        isActive = state.isRegistrationFreezeEnabled,
                                        activeColor = LedEmerald,
                                        tag = "btn_regist",
                                        fontSize = 7.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onStepRegSequence(1) },
                                        onLongClick = { onOpenScreen(ArrangerScreen.PLAYLIST) }
                                    )
                                    PsrHardwareButton(
                                        label = "MEMORY",
                                        isActive = state.isMemoryStoreArmed,
                                        activeColor = LedCrimson,
                                        solidFillWhenActive = true,
                                        tag = "btn_memory",
                                        fontSize = 6.8.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = onToggleMemoryStore
                                    )
                                    PsrHardwareButton(
                                        label = "MENU",
                                        isActive = state.activeScreen == ArrangerScreen.MIXER,
                                        tag = "btn_left_menu",
                                        fontSize = 7.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenScreen(ArrangerScreen.MIXER) }
                                    )
                                }

                                // Bottom Knobs: BALANCE, MIXER (Style Vol), DIRECT ACCESS
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RotaryMetalKnob(
                                        label = "BALANCE",
                                        value = state.acmpBalance,
                                        onValueChange = onSetAcmpBalance,
                                        knobSize = if (isCompactHeight) 24.dp else 30.dp,
                                        tag = "knob_balance"
                                    )
                                    RotaryMetalKnob(
                                        label = "MIXER",
                                        value = state.styleVolume,
                                        onValueChange = onSetStyleVolume,
                                        knobSize = if (isCompactHeight) 24.dp else 30.dp,
                                        tag = "knob_style_vol"
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("DIRECT\nACCESS", color = SilkscreenMuted, fontSize = 6.sp, lineHeight = 7.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(2.dp))
                                        PsrHardwareButton(
                                            label = "⚡",
                                            isActive = false,
                                            tag = "btn_direct_access",
                                            height = 20.dp,
                                            modifier = Modifier.width(34.dp),
                                            onClick = { onOpenScreen(ArrangerScreen.MULTI_PAD_LOOPER) }
                                        )
                                    }
                                }
                            }
                        }

                        // C. CENTRAL PSR-SX900 COLOR TOUCHSCREEN LCD
                        CenterLcdDisplay(
                            state = state,
                            onOpenScreen = onOpenScreen,
                            onTapTempo = onTapTempo,
                            modifier = Modifier
                                .weight(if (showSpeakers) 0.31f else 0.38f)
                                .fillMaxHeight()
                        )

                        // D. CENTER-RIGHT EXIT/MENU + DATA ENTRY WHEEL + ENTER
                        Column(
                            modifier = Modifier
                                .width(if (isCompactHeight) 74.dp else 88.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(ChassisDark.copy(alpha = 0.85f))
                                .border(1.dp, ChassisBorder.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PsrHardwareButton(
                                    label = "EXIT",
                                    isActive = false,
                                    tag = "btn_exit",
                                    fontSize = 7.5.sp,
                                    height = 20.dp,
                                    modifier = Modifier.weight(1f),
                                    onClick = onExitToPerformance
                                )
                                PsrHardwareButton(
                                    label = "MENU",
                                    isActive = state.activeScreen != ArrangerScreen.PERFORMANCE,
                                    tag = "btn_right_menu",
                                    fontSize = 7.5.sp,
                                    height = 20.dp,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onOpenScreen(ArrangerScreen.MIXER) }
                                )
                            }

                            PsrDataWheelWithArrows(
                                onDeltaStep = onAdjustTempo,
                                onPrevStyle = { onCycleStyle(-1) },
                                onNextStyle = { onCycleStyle(1) },
                                dialSize = if (isCompactHeight) 42.dp else 54.dp,
                                tag = "data_entry_jog_dial"
                            )

                            PsrHardwareButton(
                                label = "ENTER",
                                isActive = false,
                                tag = "btn_enter",
                                fontSize = 7.5.sp,
                                height = 19.dp,
                                modifier = Modifier.width(46.dp),
                                onClick = { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) },
                                onLongClick = onResetTempo
                            )
                        }

                        // E. RIGHT PANEL LOCK + VOICE EFFECT + PART SELECT (LEFT/R1/R2) + HARMONY + DSP/A.FILL
                        Column(
                            modifier = Modifier
                                .weight(if (showSpeakers) 0.135f else 0.19f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(ChassisDark.copy(alpha = 0.85f))
                                .border(1.dp, ChassisBorder.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // PANEL LOCK
                            PsrHardwareButton(
                                label = if (state.isPanelLocked) "🔒 LOCKED" else "PANEL LOCK",
                                isActive = state.isPanelLocked,
                                activeColor = PsrActiveOrange,
                                solidFillWhenActive = true,
                                tag = "btn_panel_lock",
                                fontSize = 7.sp,
                                height = 20.dp,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onTogglePanelLock
                            )

                            // VOICE EFFECT (Reverb / Chorus quick toggles)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("VOICE EFFECT", color = SilkscreenMuted, fontSize = 6.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                                    PsrHardwareButton(
                                        label = "REV",
                                        isActive = state.reverbDepth > 0.05f,
                                        tag = "btn_quick_reverb",
                                        showLedBar = true,
                                        fontSize = 6.5.sp,
                                        height = 19.dp,
                                        modifier = Modifier.weight(1f),
                                        onClick = onToggleReverbQuick
                                    )
                                    PsrHardwareButton(
                                        label = "CHO",
                                        isActive = state.chorusDepth > 0.05f,
                                        tag = "btn_quick_chorus",
                                        showLedBar = true,
                                        fontSize = 6.5.sp,
                                        height = 19.dp,
                                        modifier = Modifier.weight(1f),
                                        onClick = onToggleChorusQuick
                                    )
                                }
                            }

                            // PART SELECT (LEFT / R1 / R2 / R3 — Tap to toggle, Long-Press to open Voice/Part Editor)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PART SELECT", color = SilkscreenMuted, fontSize = 6.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                                    PsrHardwareButton(
                                        label = "LEFT",
                                        isActive = state.isLeftVoiceEnabled,
                                        tag = "btn_part_left",
                                        fontSize = 6.sp,
                                        height = 20.dp,
                                        modifier = Modifier.weight(1.1f),
                                        onClick = { onTogglePartEnabled(KeyboardPartTarget.LEFT) },
                                        onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
                                    )
                                    PsrHardwareButton(
                                        label = "R1",
                                        isActive = state.isRight1Enabled,
                                        tag = "btn_part_right1",
                                        fontSize = 6.2.sp,
                                        height = 20.dp,
                                        modifier = Modifier.weight(0.95f),
                                        onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_1) },
                                        onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
                                    )
                                    PsrHardwareButton(
                                        label = "R2",
                                        isActive = state.isRight2Enabled,
                                        activeColor = PsrActiveOrange,
                                        tag = "btn_part_right2",
                                        fontSize = 6.2.sp,
                                        height = 20.dp,
                                        modifier = Modifier.weight(0.95f),
                                        onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_2) },
                                        onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
                                    )
                                    PsrHardwareButton(
                                        label = "R3",
                                        isActive = state.isRight3Enabled,
                                        activeColor = LedEmerald,
                                        tag = "btn_part_right3",
                                        fontSize = 6.2.sp,
                                        height = 20.dp,
                                        modifier = Modifier.weight(0.95f),
                                        onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_3) },
                                        onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
                                    )
                                }
                            }

                            // HARMONY / ARPEGGIO + DSP / AUTO FILL
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                                PsrHardwareButton(
                                    label = "HARM",
                                    isActive = state.isHarmonyEnabled,
                                    tag = "btn_harmony",
                                    showLedBar = true,
                                    fontSize = 6.5.sp,
                                    height = 20.dp,
                                    modifier = Modifier.weight(1f),
                                    onClick = onToggleHarmony
                                )
                                PsrHardwareButton(
                                    label = "DSP",
                                    isActive = state.isDspEnabled,
                                    tag = "btn_dsp",
                                    showLedBar = true,
                                    fontSize = 6.5.sp,
                                    height = 20.dp,
                                    modifier = Modifier.weight(1f),
                                    onClick = onToggleDsp
                                )
                                PsrHardwareButton(
                                    label = "A.FILL",
                                    isActive = state.isAutoFillEnabled,
                                    activeColor = PsrActiveOrange,
                                    tag = "btn_auto_fill",
                                    showLedBar = true,
                                    fontSize = 6.5.sp,
                                    height = 20.dp,
                                    modifier = Modifier.weight(1f),
                                    onClick = onAutoFill
                                )
                            }
                        }

                        // F. RIGHT SPEAKER GRILLE
                        if (showSpeakers) {
                            PerforatedSpeakerGrille(modifier = Modifier.weight(0.11f).fillMaxHeight())
                        }
                    }

                    // --- LOWER FULL-WIDTH PSR-SX900 ARRANGER STRIP ---
                    Row(
                        modifier = Modifier
                            .weight(0.32f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0D1017))
                            .border(1.dp, ChassisBorder.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 1. STYLE CONTROL (INTRO 1-3, MAIN A-D, BREAK, ENDING 1-3, SYNC START/STOP, START/STOP, FADE, TAP)
                        Column(
                            modifier = Modifier.weight(15.2f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            PsrStripSectionHeader(title = "STYLE CONTROL", accentColor = PsrNeonBlue)
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // INTRO 1, 2, 3
                                listOf(ArrangerSection.INTRO_1 to "1", ArrangerSection.INTRO_2 to "2", ArrangerSection.INTRO_3 to "3").forEach { (sec, lbl) ->
                                    PsrStripKey(
                                        label = lbl,
                                        topLabel = if (lbl == "2") "INTRO" else "",
                                        isActive = state.activeSection == sec,
                                        isQueued = state.queuedSection == sec,
                                        activeColor = PsrNeonBlue,
                                        tag = "btn_${sec.name.lowercase()}",
                                        modifier = Modifier.weight(0.85f),
                                        onClick = { onIntro(sec) }
                                    )
                                }
                                Spacer(Modifier.width(2.dp))

                                // MAIN VARIATION A, B, C, D (Active glows SOLID PSR ORANGE!)
                                listOf(
                                    ArrangerSection.MAIN_A to "A",
                                    ArrangerSection.MAIN_B to "B",
                                    ArrangerSection.MAIN_C to "C",
                                    ArrangerSection.MAIN_D to "D"
                                ).forEach { (sec, lbl) ->
                                    val isFill = state.activeSection == sec.correspondingFill()
                                    PsrStripKey(
                                        label = lbl,
                                        topLabel = if (lbl == "B") "MAIN" else if (lbl == "C") "VAR" else "",
                                        isActive = state.activeSection == sec || isFill,
                                        isQueued = state.queuedSection == sec,
                                        activeColor = PsrActiveOrange,
                                        solidActiveFill = true,
                                        tag = "btn_${sec.name.lowercase()}",
                                        modifier = Modifier.weight(0.95f),
                                        onClick = { onMainVariation(sec) }
                                    )
                                }
                                Spacer(Modifier.width(2.dp))

                                // BREAK
                                PsrStripKey(
                                    label = "",
                                    topLabel = "BREAK",
                                    showTopBar = true,
                                    isActive = state.activeSection == ArrangerSection.BREAK,
                                    activeColor = PsrActiveOrange,
                                    tag = "btn_break",
                                    modifier = Modifier.weight(1.0f),
                                    onClick = onBreak
                                )
                                Spacer(Modifier.width(2.dp))

                                // ENDING 1, 2, 3
                                listOf(ArrangerSection.ENDING_1 to "1", ArrangerSection.ENDING_2 to "2", ArrangerSection.ENDING_3 to "3").forEach { (sec, lbl) ->
                                    PsrStripKey(
                                        label = lbl,
                                        topLabel = if (lbl == "2") "ENDING" else "",
                                        isActive = state.activeSection == sec,
                                        isQueued = state.queuedSection == sec,
                                        activeColor = PsrNeonBlue,
                                        tag = "btn_${sec.name.lowercase()}",
                                        modifier = Modifier.weight(0.85f),
                                        onClick = { onEnding(sec) }
                                    )
                                }
                                Spacer(Modifier.width(3.dp))

                                // SYNC START, SYNC STOP, START/STOP (vivid neon blue!), FADE IN/OUT, TAP TEMPO
                                PsrStripKey(
                                    label = "",
                                    topLabel = "SYNC\nSTART",
                                    showTopBar = true,
                                    isActive = state.isSyncStartArmed,
                                    activeColor = PsrNeonBlue,
                                    tag = "btn_sync_start",
                                    modifier = Modifier.weight(1.05f),
                                    onClick = onSyncStart
                                )
                                PsrStripKey(
                                    label = "",
                                    topLabel = "SYNC\nSTOP",
                                    showTopBar = true,
                                    isActive = state.isSyncStopArmed,
                                    activeColor = PsrNeonBlue,
                                    tag = "btn_sync_stop",
                                    modifier = Modifier.weight(1.05f),
                                    onClick = onSyncStop
                                )
                                PsrStripKey(
                                    label = if (state.isPlaying) "■" else "▶/❚❚",
                                    topLabel = "START\nSTOP",
                                    isActive = true,
                                    activeColor = if (state.isPlaying && state.currentBeat == 1) PsrActiveOrange else PsrNeonBlue,
                                    solidActiveFill = true,
                                    fontSize = 9.sp,
                                    tag = "btn_start_stop",
                                    modifier = Modifier.weight(1.25f),
                                    onClick = onStartStop
                                )
                                PsrStripKey(
                                    label = "",
                                    topLabel = "FADE\nIN/OUT",
                                    showTopBar = true,
                                    isActive = state.isFadeActive,
                                    activeColor = PsrActiveOrange,
                                    tag = "btn_fade_in_out",
                                    modifier = Modifier.weight(1.0f),
                                    onClick = onFadeInOut
                                )
                                PsrStripKey(
                                    label = "",
                                    topLabel = "TAP\nTEMPO",
                                    showTopBar = true,
                                    isActive = state.isPlaying && state.currentBeat == 1,
                                    activeColor = LedAmber,
                                    tag = "btn_tap_tempo",
                                    modifier = Modifier.weight(1.0f),
                                    onClick = onTapTempo,
                                    onLongClick = onResetTempo
                                )
                            }
                        }

                        // 2. REGISTRATION MEMORY 1–8 (Active glows vivid neon blue!)
                        Column(
                            modifier = Modifier.weight(7.2f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            PsrStripSectionHeader(
                                title = if (state.isMemoryStoreArmed) "SAVE REGIST (BANK ${state.activeRegistrationBank})" else "REGISTRATION MEMORY",
                                accentColor = if (state.isMemoryStoreArmed) LedCrimson else PsrNeonBlue
                            )
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                state.registrationSlots.forEach { slot ->
                                    val isSelected = state.activeRegistrationSlot == slot.slotNumber
                                    PsrStripKey(
                                        label = "${slot.slotNumber}",
                                        isActive = isSelected || slot.isPopulated || state.isMemoryStoreArmed,
                                        activeColor = when {
                                            state.isMemoryStoreArmed -> LedCrimson
                                            isSelected -> PsrNeonBlue
                                            else -> Color(0xFF425A82)
                                        },
                                        solidActiveFill = isSelected,
                                        tag = "btn_reg_${slot.slotNumber}",
                                        fontSize = 9.5.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onRegistrationPressed(slot.slotNumber) },
                                        onLongClick = { onRegistrationLongPressed(slot.slotNumber) }
                                    )
                                }
                            }
                        }

                        // 3. ONE TOUCH SETTING 1–4 (Glowing blue LEDs on far right!)
                        Column(
                            modifier = Modifier.weight(3.8f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            PsrStripSectionHeader(title = "ONE TOUCH SETTING", accentColor = PsrNeonBlue)
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                            ) {
                                for (slot in 1..4) {
                                    PsrStripKey(
                                        label = "$slot",
                                        showTopBar = true,
                                        isActive = state.activeOtsSlot == slot,
                                        activeColor = PsrNeonBlue,
                                        tag = "btn_ots_$slot",
                                        fontSize = 9.5.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onSelectOts(slot) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ===============================================================
                // 3. REALISTIC PLAYABLE PIANO KEYBOARD (49/61/76/88 SCROLLABLE)
                // ===============================================================
                PlayablePianoKeyboard(
                    keyboardLayout = state.keyboardLayout,
                    startMidiNote = state.keyboardStartMidi,
                    visibleWhiteKeyCount = state.visibleWhiteKeyCount,
                    splitNoteMidi = state.splitNoteMidi,
                    isSplitEnabled = state.isSplitEnabled,
                    showKeyLabels = state.showKeyLabels,
                    showScrollbar = state.showKeyboardScrollbar,
                    isSustainEnabled = state.isSustainEnabled,
                    pitchBendSemitones = state.pitchBendSemitones,
                    pressedMidiNotes = state.pressedMidiNotes,
                    onScrollToMidi = onScrollKeyboardToMidi,
                    onShiftOctaveView = onShiftKeyboardOctaveView,
                    onToggleSustain = onToggleSustain,
                    onPitchBend = onPitchBend,
                    onKeyDown = onPianoKeyDown,
                    onKeyUp = onPianoKeyUp,
                    modifier = Modifier
                        .weight(keyboardWeight)
                        .fillMaxWidth()
                )
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
                onImportYamahaSetClick = onImportYamahaSetClick,
                onImportOrgSetClick = onImportOrgSetClick,
                onImportSingleFileClick = onImportSingleFileClick,
                onSelectStyle = onSelectStyle,
                onRemoveSource = onRemoveSource,
                onRemoveStyle = onRemoveStyle,
                onSelectPartVoice = onSelectPartVoice,
                onTogglePartEnabled = onTogglePartEnabled,
                onSetPartVolume = onSetPartVolume,
                onSetPartPan = onSetPartPan,
                onSetPartOctave = onSetPartOctave,
                onSetPartNoteRange = onSetPartNoteRange,
                onSetChannelVolume = onSetChannelVolume,
                onSetChannelPan = onSetChannelPan,
                onToggleChannelMute = onToggleChannelMute,
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
                onSelectRegBank = onSelectRegBank,
                onRecallRegSlot = onRegistrationPressed,
                onSaveRegSlot = onRegistrationLongPressed,
                onStepRegSequence = onStepRegSequence,
                onToggleRegFreeze = onToggleRegFreeze,
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
