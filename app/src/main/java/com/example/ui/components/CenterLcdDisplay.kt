package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.KeyboardPartTarget
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldBright
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LcdBackdrop
import com.example.ui.theme.LcdDockBg
import com.example.ui.theme.LcdHeaderSurface
import com.example.ui.theme.LedAmber
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TileChordBottom
import com.example.ui.theme.TileChordTop
import com.example.ui.theme.TileRegistBottom
import com.example.ui.theme.TileRegistTop
import com.example.ui.theme.TileStyleBottom
import com.example.ui.theme.TileStyleTop
import com.example.ui.theme.TileVoiceBottom
import com.example.ui.theme.TileVoiceTop

/**
 * High-contrast live performance LCD rendered directly inside the PSR-SX900 skin screen bezel.
 * Adapts smoothly from compact phone skin (~140x75dp) up to full tablet dimensions without clipping.
 */
@Composable
fun CenterLcdDisplay(
    state: ArrangerUiState,
    onOpenScreen: (ArrangerScreen) -> Unit,
    onTapTempo: () -> Unit,
    dialTargetLabel: String = "TEMPO",
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(LcdBackdrop)
    ) {
        val compact = maxHeight < 105.dp
        val topBarH = if (compact) 13.dp else 18.dp
        val telemetryH = if (compact) 15.dp else 23.dp
        val dockH = if (compact) 13.dp else 20.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. LCD Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarH)
                    .background(LcdHeaderSurface)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.statusBanner ?: "ETHIO YAMAHA",
                    color = if (state.statusBanner != null) LedAmber else ChampagneGoldBright,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 6.5.sp else 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isPanelLocked) {
                        Text(
                            text = "LOCK",
                            color = PsrActiveOrange,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 6.sp else 7.5.sp
                        )
                    }
                    Text(
                        text = if (state.isAcmpEnabled) "ACMP" else "FREE",
                        color = if (state.isAcmpEnabled) ElectricBlueGlow else TextMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 6.sp else 7.5.sp
                    )
                    // 8-channel compact activity indicator dots
                    Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                        state.eightAccompChannels.forEach { ch ->
                            Box(
                                modifier = Modifier
                                    .size(if (compact) 3.dp else 4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            ch.isMuted -> Color(0xFF3A3F4B)
                                            state.isPlaying -> LedEmerald
                                            else -> ElectricBlue.copy(alpha = 0.7f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            // 2. Top Row Modules: STYLE & 4-PART VOICES
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 1.5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // STYLE Tile
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.linearGradient(listOf(TileStyleTop, TileStyleBottom)))
                        .testTag("lcd_style_card")
                        .clickable { onOpenScreen(ArrangerScreen.STYLE_LIBRARY) }
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STYLE",
                            color = Color(0xFFFFE5CC),
                            fontFamily = InterFamily,
                            fontSize = if (compact) 5.5.sp else 7.sp
                        )
                        Text(
                            text = state.queuedSection?.shortLabel?.let { "${state.activeSection.shortLabel}→$it" }
                                ?: state.activeSection.displayLabel,
                            color = Color.White,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 5.8.sp else 7.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = state.activeStyle.name,
                        color = Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 8.5.sp else 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // VOICE / 4-PART Tile
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.linearGradient(listOf(TileVoiceTop, TileVoiceBottom)))
                        .testTag("lcd_voice_card")
                        .clickable { onOpenScreen(ArrangerScreen.VOICE_SELECT) }
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeLabels = KeyboardPartTarget.entries.filter { state.partConfig(it).isEnabled }
                            .joinToString("·") { it.shortLabel }
                            .ifEmpty { "OFF" }
                        Text(
                            text = activeLabels,
                            color = ChampagneGoldBright,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 5.5.sp else 7.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = state.right1Voice.name,
                        color = Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 8.5.sp else 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. Telemetry Strip: BPM / SIG / BAR.BEAT / DIAL MODE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(telemetryH)
                    .background(Color(0xFF070A10))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "♩${state.tempoBpm}",
                    color = TextPrimary,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 7.5.sp else 9.5.sp,
                    modifier = Modifier.clickable(onClick = onTapTempo)
                )
                Text(
                    text = state.activeStyle.timeSignatureLabel,
                    color = ChampagneGold,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 7.sp else 9.sp
                )
                Text(
                    text = "${state.measureNumber.toString().padStart(3, '0')}.${state.currentBeat}",
                    color = if (state.isPlaying && state.currentBeat == 1) PsrActiveOrange else ElectricBlueGlow,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 7.5.sp else 9.5.sp
                )
                Text(
                    text = "$dialTargetLabel TR${if (state.transposeSemitones >= 0) "+" else ""}${state.transposeSemitones}",
                    color = TextMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = if (compact) 5.8.sp else 7.5.sp,
                    maxLines = 1
                )
            }

            // 4. Bottom Row Modules: CHORD & REGISTRATION MEMORY
            Row(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 1.5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // CHORD Tile
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.linearGradient(listOf(TileChordTop, TileChordBottom)))
                        .testTag("lcd_chord_card")
                        .clickable { onOpenScreen(ArrangerScreen.MULTI_PAD_LOOPER) }
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.detectedChord.displayName,
                        color = Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 9.sp else 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (state.isChordLooperPlaying) "LOOP" else "OCT${if (state.octaveShift >= 0) "+" else ""}${state.octaveShift}",
                        color = Color(0xFFE0D1FF),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = if (compact) 5.5.sp else 7.sp
                    )
                }

                // REGISTRATION Tile
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.linearGradient(listOf(TileRegistTop, TileRegistBottom)))
                        .testTag("lcd_regist_card")
                        .clickable { onOpenScreen(ArrangerScreen.REGISTRATION_MEMORY) }
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "B${state.activeRegistrationBank}-R${state.activeRegistrationSlot ?: "-"}",
                        color = Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 8.5.sp else 10.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = if (state.isRegistrationFreezeEnabled) "FRZ" else "REG",
                        color = if (state.isRegistrationFreezeEnabled) Color(0xFFFFF59D) else Color(0xFFD5F5D8),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 5.8.sp else 7.5.sp
                    )
                }
            }

            // 5. Compact Interactive Dock Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dockH)
                    .background(LcdDockBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LcdDockChip("Style", state.activeScreen == ArrangerScreen.STYLE_LIBRARY, "dock_style", compact, Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.STYLE_LIBRARY)
                }
                LcdDockChip("Voice", state.activeScreen == ArrangerScreen.VOICE_SELECT, "dock_voice", compact, Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.VOICE_SELECT)
                }
                LcdDockChip("Parts", state.activeScreen == ArrangerScreen.VOICE_PART_SETUP, "dock_parts", compact, Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.VOICE_PART_SETUP)
                }
                LcdDockChip("Mix", state.activeScreen == ArrangerScreen.MIXER, "dock_audio", compact, Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.MIXER)
                }
                LcdDockChip("Menu", state.activeScreen == ArrangerScreen.MAIN_MENU, "dock_menu", compact, Modifier.weight(1f)) {
                    onOpenScreen(ArrangerScreen.MAIN_MENU)
                }
            }
        }
    }
}

@Composable
private fun LcdDockChip(
    label: String,
    highlighted: Boolean,
    tag: String,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .testTag(tag)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (highlighted) ChampagneGoldBright else Color(0xFFC5D0E2),
            fontFamily = InterFamily,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (compact) 6.sp else 7.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
