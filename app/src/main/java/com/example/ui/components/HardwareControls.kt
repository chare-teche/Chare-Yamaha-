package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChassisBorder
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.ChassisPanel
import com.example.ui.theme.ChassisVoid
import com.example.ui.theme.EthioBlue
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.EthioYellow
import com.example.ui.theme.LedBlue
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.LedOff
import com.example.ui.theme.MetalDark
import com.example.ui.theme.MetalLight
import com.example.ui.theme.MetalMid
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrActiveOrangeDark
import com.example.ui.theme.PsrNeonBlue
import com.example.ui.theme.PsrNeonBlueGlow
import com.example.ui.theme.SilkscreenMuted
import com.example.ui.theme.SilkscreenWhite
import com.example.ui.theme.SpaceGroteskFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Crisp Canvas-drawn Ethiopian flag emblem for the ETHIO YAMAHA header identity.
 */
@Composable
fun EthioFlagBadge(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(width = 24.dp, height = 15.dp)
            .clip(RoundedCornerShape(2.dp))
            .border(0.6.dp, Color(0x55FFFFFF), RoundedCornerShape(2.dp))
    ) {
        val h3 = size.height / 3f
        drawRect(EthioGreen, Offset(0f, 0f), Size(size.width, h3))
        drawRect(EthioYellow, Offset(0f, h3), Size(size.width, h3))
        drawRect(EthioRed, Offset(0f, h3 * 2f), Size(size.width, h3))
        drawCircle(
            color = EthioBlue,
            radius = size.height * 0.28f,
            center = center
        )
        drawCircle(
            color = EthioYellow,
            radius = size.height * 0.14f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * Live 4-bar audio signal level meter for the top PSR-SX900 status bar.
 */
@Composable
fun SignalPeakMeter(
    peakLevel: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val thresholds = listOf(0.06f, 0.22f, 0.48f, 0.76f)
        val heights = listOf(5.dp, 8.dp, 11.dp, 14.dp)
        thresholds.forEachIndexed { idx, th ->
            val lit = peakLevel >= th
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(heights[idx])
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        when {
                            !lit -> Color(0xFF394252)
                            idx == 3 -> PsrActiveOrange
                            else -> Color.White
                        }
                    )
            )
        }
    }
}

/**
 * Acoustic perforated dark speaker grille matching the PSR-SX900 left & right stereo speakers.
 */
@Composable
fun PerforatedSpeakerGrille(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF151820), Color(0xFF0A0C10))))
            .border(1.dp, Color(0xFF2E3544), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.52f)
            val maxR = minOf(size.width, size.height) * 0.44f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF282E3A), Color(0xFF161A22), Color(0xFF0A0C10)),
                    center = center,
                    radius = maxR
                ),
                radius = maxR,
                center = center
            )

            // Fine acoustic woven dot mesh
            val step = 5.5.dp.toPx()
            val dotR = 1.15.dp.toPx()
            var y = step * 0.5f
            var row = 0
            while (y < size.height - 2f) {
                var x = if (row % 2 == 0) step * 0.5f else step
                while (x < size.width - 2f) {
                    drawCircle(Color(0xAA050608), radius = dotR, center = Offset(x, y))
                    x += step
                }
                y += step * 0.85f
                row++
            }
        }
    }
}

/**
 * Iconic PSR-SX900 MASTER VOLUME brushed metallic knob surrounded by a glowing neon-blue LED halo ring.
 */
@Composable
fun NeonHaloMasterKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    knobSize: Dp,
    tag: String,
    modifier: Modifier = Modifier
) {
    var currentVal by remember(value) { mutableFloatStateOf(value) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "MASTER\nVOLUME",
            color = SilkscreenWhite,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 7.sp,
            lineHeight = 8.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(knobSize)
                .testTag(tag)
                .semantics { contentDescription = "Master Volume ${(value * 100).toInt()} percent" }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = (-dragAmount.y + dragAmount.x * 0.4f) / 130f
                        currentVal = (currentVal + delta).coerceIn(0f, 1f)
                        onValueChange(currentVal)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension * 0.47f
                val c = center

                // Vivid neon-blue outer halo glow matching the PSR-SX900 reference
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PsrNeonBlueGlow.copy(alpha = 0.65f), PsrNeonBlue.copy(alpha = 0.18f), Color.Transparent),
                        center = c,
                        radius = r * 1.12f
                    ),
                    radius = r * 1.12f,
                    center = c
                )
                // Illuminated LED ring arc
                drawArc(
                    color = Color(0xFF1F2B40),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = PsrNeonBlueGlow,
                    startAngle = 135f,
                    sweepAngle = 270f * value.coerceIn(0.05f, 1f),
                    useCenter = false,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
                // Brushed aluminum knob cap
                drawCircle(color = ChassisVoid, radius = r * 0.8f, center = c)
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(MetalDark, MetalLight, MetalMid, MetalLight, MetalDark),
                        center = c
                    ),
                    radius = r * 0.72f,
                    center = c
                )
                // Position indicator notch
                val angleDeg = 135f + value.coerceIn(0f, 1f) * 270f
                val rad = angleDeg * PI / 180.0
                drawLine(
                    color = ChassisVoid,
                    start = Offset(c.x + (r * 0.2f * cos(rad)).toFloat(), c.y + (r * 0.2f * sin(rad)).toFloat()),
                    end = Offset(c.x + (r * 0.68f * cos(rad)).toFloat(), c.y + (r * 0.68f * sin(rad)).toFloat()),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Row(
            modifier = Modifier.width(knobSize),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("MIN", color = SilkscreenMuted, fontSize = 6.sp, fontFamily = SpaceGroteskFamily)
            Text("MAX", color = SilkscreenMuted, fontSize = 6.sp, fontFamily = SpaceGroteskFamily)
        }
    }
}

