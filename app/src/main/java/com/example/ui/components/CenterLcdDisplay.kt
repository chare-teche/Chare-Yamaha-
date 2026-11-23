package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerUiState
import com.example.ui.theme.ChassisVoid
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LcdDockBg
import com.example.ui.theme.LcdScreenBg
import com.example.ui.theme.LcdTopBarBg
import com.example.ui.theme.LedAmber
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrNeonBlueGlow
import com.example.ui.theme.SilkscreenMuted
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TileChordBottom
import com.example.ui.theme.TileChordTop
import com.example.ui.theme.TileRegistBottom
import com.example.ui.theme.TileRegistTop
import com.example.ui.theme.TileStyleBottom
import com.example.ui.theme.TileStyleTop
import com.example.ui.theme.TileVoiceBottom
import com.example.ui.theme.TileVoiceTop

/**
 * Authentic Yamaha PSR-SX900 Home Touchscreen Display matching the reference image:
 * - Black glass bezel with Home top bar
 * - 2x2 vibrant gradient tiles: Style (Sunset Orange), Voice (Cobalt Blue), Chord (Violet), Registration (Emerald Green)
 * - Middle dark telemetry bar: Tempo | Time Sig | Measure | Beat
 * - 6-icon bottom touchscreen dock: Style, Voice, Song, Multi Pad, Playlist, Audio
 */
