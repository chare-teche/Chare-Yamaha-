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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
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

/**
 * Pure geometry specification measured directly from the 1280 x 591 master PNG
 * (`reference-mobile-keyboard-clean_0ddac918.png`).
 * Upper control panel occupies y = 0..302; lower piano keyboard occupies y = 302..591.
 */
object PsrFullReferenceGeometry {
    const val FULL_WIDTH_PX = 1280f
    const val FULL_HEIGHT_PX = 591f
    const val FULL_ASPECT_RATIO = FULL_WIDTH_PX / FULL_HEIGHT_PX

    // Upper panel coordinate scale relative to the 1780 x 422 panel coordinate space
    const val UPPER_SCALE_X = 1280f / 1780f
    const val UPPER_SCALE_Y = 302f / 422f

    // Center LCD screen bounds in 1280 x 591 pixel coordinates
    const val LCD_LEFT_PX = 528f
    const val LCD_TOP_PX = 40f
    const val LCD_WIDTH_PX = 232f
    const val LCD_HEIGHT_PX = 129f

    // Exact piano keyboard vertical spans in 1280 x 591 pixels
    const val KEYBOARD_TOP_PX = 304f
    const val KEYBOARD_BOTTOM_PX = 591f
    const val BLACK_KEY_VISUAL_BOTTOM_PX = 371f
    const val BLACK_KEY_HIT_BOTTOM_PX = 380f

    data class KeyBoundsPx(
        val midiNote: Int,
        val leftPx: Float,
        val rightPx: Float
    )

    // 17 white key boundaries (C3=48 .. E5=76) measured directly from PNG key gaps
    private val WHITE_BOUNDARIES_PX = floatArrayOf(
        0f, 75f, 150f, 225f, 301f, 376f, 451f, 526f, 602f,
        677f, 752f, 828f, 903f, 978f, 1053f, 1129f, 1204f, 1280f
    )
    private val WHITE_MIDI_NOTES = intArrayOf(
        48, 50, 52, 53, 55, 57, 59,
        60, 62, 64, 65, 67, 69, 71,
        72, 74, 76
    )

    val WHITE_KEYS: List<KeyBoundsPx> = WHITE_MIDI_NOTES.mapIndexed { idx, midi ->
        KeyBoundsPx(midiNote = midi, leftPx = WHITE_BOUNDARIES_PX[idx], rightPx = WHITE_BOUNDARIES_PX[idx + 1])
    }

    // 12 black keys measured directly from PNG pixels
    val BLACK_KEYS: List<KeyBoundsPx> = listOf(
        KeyBoundsPx(49, 50f, 89f),     // C#3
        KeyBoundsPx(51, 144f, 183f),   // Eb3
        KeyBoundsPx(54, 273f, 313f),   // F#3
        KeyBoundsPx(56, 361f, 400f),   // Ab3
        KeyBoundsPx(58, 447f, 487f),   // Bb3
        KeyBoundsPx(61, 577f, 616f),   // C#4
        KeyBoundsPx(63, 671f, 710f),   // Eb4
        KeyBoundsPx(66, 800f, 840f),   // F#4
        KeyBoundsPx(68, 888f, 927f),   // Ab4
        KeyBoundsPx(70, 974f, 1014f),  // Bb4
        KeyBoundsPx(73, 1104f, 1143f), // C#5
        KeyBoundsPx(75, 1198f, 1237f)  // Eb5
    )

    /**
     * Hit-test normalized touch coordinates (0f..1f within the lower keyboard canvas,
     * covering y = 304..591 of the 1280x591 PNG). Returns exact MIDI note (48..76).
     */
    fun hitTestPianoKey(normX: Float, normY: Float): Int? {
        if (normX < 0f || normX > 1f || normY < 0f || normY > 1f) return null
        val px = (normX * FULL_WIDTH_PX).coerceIn(0f, FULL_WIDTH_PX - 0.01f)
        val py = KEYBOARD_TOP_PX + normY * (KEYBOARD_BOTTOM_PX - KEYBOARD_TOP_PX)

        if (py <= BLACK_KEY_HIT_BOTTOM_PX) {
            for (bk in BLACK_KEYS) {
                if (px >= bk.leftPx && px <= bk.rightPx) {
                    return bk.midiNote
                }
            }
        }
        for (wk in WHITE_KEYS) {
            if (px >= wk.leftPx && px <= wk.rightPx) {
                return wk.midiNote
            }
        }
        return WHITE_KEYS.lastOrNull()?.midiNote
    }
}