/**
 * Brushed metal rotary knob (BALANCE, MIXER/STYLE VOL).
 */
@Composable
fun RotaryMetalKnob(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    knobSize: Dp,
    tag: String,
    modifier: Modifier = Modifier
) {
    var currentVal by remember(value) { mutableFloatStateOf(value) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = label,
            color = SilkscreenMuted,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 6.5.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(knobSize)
                .testTag(tag)
                .semantics { contentDescription = "$label ${(value * 100).toInt()} percent" }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = (-dragAmount.y + dragAmount.x * 0.4f) / 130f
                        currentVal = (currentVal + delta).coerceIn(0f, 1f)
                        onValueChange(currentVal)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension * 0.46f
                val c = center
                drawArc(
                    color = Color(0xFF333B4A),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = PsrNeonBlue,
                    startAngle = 135f,
                    sweepAngle = 270f * value.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawCircle(color = ChassisVoid, radius = r * 0.85f, center = c)
                drawCircle(
                    brush = Brush.sweepGradient(listOf(MetalDark, MetalLight, MetalDark, MetalMid, MetalDark), c),
                    radius = r * 0.74f,
                    center = c
                )
                val rad = (135f + value.coerceIn(0f, 1f) * 270f) * PI / 180.0
                drawLine(
                    color = Color.White,
                    start = Offset(c.x + (r * 0.25f * cos(rad)).toFloat(), c.y + (r * 0.25f * sin(rad)).toFloat()),
                    end = Offset(c.x + (r * 0.68f * cos(rad)).toFloat(), c.y + (r * 0.68f * sin(rad)).toFloat()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Sleek PSR-SX900 tactile hardware button (upper console: SONG, STYLE, VOICE, MIDI, UTILITY, REGIST, MEMORY, MENU,
 * EXIT, ENTER, LEFT, RIGHT1, RIGHT2, DSP, HARMONY, PANEL LOCK).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PsrHardwareButton(
    label: String,
    isActive: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
    activeColor: Color = PsrNeonBlue,
    solidFillWhenActive: Boolean = false,
    showLedBar: Boolean = false,
    fontSize: TextUnit = 8.sp,
    height: Dp = 24.dp,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(4.dp)
    val bgBrush = when {
        isActive && solidFillWhenActive -> Brush.verticalGradient(listOf(activeColor, activeColor.copy(alpha = 0.72f)))
        isActive -> Brush.verticalGradient(listOf(activeColor.copy(alpha = 0.42f), Color(0xFF151E30)))
        else -> Brush.verticalGradient(listOf(Color(0xFF282E3B), Color(0xFF13161E)))
    }
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(bgBrush)
            .border(
                width = if (isActive) 1.2.dp else 0.9.dp,
                color = if (isActive) activeColor else Color(0xFF3E4657),
                shape = shape
            )
            .testTag(tag)
            .semantics { contentDescription = label }
            .then(
                if (onLongClick != null) Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                else Modifier.clickable(onClick = onClick)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showLedBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp)
                    .fillMaxWidth(0.45f)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) activeColor else LedOff)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (showLedBar) 3.dp else 0.dp, start = 2.dp, end = 2.dp)
        )
    }
}