@Composable
fun CenterLcdDisplay(
    state: ArrangerUiState,
    onOpenScreen: (ArrangerScreen) -> Unit,
    onTapTempo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bezelShape = RoundedCornerShape(7.dp)
    Column(
        modifier = modifier
            .clip(bezelShape)
            .background(ChassisVoid)
            .border(1.5.dp, Color(0xFF3A4354), bezelShape)
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .background(LcdScreenBg)
        ) {
            // 1. LCD Top Status Bar ("Home" + R1/R2 + Audio/Offline status)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(LcdTopBarBg)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = PsrNeonBlueGlow,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (state.statusBanner != null) state.statusBanner else "Home",
                        color = if (state.statusBanner != null) LedAmber else Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (state.isPanelLocked) {
                        Text("LOCKED", color = PsrActiveOrange, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 7.5.sp)
                    }
                    if (state.isRight2Enabled || state.isRight3Enabled) {
                        Text(state.activeRightPartsSummary, color = PsrNeonBlueGlow, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 7.5.sp)
                    }
                    Text(
                        text = if (state.connectedMidiDevices.isNotEmpty()) "MIDI" else "LOCAL",
                        color = LedEmerald,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 7.5.sp
                    )
                }
            }

            // 2. Top Row of 2x2 Vibrant Gradient Tiles: STYLE (Orange) & VOICE (Blue)
            Row(
                modifier = Modifier
                    .weight(1.08f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // STYLE TILE — Warm Sunset Orange Gradient
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(listOf(TileStyleTop, TileStyleBottom)))
                        .testTag("lcd_style_card")
                        .clickable { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0x33FFFFFF),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Style",
                                color = Color(0xFFFFE5CC),
                                fontFamily = InterFamily,
                                fontSize = 7.5.sp
                            )
                            Text(
                                text = state.activeSection.displayLabel,
                                color = Color.White,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.sp
                            )
                        }
                        Text(
                            text = state.activeStyle.name,
                            color = Color.White,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = state.activeStyle.category,
                            color = Color(0xFFFFDFC2),
                            fontFamily = InterFamily,
                            fontSize = 7.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // VOICE TILE — Vibrant Cobalt Blue Gradient
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(listOf(TileVoiceTop, TileVoiceBottom)))
                        .testTag("lcd_voice_card")
                        .clickable { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Piano,
                        contentDescription = null,
                        tint = Color(0x33FFFFFF),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Voice",
                                color = Color(0xFFD1E9FF),
                                fontFamily = InterFamily,
                                fontSize = 7.5.sp
                            )
                            Text(
                                text = state.activeRightPartsSummary,
                                color = Color.White,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.sp
                            )
                        }
                        Text(
                            text = state.right1Voice.name,
                            color = Color.White,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val secondaryLayerText = buildList {
                            if (state.isRight2Enabled) add("R2:${state.right2Voice.name.take(10)}")
                            if (state.isRight3Enabled) add("R3:${state.right3Voice.name.take(10)}")
                        }.joinToString(" + ").ifEmpty {
                            if (state.isLeftVoiceEnabled) "L: ${state.leftVoice.name}" else "LEFT OFF"
                        }
                        Text(
                            text = secondaryLayerText,
                            color = Color(0xFFCCE6FF),
                            fontFamily = InterFamily,
                            fontSize = 7.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3. Middle Dark Telemetry Strip: Tempo | Time Sig | Measure | Beat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(27.dp)
                    .background(Color(0xFF0A0E16))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryCell(
                    label = "♩ Tempo",
                    value = "${state.tempoBpm}",
                    valueColor = Color.White,
                    modifier = Modifier.weight(1f).clickable(onClick = onTapTempo)
                )
                TelemetryCell(
                    label = "Time Sig",
                    value = state.activeStyle.timeSignatureLabel,
                    valueColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCell(
                    label = "Measure",
                    value = state.measureNumber.toString().padStart(3, '0'),
                    valueColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCell(
                    label = "Beat",
                    value = "${state.currentBeat}",
                    valueColor = if (state.isPlaying && state.currentBeat == 1) PsrActiveOrange else PsrNeonBlueGlow,
                    modifier = Modifier.weight(0.8f)
                )
            }

            // 4. Bottom Row of 2x2 Vibrant Gradient Tiles: CHORD (Violet) & REGISTRATION (Emerald Green)
            Row(
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // CHORD TILE — Deep Electric Violet Gradient
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(listOf(TileChordTop, TileChordBottom)))
                        .testTag("lcd_chord_card")
                        .clickable { onOpenScreen(ArrangerScreen.MULTI_PAD_LOOPER) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chord", color = Color(0xFFE0D1FF), fontFamily = InterFamily, fontSize = 7.5.sp)
                        Text(
                            text = if (state.isChordLooperPlaying) "LOOP" else "TR ${if (state.transposeSemitones >= 0) "+" else ""}${state.transposeSemitones}",
                            color = Color(0xFFEDE4FF),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp
                        )
                    }
                    Text(
                        text = state.detectedChord.displayName,
                        color = Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                // REGISTRATION TILE — Rich Emerald Green Gradient
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(listOf(TileRegistTop, TileRegistBottom)))
                        .testTag("lcd_regist_card")
                        .clickable { onOpenScreen(ArrangerScreen.PLAYLIST) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Registration", color = Color(0xFFD5F5D8), fontFamily = InterFamily, fontSize = 7.5.sp)
                        if (state.isRegistrationFreezeEnabled) {
                            Text("FREEZE", color = Color(0xFFFFF59D), fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 7.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Bank ${state.activeRegistrationBank}",
                            color = Color.White,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Regist ${state.activeRegistrationSlot ?: "-"}",
                            color = Color(0xFFE6FFE8),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }

            // 5. Bottom 6-Icon PSR-SX900 Touchscreen Dock Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp)
                    .background(LcdDockBg)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LcdDockItem("Style", Icons.Default.LibraryMusic, true, "dock_style", Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.STYLE_LIBRARY)
                }
                LcdDockItem("Voice", Icons.Default.Piano, false, "dock_voice", Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.VOICE_SELECT)
                }
                LcdDockItem("Song", Icons.Default.MusicNote, false, "dock_song", Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.PLAYLIST)
                }
                LcdDockItem("Multi Pad", Icons.Default.GridView, state.activeMultiPadSlots.isNotEmpty(), "dock_pad", Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.MULTI_PAD_LOOPER)
                }
                LcdDockItem("Playlist", Icons.Default.QueueMusic, false, "dock_playlist", Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.PLAYLIST)
                }
                LcdDockItem("Audio", Icons.Default.Tune, false, "dock_audio", Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.MIXER)
                }
            }
        }
    }
}

@Composable
private fun TelemetryCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = SilkscreenMuted,
            fontFamily = InterFamily,
            fontSize = 6.8.sp,
            maxLines = 1
        )
        Text(
            text = value,
            color = valueColor,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            lineHeight = 11.5.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun LcdDockItem(
    label: String,
    icon: ImageVector,
    highlighted: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .testTag(tag)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (highlighted) PsrNeonBlueGlow else Color(0xFFD0D9E8),
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = label,
            color = if (highlighted) PsrNeonBlueGlow else Color(0xFFB7C3D6),
            fontFamily = InterFamily,
            fontSize = 6.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
