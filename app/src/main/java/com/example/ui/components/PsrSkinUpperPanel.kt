package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerSection
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.KeyboardPartTarget
import com.example.ui.theme.ChampagneGoldBright
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.LedAmber
import com.example.ui.theme.LedCrimson
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrNeonBlue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val PSR_SKIN_WIDTH_PX = 1780f
const val PSR_SKIN_HEIGHT_PX = 422f
const val PSR_SKIN_ASPECT_RATIO = PSR_SKIN_WIDTH_PX / PSR_SKIN_HEIGHT_PX

private enum class PanelDialTarget(val label: String) {
    TEMPO("TEMPO"),
    OCTAVE("OCTAVE"),
    TRANSPOSE("TRANS")
}

/**
 * Photorealistic PSR-SX900 upper control-panel skin (`psr_sx900_panel_skin.jpg`, 1780x422)
 * with transparent pixel-aligned touch regions, rotary drag knobs, data jog dial,
 * live Center LCD overlay, and subtle active LED indicators wired directly to ArrangerViewModel.
 */
@Composable
fun PsrSkinUpperPanel(
    state: ArrangerUiState,
    onOpenScreen: (ArrangerScreen) -> Unit,
    onExitToPerformance: () -> Unit,
    onTogglePanelLock: () -> Unit,
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
    onSelectOts: (Int) -> Unit,
    onToggleOtsLink: () -> Unit,
    onToggleMemoryStore: () -> Unit,
    onStepRegSequence: (Int) -> Unit,
    onRegistrationPressed: (Int) -> Unit,
    onRegistrationLongPressed: (Int) -> Unit,
    onMultiPadPressed: (Int) -> Unit,
    onStopAllMultiPads: () -> Unit,
    onToggleChannelMute: (AccompPart) -> Unit,
    onSetMasterVolume: (Float) -> Unit,
    onSetAcmpBalance: (Float) -> Unit,
    onSetStyleVolume: (Float) -> Unit,
    onCycleStyle: (Int) -> Unit,
    onPitchBend: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialTarget by remember { mutableStateOf(PanelDialTarget.TEMPO) }

    val stepDialTarget: (Int) -> Unit = { delta ->
        when (dialTarget) {
            PanelDialTarget.TEMPO -> onAdjustTempo(delta)
            PanelDialTarget.OCTAVE -> onAdjustOctave(delta)
            PanelDialTarget.TRANSPOSE -> onAdjustTranspose(delta)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(PSR_SKIN_ASPECT_RATIO)
    ) {
        val w = maxWidth
        val h = maxHeight

        fun xDp(px: Float): Dp = w * (px / PSR_SKIN_WIDTH_PX)
        fun yDp(px: Float): Dp = h * (px / PSR_SKIN_HEIGHT_PX)

        // 1. Original PSR-SX900 Skin Artwork (zero cropping/distortion)
        Image(
            painter = painterResource(id = R.drawable.psr_sx900_panel_skin),
            contentDescription = "PSR-SX900 Control Panel",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Center Live LCD Display inside skin bezel (738..1052, 69..236)
        CenterLcdDisplay(
            state = state,
            onOpenScreen = onOpenScreen,
            onTapTempo = onTapTempo,
            dialTargetLabel = dialTarget.label,
            modifier = Modifier
                .offset(x = xDp(738f), y = yDp(69f))
                .size(width = xDp(314f), height = yDp(167f))
        )

        // 3. Left Power / Panel Lock + Rotary Knobs (MASTER, BALANCE, CHORD/DRUM)
        SkinButtonHitbox(
            cx = 386f, cy = 53f, bw = 48f, bh = 46f,
            xDp = ::xDp, yDp = ::yDp,
            description = if (state.isPanelLocked) "Unlock Panel" else "Lock Panel",
            tag = "btn_panel_lock",
            isActive = state.isPanelLocked,
            ledColor = PsrActiveOrange,
            shape = CircleShape,
            onClick = onTogglePanelLock
        )

        SkinRotaryKnobOverlay(
            cx = 386f, cy = 152f, diameterPx = 80f,
            xDp = ::xDp, yDp = ::yDp,
            value = state.masterVolume,
            onValueChange = onSetMasterVolume,
            accentColor = ElectricBlueGlow,
            description = "Master Volume",
            tag = "knob_master_volume"
        )

        SkinRotaryKnobOverlay(
            cx = 386f, cy = 244f, diameterPx = 52f,
            xDp = ::xDp, yDp = ::yDp,
            value = state.acmpBalance,
            onValueChange = onSetAcmpBalance,
            accentColor = PsrNeonBlue,
            description = "Accompaniment Balance",
            tag = "knob_balance"
        )

        SkinRotaryKnobOverlay(
            cx = 487f, cy = 244f, diameterPx = 52f,
            xDp = ::xDp, yDp = ::yDp,
            value = state.styleVolume,
            onValueChange = onSetStyleVolume,
            accentColor = ChampagneGoldBright,
            description = "Style Volume",
            tag = "knob_style_vol"
        )

        // 4. Left 5-Column Matrix (x = 464, 520, 575, 630, 685)
        // Row 1 (y = 73): Transport / Style Select
        SkinButtonHitbox(464f, 73f, 48f, 34f, ::xDp, ::yDp, "Previous Style", "btn_style_prev", onClick = { onCycleStyle(-1) })
        SkinButtonHitbox(520f, 73f, 48f, 34f, ::xDp, ::yDp, "Next Style", "btn_style_next", onClick = { onCycleStyle(1) })
        SkinButtonHitbox(575f, 73f, 48f, 34f, ::xDp, ::yDp, "Reset Tempo", "btn_player_reset", onClick = onResetTempo)
        SkinButtonHitbox(
            630f, 73f, 48f, 34f, ::xDp, ::yDp, "Play or Stop Style", "btn_player_play_stop",
            isActive = state.isPlaying, ledColor = LedEmerald, onClick = onStartStop
        )
        SkinButtonHitbox(
            685f, 73f, 48f, 34f, ::xDp, ::yDp, "Style Library", "btn_style",
            isActive = state.activeScreen == ArrangerScreen.STYLE_LIBRARY, ledColor = PsrActiveOrange,
            onClick = { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
        )

        // Row 2 (y = 118): Channels & Parts
        SkinButtonHitbox(
            464f, 118f, 48f, 34f, ::xDp, ::yDp, "Channel On Off", "btn_channel_on_off",
            isActive = state.activeScreen == ArrangerScreen.CHANNEL_ON_OFF, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.CHANNEL_ON_OFF) }
        )
        SkinButtonHitbox(
            520f, 118f, 48f, 34f, ::xDp, ::yDp, "Mixer and Effects", "btn_mixer_open",
            isActive = state.activeScreen == ArrangerScreen.MIXER, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.MIXER) }
        )
        val drumsMuted = state.mixerChannels.find { it.part == AccompPart.DRUMS }?.isMuted == true
        SkinButtonHitbox(
            575f, 118f, 48f, 34f, ::xDp, ::yDp, "Mute Drums", "btn_drum_mute",
            isActive = drumsMuted, ledColor = LedCrimson,
            onClick = { onToggleChannelMute(AccompPart.DRUMS) }
        )
        SkinButtonHitbox(
            630f, 118f, 48f, 34f, ::xDp, ::yDp, "Left Part Toggle", "btn_part_left",
            isActive = state.isLeftVoiceEnabled, ledColor = ElectricBlueGlow,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.LEFT) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_PART_SETUP) }
        )
        SkinButtonHitbox(
            685f, 118f, 48f, 34f, ::xDp, ::yDp, "ACMP On Off", "btn_acmp_toggle",
            isActive = state.isAcmpEnabled, ledColor = LedEmerald,
            onClick = onToggleAcmp
        )

        // Row 3 (y = 168): TEMPO, OCTAVE, TRAN, -, +
        SkinButtonHitbox(
            464f, 168f, 48f, 34f, ::xDp, ::yDp, "Tap Tempo and Select Tempo Dial", "btn_tap_tempo",
            isActive = dialTarget == PanelDialTarget.TEMPO, ledColor = ChampagneGoldBright,
            onClick = {
                if (dialTarget == PanelDialTarget.TEMPO) onTapTempo()
                else dialTarget = PanelDialTarget.TEMPO
            },
            onLongClick = onResetTempo
        )
        SkinButtonHitbox(
            520f, 168f, 48f, 34f, ::xDp, ::yDp, "Select Octave Dial", "btn_octave_mode",
            isActive = dialTarget == PanelDialTarget.OCTAVE || state.octaveShift != 0, ledColor = ElectricBlueGlow,
            onClick = { dialTarget = PanelDialTarget.OCTAVE }
        )
        SkinButtonHitbox(
            575f, 168f, 48f, 34f, ::xDp, ::yDp, "Select Transpose Dial", "btn_transpose_mode",
            isActive = dialTarget == PanelDialTarget.TRANSPOSE || state.transposeSemitones != 0, ledColor = PsrActiveOrange,
            onClick = { dialTarget = PanelDialTarget.TRANSPOSE }
        )
        SkinButtonHitbox(630f, 168f, 48f, 34f, ::xDp, ::yDp, "Decrement", "btn_dial_minus", onClick = { stepDialTarget(-1) })
        SkinButtonHitbox(685f, 168f, 48f, 34f, ::xDp, ::yDp, "Increment", "btn_dial_plus", onClick = { stepDialTarget(1) })

        // Row 4 (y = 220): AUTO FILL, RESET TE
        SkinButtonHitbox(
            630f, 220f, 48f, 34f, ::xDp, ::yDp, "Auto Fill On Off", "btn_auto_fill",
            isActive = state.isAutoFillEnabled, ledColor = PsrActiveOrange, onClick = onAutoFill
        )
        SkinButtonHitbox(685f, 220f, 48f, 34f, ::xDp, ::yDp, "Reset Tempo", "btn_reset_tempo", onClick = onResetTempo)

        // Row 5 (y = 266): FADE, SYN L (Sync Start), SYN R (Sync Stop)
        SkinButtonHitbox(
            575f, 266f, 48f, 34f, ::xDp, ::yDp, "Fade In Out", "btn_fade_in_out",
            isActive = state.isFadeActive, ledColor = PsrActiveOrange, onClick = onFadeInOut
        )
        SkinButtonHitbox(
            630f, 266f, 48f, 34f, ::xDp, ::yDp, "Sync Start", "btn_sync_start",
            isActive = state.isSyncStartArmed, ledColor = ElectricBlueGlow, onClick = onSyncStart
        )
        SkinButtonHitbox(
            685f, 266f, 48f, 34f, ::xDp, ::yDp, "Sync Stop", "btn_sync_stop",
            isActive = state.isSyncStopArmed, ledColor = ElectricBlueGlow, onClick = onSyncStop
        )

        // 5. Under LCD: KEYBOARD SET 1–5 (x = 780, 837, 894, 951, 1008; y = 269)
        listOf(780f, 837f, 894f, 951f).forEachIndexed { idx, cx ->
            val slot = idx + 1
            SkinButtonHitbox(
                cx = cx, cy = 269f, bw = 48f, bh = 34f,
                xDp = ::xDp, yDp = ::yDp,
                description = "One Touch Setting $slot",
                tag = "btn_ots_$slot",
                isActive = state.activeOtsSlot == slot,
                ledColor = PsrActiveOrange,
                onClick = { onSelectOts(slot) }
            )
        }
        SkinButtonHitbox(
            cx = 1008f, cy = 269f, bw = 48f, bh = 34f,
            xDp = ::xDp, yDp = ::yDp,
            description = "Voice Select and OTS Link",
            tag = "btn_voice",
            isActive = state.isOtsLinkEnabled || state.activeScreen == ArrangerScreen.VOICE_SELECT,
            ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) },
            onLongClick = onToggleOtsLink
        )

        // 6. Center-Right: MENU, EXIT, JOG WHEEL, <, >, Q.T., STKS
        SkinButtonHitbox(
            1106f, 73f, 48f, 34f, ::xDp, ::yDp, "Main Menu", "btn_right_menu",
            isActive = state.activeScreen == ArrangerScreen.MAIN_MENU, ledColor = ChampagneGoldBright,
            onClick = { onOpenScreen(ArrangerScreen.MAIN_MENU) }
        )
        SkinButtonHitbox(1161f, 73f, 48f, 34f, ::xDp, ::yDp, "Exit to Home", "btn_exit", onClick = onExitToPerformance)

        // Jog Wheel Overlay (cx = 1132, cy = 148, d = 84)
        var jogAccum by remember { mutableFloatStateOf(0f) }
        Box(
            modifier = Modifier
                .offset(x = xDp(1132f - 42f), y = yDp(148f - 42f))
                .size(width = xDp(84f), height = yDp(84f))
                .clip(CircleShape)
                .testTag("data_entry_jog_dial")
                .semantics { contentDescription = "Data Entry Jog Dial (${dialTarget.label})" }
                .pointerInput(dialTarget) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = (-dragAmount.y + dragAmount.x) * 0.5f
                        jogAccum += delta
                        while (jogAccum >= 8f) {
                            jogAccum -= 8f
                            stepDialTarget(1)
                        }
                        while (jogAccum <= -8f) {
                            jogAccum += 8f
                            stepDialTarget(-1)
                        }
                    }
                }
        )

        SkinButtonHitbox(
            1101f, 222f, 40f, 32f, ::xDp, ::yDp, "Previous Style or Sequence", "btn_nav_prev",
            onClick = { onCycleStyle(-1) }, onLongClick = { onStepRegSequence(-1) }
        )
        SkinButtonHitbox(
            1161f, 222f, 40f, 32f, ::xDp, ::yDp, "Next Style or Sequence", "btn_nav_next",
            onClick = { onCycleStyle(1) }, onLongClick = { onStepRegSequence(1) }
        )
        SkinButtonHitbox(
            1101f, 266f, 46f, 32f, ::xDp, ::yDp, "Worship Playlist", "btn_song",
            isActive = state.activeScreen == ArrangerScreen.PLAYLIST, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.PLAYLIST) }
        )
        SkinButtonHitbox(
            1158f, 266f, 42f, 32f, ::xDp, ::yDp, "Multi Pad and Chord Looper", "btn_direct_access",
            isActive = state.activeScreen == ArrangerScreen.MULTI_PAD_LOOPER || state.isChordLooperPlaying, ledColor = PsrActiveOrange,
            onClick = { onOpenScreen(ArrangerScreen.MULTI_PAD_LOOPER) }
        )

        // 7. Right 4-Column Matrix (x = 1237, 1291, 1344, 1398)
        // Row 1 (y = 72)
        SkinButtonHitbox(
            1237f, 72f, 46f, 34f, ::xDp, ::yDp, "Memory Store or Song Recording", "btn_memory",
            isActive = state.isMemoryStoreArmed, ledColor = LedCrimson,
            onClick = onToggleMemoryStore,
            onLongClick = { onOpenScreen(ArrangerScreen.SONG_RECORDING) }
        )
        SkinButtonHitbox(
            1291f, 72f, 46f, 34f, ::xDp, ::yDp, "Utility Settings", "btn_utility",
            isActive = state.activeScreen == ArrangerScreen.SETTINGS, ledColor = ChampagneGoldBright,
            onClick = { onOpenScreen(ArrangerScreen.SETTINGS) }
        )
        SkinButtonHitbox(
            1344f, 72f, 46f, 34f, ::xDp, ::yDp, "Style Settings and Library", "btn_style_settings",
            isActive = state.activeScreen == ArrangerScreen.STYLE_SETTINGS, ledColor = PsrActiveOrange,
            onClick = { onOpenScreen(ArrangerScreen.STYLE_SETTINGS) },
            onLongClick = { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
        )
        SkinButtonHitbox(
            1398f, 72f, 46f, 34f, ::xDp, ::yDp, "Mixer and Effects", "btn_left_menu",
            isActive = state.activeScreen == ArrangerScreen.MIXER, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.MIXER) }
        )

        // Row 2 (y = 116)
        SkinButtonHitbox(
            1237f, 116f, 46f, 34f, ::xDp, ::yDp, "Split and Fingering", "btn_split_fingering",
            isActive = state.activeScreen == ArrangerScreen.SPLIT_FINGERING, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.SPLIT_FINGERING) }
        )
        SkinButtonHitbox(
            1291f, 116f, 46f, 34f, ::xDp, ::yDp, "Registration Memory", "btn_regist",
            isActive = state.isRegistrationFreezeEnabled || state.activeScreen == ArrangerScreen.REGISTRATION_MEMORY,
            ledColor = LedEmerald,
            onClick = { onOpenScreen(ArrangerScreen.REGISTRATION_MEMORY) },
            onLongClick = { onOpenScreen(ArrangerScreen.REGISTRATION_FREEZE) }
        )
        SkinButtonHitbox(
            1344f, 116f, 46f, 34f, ::xDp, ::yDp, "MIDI Settings", "btn_midi",
            isActive = state.connectedMidiDevices.isNotEmpty() || state.activeScreen == ArrangerScreen.MIDI_SETTINGS,
            ledColor = LedEmerald,
            onClick = { onOpenScreen(ArrangerScreen.MIDI_SETTINGS) }
        )
        SkinButtonHitbox(
            1398f, 116f, 46f, 34f, ::xDp, ::yDp, "Metronome", "btn_metronome",
            isActive = state.isMetronomeEnabled, ledColor = ChampagneGoldBright,
            onClick = onToggleMetronome
        )

        // Row 3 (y = 162)
        SkinButtonHitbox(
            1344f, 162f, 46f, 34f, ::xDp, ::yDp, "Sustain Pedal", "btn_pedal_sustain",
            isActive = state.isSustainEnabled, ledColor = ElectricBlueGlow,
            onClick = onToggleSustain
        )
        SkinButtonHitbox(
            1398f, 162f, 46f, 34f, ::xDp, ::yDp, "Split Keyboard", "btn_split_toggle",
            isActive = state.isSplitEnabled, ledColor = PsrActiveOrange,
            onClick = onToggleSplit,
            onLongClick = { onOpenScreen(ArrangerScreen.SPLIT_FINGERING) }
        )

        // Row 4 (y = 208): TEMPO L, DNC 1 (R1), DNC 2 (R2), DNC 3 (R3)
        SkinButtonHitbox(
            1237f, 208f, 46f, 34f, ::xDp, ::yDp, "Tempo Lock", "btn_tempo_lock",
            isActive = state.isTempoLocked, ledColor = ChampagneGoldBright,
            onClick = onToggleTempoLock
        )
        SkinButtonHitbox(
            1291f, 208f, 46f, 34f, ::xDp, ::yDp, "Right 1 Part Toggle", "btn_part_right1",
            isActive = state.isRight1Enabled, ledColor = ElectricBlueGlow,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_1) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
        )
        SkinButtonHitbox(
            1344f, 208f, 46f, 34f, ::xDp, ::yDp, "Right 2 Part Toggle", "btn_part_right2",
            isActive = state.isRight2Enabled, ledColor = PsrActiveOrange,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_2) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
        )
        SkinButtonHitbox(
            1398f, 208f, 46f, 34f, ::xDp, ::yDp, "Right 3 Part Toggle", "btn_part_right3",
            isActive = state.isRight3Enabled, ledColor = LedEmerald,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_3) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
        )

        // 8. Lower Strip (y = 322..396)
        // Pitch Bend Switch (x = 106..162, y = 336..390)
        Box(
            modifier = Modifier
                .offset(x = xDp(104f), y = yDp(334f))
                .size(width = xDp(60f), height = yDp(56f))
                .clip(RoundedCornerShape(6.dp))
                .then(
                    if (state.pitchBendSemitones != 0f)
                        Modifier.border(1.5.dp, PsrActiveOrange, RoundedCornerShape(6.dp))
                    else Modifier
                )
                .testTag("skin_pitch_switch")
                .semantics { contentDescription = "Pitch Bend Switch" }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { onPitchBend(0f) },
                        onDragCancel = { onPitchBend(0f) },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            onPitchBend((state.pitchBendSemitones + dragAmount / 18f).coerceIn(-2f, 2f))
                        }
                    )
                }
        )

        // INTRO I, II, III
        listOf(
            247f to ArrangerSection.INTRO_1,
            295f to ArrangerSection.INTRO_2,
            345f to ArrangerSection.INTRO_3
        ).forEach { (cx, sec) ->
            SkinStripKeyHitbox(
                cx = cx, wPx = 45f, xDp = ::xDp, yDp = ::yDp,
                description = sec.displayLabel,
                tag = "btn_${sec.name.lowercase()}",
                isActive = state.activeSection == sec,
                isQueued = state.queuedSection == sec,
                activeColor = PsrNeonBlue,
                onClick = { onIntro(sec) }
            )
        }

        // VARIATION A, B, C, D
        listOf(
            399f to ArrangerSection.MAIN_A,
            447f to ArrangerSection.MAIN_B,
            495f to ArrangerSection.MAIN_C,
            544f to ArrangerSection.MAIN_D
        ).forEach { (cx, sec) ->
            val isFill = state.activeSection == sec.correspondingFill()
            SkinStripKeyHitbox(
                cx = cx, wPx = 45f, xDp = ::xDp, yDp = ::yDp,
                description = sec.displayLabel,
                tag = "btn_${sec.name.lowercase()}",
                isActive = state.activeSection == sec || isFill,
                isQueued = state.queuedSection == sec,
                activeColor = PsrActiveOrange,
                onClick = { onMainVariation(sec) }
            )
        }

        // BREAK
        SkinStripKeyHitbox(
            cx = 597f, wPx = 47f, xDp = ::xDp, yDp = ::yDp,
            description = "Break",
            tag = "btn_break",
            isActive = state.activeSection == ArrangerSection.BREAK,
            activeColor = PsrActiveOrange,
            onClick = onBreak
        )

        // START / STOP
        SkinStripKeyHitbox(
            cx = 657f, wPx = 58f, xDp = ::xDp, yDp = ::yDp,
            description = "Start or Stop Style",
            tag = "btn_start_stop",
            isActive = state.isPlaying,
            activeColor = if (state.isPlaying && state.currentBeat == 1) PsrActiveOrange else ElectricBlueGlow,
            onClick = onStartStop
        )

        // FILL 1, 2, 3, 4 (maps directly to triggering fill into MAIN A-D)
        listOf(
            719f to ArrangerSection.MAIN_A,
            763f to ArrangerSection.MAIN_B,
            812f to ArrangerSection.MAIN_C,
            859f to ArrangerSection.MAIN_D
        ).forEachIndexed { idx, (cx, targetMain) ->
            val fillSec = targetMain.correspondingFill()
            SkinStripKeyHitbox(
                cx = cx, wPx = 44f, xDp = ::xDp, yDp = ::yDp,
                description = "Fill ${idx + 1}",
                tag = "btn_fill_${idx + 1}",
                isActive = state.activeSection == fillSec,
                activeColor = PsrActiveOrange,
                onClick = { onMainVariation(targetMain) }
            )
        }

        // ENDING I, II, III
        listOf(
            915f to ArrangerSection.ENDING_1,
            963f to ArrangerSection.ENDING_2,
            1012f to ArrangerSection.ENDING_3
        ).forEach { (cx, sec) ->
            SkinStripKeyHitbox(
                cx = cx, wPx = 45f, xDp = ::xDp, yDp = ::yDp,
                description = sec.displayLabel,
                tag = "btn_${sec.name.lowercase()}",
                isActive = state.activeSection == sec,
                isQueued = state.queuedSection == sec,
                activeColor = PsrNeonBlue,
                onClick = { onEnding(sec) }
            )
        }

        // REGISTRATION MEMORY 1..8
        val regCenters = listOf(1079f, 1129f, 1178f, 1231f, 1281f, 1330f, 1383f, 1432f)
        regCenters.forEachIndexed { idx, cx ->
            val slotNum = idx + 1
            val slotUi = state.registrationSlots.getOrNull(idx)
            val isSelected = state.activeRegistrationSlot == slotNum
            val isPopulated = slotUi?.isPopulated == true
            SkinStripKeyHitbox(
                cx = cx, wPx = 46f, xDp = ::xDp, yDp = ::yDp,
                description = "Registration Memory $slotNum",
                tag = "btn_reg_$slotNum",
                isActive = isSelected || state.isMemoryStoreArmed || isPopulated,
                activeColor = when {
                    state.isMemoryStoreArmed -> LedCrimson
                    isSelected -> ElectricBlueGlow
                    else -> PsrNeonBlue.copy(alpha = 0.45f)
                },
                onClick = { onRegistrationPressed(slotNum) },
                onLongClick = { onRegistrationLongPressed(slotNum) }
            )
        }

        // MULTI PADS 1..4
        listOf(1497f, 1545f, 1593f, 1642f).forEachIndexed { idx, cx ->
            val padSlot = idx + 1
            SkinStripKeyHitbox(
                cx = cx, wPx = 45f, xDp = ::xDp, yDp = ::yDp,
                description = "Multi Pad $padSlot",
                tag = "btn_pad_$padSlot",
                isActive = padSlot in state.activeMultiPadSlots,
                activeColor = PsrActiveOrange,
                onClick = { onMultiPadPressed(padSlot) },
                onLongClick = onStopAllMultiPads
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SkinButtonHitbox(
    cx: Float,
    cy: Float,
    bw: Float,
    bh: Float,
    xDp: (Float) -> Dp,
    yDp: (Float) -> Dp,
    description: String,
    tag: String,
    isActive: Boolean = false,
    ledColor: Color = ElectricBlueGlow,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp),
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .offset(x = xDp(cx - bw * 0.5f), y = yDp(cy - bh * 0.5f))
            .size(width = xDp(bw), height = yDp(bh))
            .clip(shape)
            .then(
                if (isActive) {
                    Modifier
                        .background(ledColor.copy(alpha = 0.14f))
                        .border(1.dp, ledColor.copy(alpha = 0.78f), shape)
                } else {
                    Modifier
                }
            )
            .testTag(tag)
            .semantics { contentDescription = description }
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.combinedClickable(
                        role = Role.Button,
                        onClick = onClick
                    )
                }
            )
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(4.5.dp)
                    .clip(CircleShape)
                    .background(ledColor)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SkinStripKeyHitbox(
    cx: Float,
    wPx: Float,
    xDp: (Float) -> Dp,
    yDp: (Float) -> Dp,
    description: String,
    tag: String,
    isActive: Boolean,
    isQueued: Boolean = false,
    activeColor: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val keyY = 324f
    val keyH = 72f
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .offset(x = xDp(cx - wPx * 0.5f), y = yDp(keyY))
            .size(width = xDp(wPx), height = yDp(keyH))
            .clip(shape)
            .then(
                when {
                    isActive -> Modifier
                        .background(activeColor.copy(alpha = 0.18f))
                        .border(1.2.dp, activeColor.copy(alpha = 0.85f), shape)
                    isQueued -> Modifier
                        .background(LedAmber.copy(alpha = 0.14f))
                        .border(1.dp, LedAmber.copy(alpha = 0.7f), shape)
                    else -> Modifier
                }
            )
            .testTag(tag)
            .semantics { contentDescription = description }
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.combinedClickable(
                        role = Role.Button,
                        onClick = onClick
                    )
                }
            )
    ) {
        if (isActive || isQueued) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .fillMaxWidth(0.55f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) activeColor else LedAmber)
            )
        }
    }
}