/**
 * Lower PSR-SX900 arranger strip square/tall illuminated hardware button:
 * INTRO 1-3, MAIN A-D (solid PSR orange when active!), BREAK, ENDING 1-3,
 * SYNC START, SYNC STOP, START/STOP (vivid neon blue!), FADE, TAP, REGIST 1-8, OTS 1-4.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PsrStripKey(
    label: String,
    isActive: Boolean,
    isQueued: Boolean = false,
    tag: String,
    modifier: Modifier = Modifier,
    activeColor: Color = PsrNeonBlue,
    solidActiveFill: Boolean = false,
    showTopBar: Boolean = false,
    topLabel: String? = null,
    fontSize: TextUnit = 10.sp,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (topLabel != null) {
            Text(
                text = topLabel,
                color = SilkscreenWhite,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 6.5.sp,
                lineHeight = 7.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(2.dp))
        }

        val shape = RoundedCornerShape(4.dp)
        val bgBrush = when {
            isActive && solidActiveFill -> Brush.verticalGradient(
                listOf(activeColor, activeColor.copy(alpha = 0.72f))
            )
            isActive -> Brush.verticalGradient(
                listOf(activeColor.copy(alpha = 0.38f), Color(0xFF141B29))
            )
            isQueued -> Brush.verticalGradient(
                listOf(activeColor.copy(alpha = 0.22f), Color(0xFF181E2B))
            )
            else -> Brush.verticalGradient(
                listOf(Color(0xFF282E3A), Color(0xFF14171F))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(shape)
                .background(bgBrush)
                .border(
                    width = if (isActive) 1.5.dp else 1.dp,
                    color = when {
                        isActive -> activeColor
                        isQueued -> activeColor.copy(alpha = 0.65f)
                        else -> Color(0xFF424B5E)
                    },
                    shape = shape
                )
                .testTag(tag)
                .semantics { contentDescription = topLabel?.replace("\n", " ") ?: label }
                .then(
                    if (onLongClick != null) Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                    else Modifier.clickable(onClick = onClick)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showTopBar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .fillMaxWidth(0.55f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isActive) activeColor else if (isQueued) activeColor.copy(alpha = 0.55f) else Color(0xFF6B7485))
                )
            }
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    color = Color.White,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = if (showTopBar) 5.dp else 0.dp)
                )
            }
        }
    }
}

/**
 * Brushed metal PSR-SX900 Data Entry Jog Dial with clickable 4-way navigation arrows around it.
 */
@Composable
fun PsrDataWheelWithArrows(
    onDeltaStep: (Int) -> Unit,
    onPrevStyle: () -> Unit,
    onNextStyle: () -> Unit,
    dialSize: Dp,
    tag: String,
    modifier: Modifier = Modifier
) {
    var rotationDeg by remember { mutableFloatStateOf(0f) }
    var accum by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier.size(dialSize + 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Directional cursor dots/arrows around wheel matching PSR-SX900 reference
        Text(
            text = "▲", color = SilkscreenMuted, fontSize = 7.sp,
            modifier = Modifier.align(Alignment.TopCenter).clickable { onDeltaStep(1) }
        )
        Text(
            text = "▼", color = SilkscreenMuted, fontSize = 7.sp,
            modifier = Modifier.align(Alignment.BottomCenter).clickable { onDeltaStep(-1) }
        )
        Text(
            text = "◄", color = SilkscreenMuted, fontSize = 7.sp,
            modifier = Modifier.align(Alignment.CenterStart).clickable(onClick = onPrevStyle)
        )
        Text(
            text = "►", color = SilkscreenMuted, fontSize = 7.sp,
            modifier = Modifier.align(Alignment.CenterEnd).clickable(onClick = onNextStyle)
        )

        Box(
            modifier = Modifier
                .size(dialSize)
                .clip(CircleShape)
                .testTag(tag)
                .semantics { contentDescription = "Data Entry Jog Wheel" }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = (-dragAmount.y + dragAmount.x) * 0.5f
                        rotationDeg = (rotationDeg + delta * 1.8f) % 360f
                        accum += delta
                        while (accum >= 8f) { accum -= 8f; onDeltaStep(1) }
                        while (accum <= -8f) { accum += 8f; onDeltaStep(-1) }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension * 0.49f
                val c = center
                drawCircle(color = ChassisVoid, radius = r, center = c)
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFF454E60), Color(0xFFDCE3EE), Color(0xFF282E3A), Color(0xFFC0C9D9), Color(0xFF454E60)),
                        c
                    ),
                    radius = r * 0.9f,
                    center = c
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF394150), Color(0xFF141820)),
                        c,
                        r * 0.74f
                    ),
                    radius = r * 0.74f,
                    center = c
                )
                val rad = rotationDeg * PI / 180.0
                val dimple = Offset(c.x + (r * 0.45f * cos(rad)).toFloat(), c.y + (r * 0.45f * sin(rad)).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF0A0C10), Color(0xFF4A5366)), dimple, r * 0.2f),
                    radius = r * 0.17f,
                    center = dimple
                )
            }
        }
    }
}

/**
 * Group label with thin horizontal neon-blue / silver accent bar matching the PSR-SX900 panel print.
 */
@Composable
fun PsrStripSectionHeader(
    title: String,
    accentColor: Color = PsrNeonBlue,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.weight(0.35f).height(1.2.dp).background(accentColor.copy(alpha = 0.75f)))
        Text(
            text = title,
            color = SilkscreenWhite,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 7.sp,
            maxLines = 1
        )
        Box(modifier = Modifier.weight(1f).height(1.2.dp).background(accentColor.copy(alpha = 0.75f)))
    }
}
