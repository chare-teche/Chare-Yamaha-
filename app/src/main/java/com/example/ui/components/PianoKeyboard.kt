package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DetectedChord
import com.example.domain.model.KeyboardLayoutRange
import com.example.ui.theme.BlackKeyBody
import com.example.ui.theme.BlackKeyPressed
import com.example.ui.theme.BlackKeyTop
import com.example.ui.theme.ChassisBorder
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.ChassisVoid
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrNeonBlue
import com.example.ui.theme.PsrNeonBlueGlow
import com.example.ui.theme.SilkscreenMuted
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.WhiteKeyBottom
import com.example.ui.theme.WhiteKeyChordGlow
import com.example.ui.theme.WhiteKeyMid
import com.example.ui.theme.WhiteKeyPressed
import com.example.ui.theme.WhiteKeyTop

private data class WhiteKeySpec(val midiNote: Int, val index: Int)
private data class BlackKeySpec(val midiNote: Int, val leftWhiteIndex: Int)

private val WHITE_PITCH_CLASSES = setOf(0, 2, 4, 5, 7, 9, 11)

private fun buildKeyboardLayout(
    startMidi: Int,
    whiteKeyCount: Int,
    highestAllowedMidi: Int
): Pair<List<WhiteKeySpec>, List<BlackKeySpec>> {
    val whites = mutableListOf<WhiteKeySpec>()
    val blacks = mutableListOf<BlackKeySpec>()
    var midi = startMidi.coerceAtLeast(21)
    while ((midi % 12) !in WHITE_PITCH_CLASSES && midi < highestAllowedMidi) midi++

    while (whites.size < whiteKeyCount && midi <= highestAllowedMidi) {
        val pc = midi % 12
        if (pc in WHITE_PITCH_CLASSES) {
            val whiteIdx = whites.size
            whites.add(WhiteKeySpec(midi, whiteIdx))
            val nextPc = (midi + 1) % 12
            if (nextPc !in WHITE_PITCH_CLASSES && (midi + 1) <= highestAllowedMidi && whites.size < whiteKeyCount) {
                blacks.add(BlackKeySpec(midi + 1, whiteIdx))
            }
        }
        midi++
    }
    return whites to blacks
}

/**
 * Realistic Playable Multi-Touch Piano Keyboard supporting 49, 61, 76, and 88-key ranges,
 * horizontal scrolling across the full compass, tall ivory white keys, shorter narrower glossy
 * ebony black keys (57% length, accurate 2+3 grouping), velocity sensitivity, and live split zone.
 */