@Composable
private fun SkinRotaryKnobOverlay(
    cx: Float,
    cy: Float,
    diameterPx: Float,
    xDp: (Float) -> Dp,
    yDp: (Float) -> Dp,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    description: String,
    tag: String
) {
    var currentVal by remember(value) { mutableFloatStateOf(value) }
    Box(
        modifier = Modifier
            .offset(x = xDp(cx - diameterPx * 0.5f), y = yDp(cy - diameterPx * 0.5f))
            .size(width = xDp(diameterPx), height = yDp(diameterPx))
            .clip(CircleShape)
            .testTag(tag)
            .semantics { contentDescription = "$description ${(value * 100).toInt()}%" }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = (-dragAmount.y + dragAmount.x * 0.4f) / 130f
                    currentVal = (currentVal + delta).coerceIn(0f, 1f)
                    onValueChange(currentVal)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            drawArc(
                color = accentColor.copy(alpha = 0.85f),
                startAngle = 135f,
                sweepAngle = 270f * value.coerceIn(0.03f, 1f),
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            val rad = (135f + value.coerceIn(0f, 1f) * 270f) * PI / 180.0
            val r = size.minDimension * 0.38f
            drawCircle(
                color = accentColor,
                radius = 2.2.dp.toPx(),
                center = Offset(
                    x = center.x + (r * cos(rad)).toFloat(),
                    y = center.y + (r * sin(rad)).toFloat()
                )
            )
        }
    }
}