private enum class ReferenceDialTarget(val label: String) {
    TEMPO("TEMPO"),
    OCTAVE("OCTAVE"),
    TRANSPOSE("TRANS")
}

/**
 * Single unified 1280x591 interface reproducing the master full-design PNG:
 * - Upper half (y = 0..302): PSR-SX900 control panel with live CenterLcdDisplay, knobs, jog dial,
 *   pitch switch, and transparent touch controls connected to ArrangerViewModel.
 * - Lower half (y = 304..591): Exact 29-key (17 white + 12 black) piano keyboard geometry from the
 *   PNG connected directly to multi-touch audio synthesis and chord detection.
 */
@Composable
fun PsrFullReferenceConsole(
    state: ArrangerUiState,
    onOpenScreen: (ArrangerScreen) -> Unit,
    onExitToPerformance: () -> Unit,
    onTogglePanelLock: () -> Unit,
    onPianoKeyDown: (Int, Float) -> Unit,
    onPianoKeyUp: (Int) -> Unit,
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
    var dialTarget by remember { mutableStateOf(ReferenceDialTarget.TEMPO) }
    val stepDialTarget: (Int) -> Unit = { delta ->
        when (dialTarget) {
            ReferenceDialTarget.TEMPO -> onAdjustTempo(delta)
            ReferenceDialTarget.OCTAVE -> onAdjustOctave(delta)
            ReferenceDialTarget.TRANSPOSE -> onAdjustTranspose(delta)
        }
    }

    val currentOnKeyDown by rememberUpdatedState(onPianoKeyDown)
    val currentOnKeyUp by rememberUpdatedState(onPianoKeyUp)
    val pointerToNote = remember { mutableStateMapOf<PointerId, Int>() }

    DisposableEffect(Unit) {
        onDispose {
            pointerToNote.values.toSet().forEach { currentOnKeyUp(it) }
            pointerToNote.clear()
        }
    }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(PsrFullReferenceGeometry.FULL_ASPECT_RATIO)
    ) {
        val w = maxWidth
        val h = maxHeight

        // Direct pixel helpers in the 1280 x 591 coordinate system
        fun xFull(px: Float): Dp = w * (px / PsrFullReferenceGeometry.FULL_WIDTH_PX)
        fun yFull(py: Float): Dp = h * (py / PsrFullReferenceGeometry.FULL_HEIGHT_PX)

        // Upper-panel coordinates mapped from original 1780x422 landmarks to 1280x591
        fun ux(px1780: Float): Float = px1780 * PsrFullReferenceGeometry.UPPER_SCALE_X
        fun uy(py422: Float): Float = py422 * PsrFullReferenceGeometry.UPPER_SCALE_Y

        // 1. MASTER FULL DESIGN PNG ARTWORK (exact geometry preserved)
        Image(
            painter = painterResource(id = R.drawable.psr_sx900_full_reference),
            contentDescription = "ETHIO YAMAHA PSR-SX900 Full Console and Keyboard",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // 2. LIVE DYNAMIC CENTER LCD INSIDE BEZEL (x=528..760, y=40..169)
        CenterLcdDisplay(
            state = state,
            onOpenScreen = onOpenScreen,
            onTapTempo = onTapTempo,
            dialTargetLabel = dialTarget.label,
            modifier = Modifier
                .offset(x = xFull(PsrFullReferenceGeometry.LCD_LEFT_PX), y = yFull(PsrFullReferenceGeometry.LCD_TOP_PX))
                .size(width = xFull(PsrFullReferenceGeometry.LCD_WIDTH_PX), height = yFull(PsrFullReferenceGeometry.LCD_HEIGHT_PX))
        )

        // 3. UPPER PANEL KNOBS & POWER
        RefHitbox(
            cx = ux(386f), cy = uy(53f), bw = ux(48f), bh = uy(46f),
            xFull = ::xFull, yFull = ::yFull,
            description = if (state.isPanelLocked) "Unlock Panel" else "Lock Panel",
            tag = "btn_panel_lock",
            isActive = state.isPanelLocked,
            ledColor = PsrActiveOrange,
            shape = CircleShape,
            onClick = onTogglePanelLock
        )

        RefKnobOverlay(
            cx = ux(386f), cy = uy(152f), dPx = ux(80f),
            xFull = ::xFull, yFull = ::yFull,
            value = state.masterVolume,
            onValueChange = onSetMasterVolume,
            accentColor = ElectricBlueGlow,
            description = "Master Volume",
            tag = "knob_master_volume"
        )
        RefKnobOverlay(
            cx = ux(386f), cy = uy(244f), dPx = ux(52f),
            xFull = ::xFull, yFull = ::yFull,
            value = state.acmpBalance,
            onValueChange = onSetAcmpBalance,
            accentColor = PsrNeonBlue,
            description = "Balance",
            tag = "knob_balance"
        )
        RefKnobOverlay(
            cx = ux(487f), cy = uy(244f), dPx = ux(52f),
            xFull = ::xFull, yFull = ::yFull,
            value = state.styleVolume,
            onValueChange = onSetStyleVolume,
            accentColor = ChampagneGoldBright,
            description = "Chord Drum Volume",
            tag = "knob_style_vol"
        )

        // 4. LEFT 5-COLUMN MATRIX
        RefHitbox(ux(464f), uy(73f), ux(48f), uy(34f), ::xFull, ::yFull, "Previous Song or Style", "btn_style_prev", onClick = { onCycleStyle(-1) })
        RefHitbox(ux(520f), uy(73f), ux(48f), uy(34f), ::xFull, ::yFull, "Next Song or Style", "btn_style_next", onClick = { onCycleStyle(1) })
        RefHitbox(ux(575f), uy(73f), ux(48f), uy(34f), ::xFull, ::yFull, "Reset Tempo", "btn_player_reset", onClick = onResetTempo)
        RefHitbox(
            ux(630f), uy(73f), ux(48f), uy(34f), ::xFull, ::yFull, "Play or Stop", "btn_player_play_stop",
            isActive = state.isPlaying, ledColor = LedEmerald,
            onClick = onStartStop, onLongClick = { onOpenScreen(ArrangerScreen.SONG_PLAYER) }
        )
        RefHitbox(
            ux(685f), uy(73f), ux(48f), uy(34f), ::xFull, ::yFull, "Style Select", "btn_style",
            isActive = state.activeScreen == ArrangerScreen.STYLE_LIBRARY, ledColor = PsrActiveOrange,
            onClick = { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
        )

        val drumsMuted = state.mixerChannels.find { it.part == AccompPart.DRUMS }?.isMuted == true
        RefHitbox(
            ux(464f), uy(118f), ux(48f), uy(34f), ::xFull, ::yFull, "Channel On Off", "btn_channel_on_off",
            isActive = state.activeScreen == ArrangerScreen.CHANNEL_ON_OFF, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.CHANNEL_ON_OFF) }
        )
        RefHitbox(
            ux(520f), uy(118f), ux(48f), uy(34f), ::xFull, ::yFull, "Mixer", "btn_mixer_open",
            isActive = state.activeScreen == ArrangerScreen.MIXER, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.MIXER) }
        )
        RefHitbox(
            ux(575f), uy(118f), ux(48f), uy(34f), ::xFull, ::yFull, "Drum Mute", "btn_drum_mute",
            isActive = drumsMuted, ledColor = LedCrimson,
            onClick = { onToggleChannelMute(AccompPart.DRUMS) }
        )
        RefHitbox(
            ux(630f), uy(118f), ux(48f), uy(34f), ::xFull, ::yFull, "Left Part Toggle", "btn_part_left",
            isActive = state.isLeftVoiceEnabled, ledColor = ElectricBlueGlow,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.LEFT) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_PART_SETUP) }
        )
        RefHitbox(
            ux(685f), uy(118f), ux(48f), uy(34f), ::xFull, ::yFull, "ACMP Toggle", "btn_acmp_toggle",
            isActive = state.isAcmpEnabled, ledColor = LedEmerald,
            onClick = onToggleAcmp
        )

        RefHitbox(
            ux(464f), uy(168f), ux(48f), uy(34f), ::xFull, ::yFull, "Tempo", "btn_tap_tempo",
            isActive = dialTarget == ReferenceDialTarget.TEMPO, ledColor = ChampagneGoldBright,
            onClick = {
                if (dialTarget == ReferenceDialTarget.TEMPO) onTapTempo()
                else dialTarget = ReferenceDialTarget.TEMPO
            },
            onLongClick = onResetTempo
        )
        RefHitbox(
            ux(520f), uy(168f), ux(48f), uy(34f), ::xFull, ::yFull, "Octave Mode", "btn_octave_mode",
            isActive = dialTarget == ReferenceDialTarget.OCTAVE || state.octaveShift != 0, ledColor = ElectricBlueGlow,
            onClick = { dialTarget = ReferenceDialTarget.OCTAVE }
        )
        RefHitbox(
            ux(575f), uy(168f), ux(48f), uy(34f), ::xFull, ::yFull, "Transpose Mode", "btn_transpose_mode",
            isActive = dialTarget == ReferenceDialTarget.TRANSPOSE || state.transposeSemitones != 0, ledColor = PsrActiveOrange,
            onClick = { dialTarget = ReferenceDialTarget.TRANSPOSE }
        )
        RefHitbox(ux(630f), uy(168f), ux(48f), uy(34f), ::xFull, ::yFull, "Dial Minus", "btn_dial_minus", onClick = { stepDialTarget(-1) })
        RefHitbox(ux(685f), uy(168f), ux(48f), uy(34f), ::xFull, ::yFull, "Dial Plus", "btn_dial_plus", onClick = { stepDialTarget(1) })

        RefHitbox(
            ux(630f), uy(220f), ux(48f), uy(34f), ::xFull, ::yFull, "Auto Fill", "btn_auto_fill",
            isActive = state.isAutoFillEnabled, ledColor = PsrActiveOrange, onClick = onAutoFill
        )
        RefHitbox(ux(685f), uy(220f), ux(48f), uy(34f), ::xFull, ::yFull, "Reset Tempo", "btn_reset_tempo", onClick = onResetTempo)

        RefHitbox(
            ux(575f), uy(266f), ux(48f), uy(34f), ::xFull, ::yFull, "Fade In Out", "btn_fade_in_out",
            isActive = state.isFadeActive, ledColor = PsrActiveOrange, onClick = onFadeInOut
        )
        RefHitbox(
            ux(630f), uy(266f), ux(48f), uy(34f), ::xFull, ::yFull, "Sync Start", "btn_sync_start",
            isActive = state.isSyncStartArmed, ledColor = ElectricBlueGlow, onClick = onSyncStart
        )
        RefHitbox(
            ux(685f), uy(266f), ux(48f), uy(34f), ::xFull, ::yFull, "Sync Stop", "btn_sync_stop",
            isActive = state.isSyncStopArmed, ledColor = ElectricBlueGlow, onClick = onSyncStop
        )

        // 5. UNDER LCD: KEYBOARD SET 1–5
        listOf(780f, 837f, 894f, 951f).forEachIndexed { idx, cx1780 ->
            val slot = idx + 1
            RefHitbox(
                cx = ux(cx1780), cy = uy(269f), bw = ux(48f), bh = uy(34f),
                xFull = ::xFull, yFull = ::yFull,
                description = "OTS $slot",
                tag = "btn_ots_$slot",
                isActive = state.activeOtsSlot == slot,
                ledColor = PsrActiveOrange,
                onClick = { onSelectOts(slot) }
            )
        }
        RefHitbox(
            cx = ux(1008f), cy = uy(269f), bw = ux(48f), bh = uy(34f),
            xFull = ::xFull, yFull = ::yFull,
            description = "Voice Select",
            tag = "btn_voice",
            isActive = state.isOtsLinkEnabled || state.activeScreen == ArrangerScreen.VOICE_SELECT,
            ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) },
            onLongClick = onToggleOtsLink
        )

        // 6. CENTER-RIGHT: MENU, EXIT, JOG DIAL, <, >, Q.T., STKS
        RefHitbox(
            ux(1106f), uy(73f), ux(48f), uy(34f), ::xFull, ::yFull, "Main Menu", "btn_right_menu",
            isActive = state.activeScreen == ArrangerScreen.MAIN_MENU, ledColor = ChampagneGoldBright,
            onClick = { onOpenScreen(ArrangerScreen.MAIN_MENU) }
        )
        RefHitbox(ux(1161f), uy(73f), ux(48f), uy(34f), ::xFull, ::yFull, "Exit", "btn_exit", onClick = onExitToPerformance)

        var jogAccum by remember { mutableFloatStateOf(0f) }
        Box(
            modifier = Modifier
                .offset(x = xFull(ux(1132f) - ux(42f)), y = yFull(uy(148f) - uy(42f)))
                .size(width = xFull(ux(84f)), height = yFull(uy(84f)))
                .clip(CircleShape)
                .testTag("data_entry_jog_dial")
                .semantics { contentDescription = "Data Entry Jog Dial" }
                .pointerInput(dialTarget) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = (-dragAmount.y + dragAmount.x) * 0.5f
                        jogAccum += delta
                        while (jogAccum >= 8f) { jogAccum -= 8f; stepDialTarget(1) }
                        while (jogAccum <= -8f) { jogAccum += 8f; stepDialTarget(-1) }
                    }
                }
        )

        RefHitbox(ux(1101f), uy(222f), ux(40f), uy(32f), ::xFull, ::yFull, "Prev Style", "btn_nav_prev", onClick = { onCycleStyle(-1) }, onLongClick = { onStepRegSequence(-1) })
        RefHitbox(ux(1161f), uy(222f), ux(40f), uy(32f), ::xFull, ::yFull, "Next Style", "btn_nav_next", onClick = { onCycleStyle(1) }, onLongClick = { onStepRegSequence(1) })
        RefHitbox(
            ux(1101f), uy(266f), ux(46f), uy(32f), ::xFull, ::yFull, "Playlist", "btn_song",
            isActive = state.activeScreen == ArrangerScreen.PLAYLIST, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.PLAYLIST) }
        )
        RefHitbox(
            ux(1158f), uy(266f), ux(42f), uy(32f), ::xFull, ::yFull, "Multi Pads and Looper", "btn_direct_access",
            isActive = state.activeScreen == ArrangerScreen.MULTI_PAD_LOOPER || state.isChordLooperPlaying, ledColor = PsrActiveOrange,
            onClick = { onOpenScreen(ArrangerScreen.MULTI_PAD_LOOPER) }
        )

        // 7. RIGHT 4-COLUMN MATRIX
        RefHitbox(
            ux(1237f), uy(72f), ux(46f), uy(34f), ::xFull, ::yFull, "Memory Store", "btn_memory",
            isActive = state.isMemoryStoreArmed, ledColor = LedCrimson,
            onClick = onToggleMemoryStore, onLongClick = { onOpenScreen(ArrangerScreen.SONG_RECORDING) }
        )
        RefHitbox(
            ux(1291f), uy(72f), ux(46f), uy(34f), ::xFull, ::yFull, "Settings", "btn_utility",
            isActive = state.activeScreen == ArrangerScreen.SETTINGS, ledColor = ChampagneGoldBright,
            onClick = { onOpenScreen(ArrangerScreen.SETTINGS) }
        )
        RefHitbox(
            ux(1344f), uy(72f), ux(46f), uy(34f), ::xFull, ::yFull, "Style Settings", "btn_style_settings",
            isActive = state.activeScreen == ArrangerScreen.STYLE_SETTINGS, ledColor = PsrActiveOrange,
            onClick = { onOpenScreen(ArrangerScreen.STYLE_SETTINGS) },
            onLongClick = { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
        )
        RefHitbox(
            ux(1398f), uy(72f), ux(46f), uy(34f), ::xFull, ::yFull, "Mixer and Effects", "btn_left_menu",
            isActive = state.activeScreen == ArrangerScreen.MIXER, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.MIXER) }
        )

        RefHitbox(
            ux(1237f), uy(116f), ux(46f), uy(34f), ::xFull, ::yFull, "Split and Fingering", "btn_split_fingering",
            isActive = state.activeScreen == ArrangerScreen.SPLIT_FINGERING, ledColor = ElectricBlueGlow,
            onClick = { onOpenScreen(ArrangerScreen.SPLIT_FINGERING) }
        )
        RefHitbox(
            ux(1291f), uy(116f), ux(46f), uy(34f), ::xFull, ::yFull, "Registration Memory", "btn_regist",
            isActive = state.isRegistrationFreezeEnabled || state.activeScreen == ArrangerScreen.REGISTRATION_MEMORY, ledColor = LedEmerald,
            onClick = { onOpenScreen(ArrangerScreen.REGISTRATION_MEMORY) },
            onLongClick = { onOpenScreen(ArrangerScreen.REGISTRATION_FREEZE) }
        )
        RefHitbox(
            ux(1344f), uy(116f), ux(46f), uy(34f), ::xFull, ::yFull, "MIDI Settings", "btn_midi",
            isActive = state.connectedMidiDevices.isNotEmpty() || state.activeScreen == ArrangerScreen.MIDI_SETTINGS, ledColor = LedEmerald,
            onClick = { onOpenScreen(ArrangerScreen.MIDI_SETTINGS) }
        )
        RefHitbox(
            ux(1398f), uy(116f), ux(46f), uy(34f), ::xFull, ::yFull, "Metronome", "btn_metronome",
            isActive = state.isMetronomeEnabled, ledColor = ChampagneGoldBright,
            onClick = onToggleMetronome
        )

        RefHitbox(
            ux(1344f), uy(162f), ux(46f), uy(34f), ::xFull, ::yFull, "Sustain Pedal", "btn_pedal_sustain",
            isActive = state.isSustainEnabled, ledColor = ElectricBlueGlow, onClick = onToggleSustain
        )
        RefHitbox(
            ux(1398f), uy(162f), ux(46f), uy(34f), ::xFull, ::yFull, "Split Keyboard", "btn_split_toggle",
            isActive = state.isSplitEnabled, ledColor = PsrActiveOrange,
            onClick = onToggleSplit, onLongClick = { onOpenScreen(ArrangerScreen.SPLIT_FINGERING) }
        )

        RefHitbox(
            ux(1237f), uy(208f), ux(46f), uy(34f), ::xFull, ::yFull, "Tempo Lock", "btn_tempo_lock",
            isActive = state.isTempoLocked, ledColor = ChampagneGoldBright, onClick = onToggleTempoLock
        )
        RefHitbox(
            ux(1291f), uy(208f), ux(46f), uy(34f), ::xFull, ::yFull, "Right 1", "btn_part_right1",
            isActive = state.isRight1Enabled, ledColor = ElectricBlueGlow,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_1) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
        )
        RefHitbox(
            ux(1344f), uy(208f), ux(46f), uy(34f), ::xFull, ::yFull, "Right 2", "btn_part_right2",
            isActive = state.isRight2Enabled, ledColor = PsrActiveOrange,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_2) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
        )
        RefHitbox(
            ux(1398f), uy(208f), ux(46f), uy(34f), ::xFull, ::yFull, "Right 3", "btn_part_right3",
            isActive = state.isRight3Enabled, ledColor = LedEmerald,
            onClick = { onTogglePartEnabled(KeyboardPartTarget.RIGHT_3) },
            onLongClick = { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
        )

        // 8. LOWER ARRANGER STRIP (Pitch, Intro 1-3, Var A-D, Break, Start, Fill 1-4, Ending 1-3, Reg 1-8, Pads 1-4)
        Box(
            modifier = Modifier
                .offset(x = xFull(ux(104f)), y = yFull(uy(334f)))
                .size(width = xFull(ux(60f)), height = yFull(uy(56f)))
                .clip(RoundedCornerShape(6.dp))
                .then(
                    if (state.pitchBendSemitones != 0f) Modifier.border(1.5.dp, PsrActiveOrange, RoundedCornerShape(6.dp))
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

        listOf(247f to ArrangerSection.INTRO_1, 295f to ArrangerSection.INTRO_2, 345f to ArrangerSection.INTRO_3).forEach { (cx1780, sec) ->
            RefStripHitbox(
                cx = ux(cx1780), wPx = ux(45f), xFull = ::xFull, yFull = ::yFull,
                description = sec.displayLabel, tag = "btn_${sec.name.lowercase()}",
                isActive = state.activeSection == sec, isQueued = state.queuedSection == sec,
                activeColor = PsrNeonBlue, onClick = { onIntro(sec) }
            )
        }
        listOf(399f to ArrangerSection.MAIN_A, 447f to ArrangerSection.MAIN_B, 495f to ArrangerSection.MAIN_C, 544f to ArrangerSection.MAIN_D).forEach { (cx1780, sec) ->
            val isFill = state.activeSection == sec.correspondingFill()
            RefStripHitbox(
                cx = ux(cx1780), wPx = ux(45f), xFull = ::xFull, yFull = ::yFull,
                description = sec.displayLabel, tag = "btn_${sec.name.lowercase()}",
                isActive = state.activeSection == sec || isFill, isQueued = state.queuedSection == sec,
                activeColor = PsrActiveOrange, onClick = { onMainVariation(sec) }
            )
        }
        RefStripHitbox(ux(597f), ux(47f), ::xFull, ::yFull, "Break", "btn_break", state.activeSection == ArrangerSection.BREAK, false, PsrActiveOrange, onClick = onBreak)
        RefStripHitbox(
            ux(657f), ux(58f), ::xFull, ::yFull, "Start or Stop Style", "btn_start_stop",
            state.isPlaying, false,
            if (state.isPlaying && state.currentBeat == 1) PsrActiveOrange else ElectricBlueGlow,
            onClick = onStartStop
        )
        listOf(719f to ArrangerSection.MAIN_A, 763f to ArrangerSection.MAIN_B, 812f to ArrangerSection.MAIN_C, 859f to ArrangerSection.MAIN_D).forEachIndexed { idx, (cx1780, sec) ->
            RefStripHitbox(
                cx = ux(cx1780), wPx = ux(44f), xFull = ::xFull, yFull = ::yFull,
                description = "Fill ${idx + 1}", tag = "btn_fill_${idx + 1}",
                isActive = state.activeSection == sec.correspondingFill(), isQueued = false,
                activeColor = PsrActiveOrange, onClick = { onMainVariation(sec) }
            )
        }
        listOf(915f to ArrangerSection.ENDING_1, 963f to ArrangerSection.ENDING_2, 1012f to ArrangerSection.ENDING_3).forEach { (cx1780, sec) ->
            RefStripHitbox(
                cx = ux(cx1780), wPx = ux(45f), xFull = ::xFull, yFull = ::yFull,
                description = sec.displayLabel, tag = "btn_${sec.name.lowercase()}",
                isActive = state.activeSection == sec, isQueued = state.queuedSection == sec,
                activeColor = PsrNeonBlue, onClick = { onEnding(sec) }
            )
        }
        listOf(1079f, 1129f, 1178f, 1231f, 1281f, 1330f, 1383f, 1432f).forEachIndexed { idx, cx1780 ->
            val slot = idx + 1
            val isSelected = state.activeRegistrationSlot == slot
            val isPopulated = state.registrationSlots.getOrNull(idx)?.isPopulated == true
            RefStripHitbox(
                cx = ux(cx1780), wPx = ux(46f), xFull = ::xFull, yFull = ::yFull,
                description = "Registration Memory $slot",
                tag = "btn_reg_$slot",
                isActive = isSelected || state.isMemoryStoreArmed || isPopulated,
                activeColor = when {
                    state.isMemoryStoreArmed -> LedCrimson
                    isSelected -> ElectricBlueGlow
                    else -> PsrNeonBlue.copy(alpha = 0.45f)
                },
                onClick = { onRegistrationPressed(slot) },
                onLongClick = { onRegistrationLongPressed(slot) }
            )
        }
        listOf(1497f, 1545f, 1593f, 1642f).forEachIndexed { idx, cx1780 ->
            val slot = idx + 1
            RefStripHitbox(
                cx = ux(cx1780), wPx = ux(45f), xFull = ::xFull, yFull = ::yFull,
                description = "Multi Pad $slot",
                tag = "btn_pad_$slot",
                isActive = slot in state.activeMultiPadSlots,
                activeColor = PsrActiveOrange,
                onClick = { onMultiPadPressed(slot) },
                onLongClick = onStopAllMultiPads
            )
        }

        // 9. EXACT LOWER PLAYABLE PIANO KEYBOARD OVERLAY ON PNG KEYS (y = 304..591)
        val kbdHeightPx = PsrFullReferenceGeometry.KEYBOARD_BOTTOM_PX - PsrFullReferenceGeometry.KEYBOARD_TOP_PX
        val blackKeyRelHeight = (PsrFullReferenceGeometry.BLACK_KEY_VISUAL_BOTTOM_PX - PsrFullReferenceGeometry.KEYBOARD_TOP_PX) / kbdHeightPx

        Canvas(
            modifier = Modifier
                .offset(x = 0.dp, y = yFull(PsrFullReferenceGeometry.KEYBOARD_TOP_PX))
                .size(width = w, height = yFull(kbdHeightPx))
                .testTag("playable_piano_keyboard")
                .semantics { contentDescription = "PSR-SX900 Playable Piano Keyboard" }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val cw = size.width.toFloat().coerceAtLeast(1f)
                            val ch = size.height.toFloat().coerceAtLeast(1f)
                            for (change in event.changes) {
                                val id = change.id
                                if (change.pressed) {
                                    val hit = PsrFullReferenceGeometry.hitTestPianoKey(
                                        normX = change.position.x / cw,
                                        normY = change.position.y / ch
                                    )
                                    val prev = pointerToNote[id]
                                    if (hit != prev) {
                                        if (prev != null) {
                                            pointerToNote.remove(id)
                                            if (!pointerToNote.values.contains(prev)) currentOnKeyUp(prev)
                                        }
                                        if (hit != null) {
                                            val vel = (0.42f + (change.position.y / ch) * 0.58f).coerceIn(0.35f, 1.0f)
                                            pointerToNote[id] = hit
                                            currentOnKeyDown(hit, vel)
                                        }
                                    }
                                    change.consume()
                                } else {
                                    val prev = pointerToNote.remove(id)
                                    if (prev != null && !pointerToNote.values.contains(prev)) {
                                        currentOnKeyUp(prev)
                                    }
                                    change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            val scaleX = size.width / PsrFullReferenceGeometry.FULL_WIDTH_PX
            val kbdH = size.height
            val blackVisualH = kbdH * blackKeyRelHeight

            // Subtle translucent pressed-white-key highlight aligned strictly to PNG white keys
            for (wk in PsrFullReferenceGeometry.WHITE_KEYS) {
                if (wk.midiNote in state.pressedMidiNotes) {
                    val left = wk.leftPx * scaleX + 1.dp.toPx()
                    val right = wk.rightPx * scaleX - 1.dp.toPx()
                    val isChordZone = state.isSplitEnabled && wk.midiNote <= state.splitNoteMidi
                    drawRoundRect(
                        color = (if (isChordZone) ChampagneGoldBright else ElectricBlueGlow).copy(alpha = 0.28f),
                        topLeft = Offset(left, 2f),
                        size = Size((right - left).coerceAtLeast(2f), kbdH - 4f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }

            // Subtle translucent pressed-black-key highlight drawn above white highlight
            for (bk in PsrFullReferenceGeometry.BLACK_KEYS) {
                if (bk.midiNote in state.pressedMidiNotes) {
                    val left = bk.leftPx * scaleX
                    val right = bk.rightPx * scaleX
                    drawRoundRect(
                        color = ElectricBlueGlow.copy(alpha = 0.42f),
                        topLeft = Offset(left, 1f),
                        size = Size((right - left).coerceAtLeast(2f), blackVisualH),
                        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RefHitbox(
    cx: Float,
    cy: Float,
    bw: Float,
    bh: Float,
    xFull: (Float) -> Dp,
    yFull: (Float) -> Dp,
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
            .offset(x = xFull(cx - bw * 0.5f), y = yFull(cy - bh * 0.5f))
            .size(width = xFull(bw), height = yFull(bh))
            .clip(shape)
            .then(
                if (isActive) {
                    Modifier
                        .background(ledColor.copy(alpha = 0.14f))
                        .border(1.dp, ledColor.copy(alpha = 0.78f), shape)
                } else Modifier
            )
            .testTag(tag)
            .semantics { contentDescription = description }
            .then(
                if (onLongClick != null) Modifier.combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
                else Modifier.combinedClickable(role = Role.Button, onClick = onClick)
            )
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(ledColor)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RefStripHitbox(
    cx: Float,
    wPx: Float,
    xFull: (Float) -> Dp,
    yFull: (Float) -> Dp,
    description: String,
    tag: String,
    isActive: Boolean,
    isQueued: Boolean = false,
    activeColor: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val keyY = 324f * PsrFullReferenceGeometry.UPPER_SCALE_Y
    val keyH = 72f * PsrFullReferenceGeometry.UPPER_SCALE_Y
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .offset(x = xFull(cx - wPx * 0.5f), y = yFull(keyY))
            .size(width = xFull(wPx), height = yFull(keyH))
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
                if (onLongClick != null) Modifier.combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
                else Modifier.combinedClickable(role = Role.Button, onClick = onClick)
            )
    ) {
        if (isActive || isQueued) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .fillMaxWidth(0.55f)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) activeColor else LedAmber)
            )
        }
    }
}

@Composable
private fun RefKnobOverlay(
    cx: Float,
    cy: Float,
    dPx: Float,
    xFull: (Float) -> Dp,
    yFull: (Float) -> Dp,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    description: String,
    tag: String
) {
    var currentVal by remember(value) { mutableFloatStateOf(value) }
    Box(
        modifier = Modifier
            .offset(x = xFull(cx - dPx * 0.5f), y = yFull(cy - dPx * 0.5f))
            .size(width = xFull(dPx), height = yFull(dPx))
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
            drawArc(
                color = accentColor.copy(alpha = 0.85f),
                startAngle = 135f,
                sweepAngle = 270f * value.coerceIn(0.03f, 1f),
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            val rad = (135f + value.coerceIn(0f, 1f) * 270f) * PI / 180.0
            val r = size.minDimension * 0.38f
            drawCircle(
                color = accentColor,
                radius = 2.dp.toPx(),
                center = Offset(
                    x = center.x + (r * cos(rad)).toFloat(),
                    y = center.y + (r * sin(rad)).toFloat()
                )
            )
        }
    }
}