@Composable
fun PlayablePianoKeyboard(
    keyboardLayout: KeyboardLayoutRange,
    startMidiNote: Int,
    visibleWhiteKeyCount: Int,
    splitNoteMidi: Int,
    isSplitEnabled: Boolean,
    showKeyLabels: Boolean,
    showScrollbar: Boolean,
    isSustainEnabled: Boolean,
    pitchBendSemitones: Float,
    pressedMidiNotes: Set<Int>,
    onScrollToMidi: (Int) -> Unit,
    onShiftOctaveView: (Int) -> Unit,
    onToggleSustain: () -> Unit,
    onPitchBend: (Float) -> Unit,
    onKeyDown: (midiNote: Int, velocity: Float) -> Unit,
    onKeyUp: (midiNote: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnKeyDown by rememberUpdatedState(onKeyDown)
    val currentOnKeyUp by rememberUpdatedState(onKeyUp)

    val (whiteKeys, blackKeys) = remember(startMidiNote, visibleWhiteKeyCount, keyboardLayout) {
        buildKeyboardLayout(startMidiNote, visibleWhiteKeyCount, keyboardLayout.highestMidi)
    }

    val pointerToNote = remember { mutableStateMapOf<PointerId, Int>() }

    DisposableEffect(startMidiNote, visibleWhiteKeyCount, keyboardLayout) {
        onDispose {
            pointerToNote.values.toSet().forEach { currentOnKeyUp(it) }
            pointerToNote.clear()
        }
    }

    val labelPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Column(modifier = modifier.fillMaxWidth().background(ChassisVoid)) {
        // Optional sleek 49/61/76/88-Key Horizontal Range Navigator + Quick Sustain/Pitch Strip
        if (showScrollbar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color(0xFF0C0F16))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Quick Sustain Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSustainEnabled) PsrNeonBlue else Color(0xFF1F2532))
                        .border(0.8.dp, if (isSustainEnabled) PsrNeonBlueGlow else ChassisBorder, RoundedCornerShape(3.dp))
                        .testTag("kbd_sustain_pill")
                        .clickable(onClick = onToggleSustain)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text("SUS", color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 7.5.sp)
                }

                // Interactive Pitch Bend mini strip
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1A202C))
                        .border(0.8.dp, if (pitchBendSemitones != 0f) PsrActiveOrange else ChassisBorder, RoundedCornerShape(3.dp))
                        .testTag("kbd_pitch_strip")
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = { onPitchBend(0f) },
                                onDragCancel = { onPitchBend(0f) },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    onPitchBend((pitchBendSemitones + dragAmount / 18f).coerceIn(-2f, 2f))
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (pitchBendSemitones == 0f) "PITCH" else String.format("%+.1f", pitchBendSemitones),
                        color = if (pitchBendSemitones == 0f) SilkscreenMuted else PsrActiveOrange,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 7.sp
                    )
                }

                // Scroll Left Octave
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1F2532))
                        .clickable { onShiftOctaveView(-1) }
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text("◀ OCT", color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 7.5.sp)
                }

                // Full-compass interactive mini keyboard scrollbar (drag to scroll 49/61/76/88 keys!)
                val totalSpan = (keyboardLayout.highestMidi - keyboardLayout.lowestMidi).coerceAtLeast(12)
                val visibleSpan = ((whiteKeys.lastOrNull()?.midiNote ?: (startMidiNote + 24)) - startMidiNote).coerceAtLeast(12)
                val thumbFraction = (visibleSpan.toFloat() / totalSpan.toFloat()).coerceIn(0.18f, 1f)
                val startFraction = ((startMidiNote - keyboardLayout.lowestMidi).toFloat() / totalSpan.toFloat()).coerceIn(0f, 1f - thumbFraction)

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF171C26))
                        .border(0.7.dp, Color(0xFF2D3648), RoundedCornerShape(3.dp))
                        .testTag("kbd_range_scrollbar")
                        .pointerInput(keyboardLayout, totalSpan) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaMidi = (dragAmount / size.width.coerceAtLeast(1)) * totalSpan
                                onScrollToMidi((startMidiNote + deltaMidi).toInt())
                            }
                        }
                ) {
                    // Draw subtle octave ticks across the full 49/61/76/88 compass
                    for (m in keyboardLayout.lowestMidi..keyboardLayout.highestMidi) {
                        if (m % 12 == 0) {
                            val x = ((m - keyboardLayout.lowestMidi).toFloat() / totalSpan) * size.width
                            drawLine(Color(0x558B97AB), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        }
                    }
                    // Highlight active visible viewport window
                    val leftX = startFraction * size.width
                    val thumbW = (thumbFraction * size.width).coerceAtLeast(24.dp.toPx())
                    drawRoundRect(
                        color = PsrNeonBlue.copy(alpha = 0.45f),
                        topLeft = Offset(leftX, 1f),
                        size = Size(thumbW, size.height - 2f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = PsrNeonBlueGlow,
                        topLeft = Offset(leftX, 1f),
                        size = Size(thumbW, 2.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // Scroll Right Octave
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1F2532))
                        .clickable { onShiftOctaveView(1) }
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text("OCT ▶", color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 7.5.sp)
                }

                Text(
                    text = "${keyboardLayout.totalKeys}K ${DetectedChord.midiNoteName(whiteKeys.firstOrNull()?.midiNote ?: 48)}–${DetectedChord.midiNoteName(whiteKeys.lastOrNull()?.midiNote ?: 72)}",
                    color = SilkscreenMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 7.5.sp,
                    maxLines = 1
                )
            }
        }

        // Sleek dark metallic keybed rail above keys
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF050608), Color(0xFF282E3B), Color(0xFF0A0C10))))
        )

        // Multi-Touch Realistic Piano Keyboard Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF08090C))
                .testTag("playable_piano_keyboard")
                .semantics { contentDescription = "Realistic multi-touch piano keyboard" }
                .pointerInput(whiteKeys, blackKeys) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val widthPx = size.width.toFloat().coerceAtLeast(1f)
                            val heightPx = size.height.toFloat().coerceAtLeast(1f)
                            val whiteW = widthPx / whiteKeys.size.coerceAtLeast(1)
                            val blackW = whiteW * 0.58f
                            val blackH = heightPx * 0.57f

                            fun hitTest(pos: Offset): Int? {
                                // Black keys are strictly shorter (57%) and narrower (58%) above white keys
                                if (pos.y <= blackH) {
                                    for (bk in blackKeys) {
                                        val left = (bk.leftWhiteIndex + 1) * whiteW - blackW * 0.5f
                                        if (Rect(left, 0f, left + blackW, blackH).contains(pos)) {
                                            return bk.midiNote
                                        }
                                    }
                                }
                                val wIdx = (pos.x / whiteW).toInt()
                                return whiteKeys.getOrNull(wIdx)?.midiNote
                            }

                            for (change in event.changes) {
                                val id = change.id
                                if (change.pressed) {
                                    val hit = hitTest(change.position)
                                    val prev = pointerToNote[id]
                                    if (hit != prev) {
                                        if (prev != null) {
                                            pointerToNote.remove(id)
                                            if (!pointerToNote.values.contains(prev)) currentOnKeyUp(prev)
                                        }
                                        if (hit != null) {
                                            // Vertical touch depth controls expressive velocity
                                            val vel = (0.42f + (change.position.y / heightPx) * 0.58f).coerceIn(0.35f, 1.0f)
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
            val whiteW = size.width / whiteKeys.size.coerceAtLeast(1)
            val keyH = size.height
            val blackW = whiteW * 0.58f
            val blackH = keyH * 0.57f
            val gap = 1.6.dp.toPx()

            // 1. Long, Tall 3D Ivory White Keys
            for (wk in whiteKeys) {
                val left = wk.index * whiteW + gap * 0.5f
                val w = whiteW - gap
                val isPressed = wk.midiNote in pressedMidiNotes
                val isInChordZone = isSplitEnabled && wk.midiNote <= splitNoteMidi

                val keyBrush = when {
                    isPressed && isInChordZone -> Brush.verticalGradient(listOf(Color(0xFF8CC0FF), WhiteKeyChordGlow, Color(0xFFB5D4FF)))
                    isPressed -> Brush.verticalGradient(listOf(Color(0xFFD2DBE8), WhiteKeyPressed, Color(0xFFB4C3D9)))
                    else -> Brush.verticalGradient(
                        0.0f to Color(0xFFD5D9E1),
                        0.06f to WhiteKeyTop,
                        0.84f to WhiteKeyMid,
                        1.0f to WhiteKeyBottom
                    )
                }

                drawRoundRect(
                    brush = keyBrush,
                    topLeft = Offset(left, 0f),
                    size = Size(w, keyH - 2.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Subtle Split Chord-Zone top indicator strip
                if (isInChordZone) {
                    drawRect(
                        color = PsrNeonBlue.copy(alpha = 0.55f),
                        topLeft = Offset(left + 1f, 0f),
                        size = Size(w - 2f, 3.dp.toPx())
                    )
                }

                // Bottom key lip bevel
                drawRoundRect(
                    color = if (isPressed) Color(0xFF7A8FA8) else Color(0xFFB5BCC9),
                    topLeft = Offset(left, keyH - 6.dp.toPx()),
                    size = Size(w, 4.5.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                if (showKeyLabels && (wk.midiNote % 12 == 0 || (isSplitEnabled && wk.midiNote == splitNoteMidi))) {
                    labelPaint.textSize = (whiteW * 0.25f).coerceIn(17f, 28f)
                    labelPaint.color = if (wk.midiNote == splitNoteMidi && isSplitEnabled) {
                        android.graphics.Color.rgb(25, 118, 210)
                    } else {
                        android.graphics.Color.rgb(92, 102, 118)
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        DetectedChord.midiNoteName(wk.midiNote),
                        left + w * 0.5f,
                        keyH - 12.dp.toPx(),
                        labelPaint
                    )
                }
            }

            // 2. Shorter, Narrower 3D Glossy Ebony Black Keys (accurate 2-black + 3-black grouping)
            for (bk in blackKeys) {
                val left = (bk.leftWhiteIndex + 1) * whiteW - blackW * 0.5f
                val isPressed = bk.midiNote in pressedMidiNotes

                // Cast shadow on white keys
                drawRoundRect(
                    color = Color(0x66000000),
                    topLeft = Offset(left + 2.dp.toPx(), 2.dp.toPx()),
                    size = Size(blackW + 1.5.dp.toPx(), blackH + 3.dp.toPx()),
                    cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
                )
                // Ebony key body
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        if (isPressed) listOf(BlackKeyPressed, Color(0xFF1B3359))
                        else listOf(Color(0xFF2B303A), BlackKeyBody, Color(0xFF07090D))
                    ),
                    topLeft = Offset(left, 0f),
                    size = Size(blackW, blackH),
                    cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
                )
                // Raised glossy top keycap surface
                val inset = blackW * 0.14f
                val facetH = blackH * 0.85f
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        if (isPressed) listOf(Color(0xFF3E6BB3), Color(0xFF234275))
                        else listOf(BlackKeyTop, Color(0xFF191D26), Color(0xFF2C323F))
                    ),
                    topLeft = Offset(left + inset, 2.dp.toPx()),
                    size = Size(blackW - inset * 2f, facetH),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                )
                // Specular gloss line
                drawLine(
                    color = if (isPressed) PsrNeonBlueGlow else Color(0x40FFFFFF),
                    start = Offset(left + inset + 2f, facetH),
                    end = Offset(left + blackW - inset - 2f, facetH),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // 3. Split Point vertical orange marker
            if (isSplitEnabled) {
                val splitIdx = whiteKeys.indexOfLast { it.midiNote <= splitNoteMidi }
                if (splitIdx in 0 until whiteKeys.lastIndex) {
                    val splitX = (splitIdx + 1) * whiteW
                    drawLine(
                        color = PsrActiveOrange,
                        start = Offset(splitX, 0f),
                        end = Offset(splitX, 18.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
        }
    }
}
