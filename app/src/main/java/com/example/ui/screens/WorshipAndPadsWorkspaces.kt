package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.WorshipSongUi
import com.example.ui.theme.ChassisBorder
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.ChassisPanel
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LcdCardBg
import com.example.ui.theme.LcdCardBorder
import com.example.ui.theme.LedAmber
import com.example.ui.theme.LedCrimson
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrNeonBlue
import com.example.ui.theme.PsrNeonBlueGlow
import com.example.ui.theme.SilkscreenMuted
import com.example.ui.theme.SilkscreenWhite
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TileRegistTop

/**
 * Church Worship Performance Playlist + Registration Memory Banks 1–4, Sequence, and Freeze.
 */
@Composable
fun WorshipAndRegistrationWorkspace(
    state: ArrangerUiState,
    onSelectBank: (Int) -> Unit,
    onRecallSlot: (Int) -> Unit,
    onSaveSlot: (Int) -> Unit,
    onStepSequence: (Int) -> Unit,
    onToggleFreeze: () -> Unit,
    onLoadWorshipSong: (WorshipSongUi) -> Unit,
    onAddCurrentAsWorshipSong: (title: String, category: String, keySig: String, notes: String) -> Unit,
    onDeleteWorshipSong: (Long) -> Unit
) {
    var newSongTitle by remember { mutableStateOf("") }
    var newSongNotes by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: Registration Banks 1–4, Sequence Stepping & Freeze
        Column(
            modifier = Modifier
                .weight(0.46f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REGISTRATION BANKS (1–4)",
                    color = PsrNeonBlueGlow,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (state.isRegistrationFreezeEnabled) PsrActiveOrange else ChassisDark)
                        .border(1.dp, if (state.isRegistrationFreezeEnabled) PsrActiveOrange else ChassisBorder, RoundedCornerShape(5.dp))
                        .testTag("regist_freeze_toggle")
                        .clickable(onClick = onToggleFreeze)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text(
                            text = if (state.isRegistrationFreezeEnabled) "FREEZE ON" else "FREEZE",
                            color = Color.White,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Bank 1..4 Selector + Sequence Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (b in 1..4) {
                    val active = state.activeRegistrationBank == b
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (active) TileRegistTop else ChassisDark)
                            .border(1.dp, if (active) LedEmerald else ChassisBorder, RoundedCornerShape(5.dp))
                            .clickable { onSelectBank(b) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("BANK $b", color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onStepSequence(-1) },
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Text("◀ PREV REGIST", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onStepSequence(1) },
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Text("NEXT REGIST ▶", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(state.registrationSlots, key = { it.slotNumber }) { slot ->
                    val isSelected = state.activeRegistrationSlot == slot.slotNumber
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) PsrNeonBlue.copy(alpha = 0.25f) else ChassisDark)
                            .border(1.dp, if (isSelected) PsrNeonBlueGlow else ChassisBorder, RoundedCornerShape(6.dp))
                            .clickable { onRecallSlot(slot.slotNumber) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "REGIST ${slot.slotNumber}: ${slot.label}",
                                color = Color.White,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (slot.isPopulated) "${slot.tempoBpm} BPM • ${slot.voiceName}" else "Empty slot — tap SAVE to store current setup",
                                color = SilkscreenMuted,
                                fontFamily = InterFamily,
                                fontSize = 9.5.sp,
                                maxLines = 1
                            )
                        }
                        Button(
                            onClick = { onSaveSlot(slot.slotNumber) },
                            colors = ButtonDefaults.buttonColors(containerColor = PsrActiveOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("SAVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Right Column: Church Worship Performance Playlist (Offline Local Room Setlist)
        Column(
            modifier = Modifier
                .weight(0.54f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CHURCH & WORSHIP SETLIST (OFFLINE READY)",
                color = PsrActiveOrange,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            // Quick Save Current Performance to Worship Playlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSongTitle,
                    onValueChange = { newSongTitle = it },
                    placeholder = { Text("Worship song title...", fontSize = 11.sp, color = SilkscreenMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PsrNeonBlueGlow,
                        unfocusedBorderColor = ChassisBorder
                    ),
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                OutlinedTextField(
                    value = newSongNotes,
                    onValueChange = { newSongNotes = it },
                    placeholder = { Text("Cue note (Intro/Key)", fontSize = 11.sp, color = SilkscreenMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PsrNeonBlueGlow,
                        unfocusedBorderColor = ChassisBorder
                    ),
                    modifier = Modifier.weight(0.9f).height(48.dp)
                )
                Button(
                    onClick = {
                        if (newSongTitle.isNotBlank()) {
                            onAddCurrentAsWorshipSong(
                                newSongTitle,
                                "Church Worship",
                                state.detectedChord.displayName,
                                newSongNotes
                            )
                            newSongTitle = ""
                            newSongNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TileRegistTop),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(48.dp).testTag("save_worship_song_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Save Song", modifier = Modifier.size(16.dp))
                    Text("SAVE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.worshipSongs, key = { it.id }) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(LcdCardBg)
                            .border(1.dp, LcdCardBorder, RoundedCornerShape(6.dp))
                            .clickable { onLoadWorshipSong(song) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TileRegistTop.copy(alpha = 0.7f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(song.keySignature, color = Color.White, fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "${song.category} • ${song.tempoBpm} BPM • TR ${if (song.transposeSemitones >= 0) "+" else ""}${song.transposeSemitones}",
                                color = PsrNeonBlueGlow,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp
                            )
                            if (song.notes.isNotBlank()) {
                                Text(
                                    text = song.notes,
                                    color = SilkscreenMuted,
                                    fontFamily = InterFamily,
                                    fontSize = 10.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { onLoadWorshipSong(song) },
                                colors = ButtonDefaults.buttonColors(containerColor = PsrNeonBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("LOAD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onDeleteWorshipSong(song.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete song", tint = LedCrimson, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-Pads 1–4 Launcher + Live Chord Looper + Interactive Chord Tutor Display.
 */
@Composable
fun MultiPadAndChordLooperWorkspace(
    state: ArrangerUiState,
    onTriggerMultiPad: (Int) -> Unit,
    onStopAllMultiPads: () -> Unit,
    onToggleChordLooperRecord: () -> Unit,
    onToggleChordLooperPlay: () -> Unit,
    onClearChordLoop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left: Multi-Pads 1–4 Performance Bank
        Column(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MULTI PAD CONTROL (1–4)",
                    color = PsrActiveOrange,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                OutlinedButton(onClick = onStopAllMultiPads, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 10.dp)) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = LedCrimson, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("STOP ALL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.multiPads.chunked(2).forEach { rowPads ->
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowPads.forEach { pad ->
                            val isActive = pad.slot in state.activeMultiPadSlots
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActive) PsrNeonBlue.copy(alpha = 0.35f) else LcdCardBg)
                                    .border(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) PsrNeonBlueGlow else LcdCardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .testTag("workspace_pad_${pad.slot}")
                                    .clickable { onTriggerMultiPad(pad.slot) }
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PAD ${pad.slot}",
                                    color = if (isActive) PsrNeonBlueGlow else PsrActiveOrange,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = pad.name,
                                    color = Color.White,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isActive) "PLAYING IN SYNC..." else "Tap to trigger synced phrase",
                                    color = if (isActive) LedEmerald else SilkscreenMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right: Chord Looper + Live Chord Tutor
        Column(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CHORD LOOPER & CHORD TUTOR",
                color = PsrNeonBlueGlow,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            // Live Chord Tutor Breakdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisDark)
                    .border(1.dp, PsrNeonBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("CURRENT DETECTED CHORD", color = SilkscreenMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.detectedChord.displayName,
                        color = Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Text(
                        text = "Notes: ${state.detectedChord.chordToneNames.joinToString(" – ")}",
                        color = LedEmerald,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Chord Looper Controls
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onToggleChordLooperRecord,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isChordLooperRecording) LedCrimson else ChassisDark
                    ),
                    modifier = Modifier.weight(1f).testTag("chord_looper_rec_btn")
                ) {
                    Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (state.isChordLooperRecording) "REC ON" else "REC CHORDS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onToggleChordLooperPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isChordLooperPlaying) TileRegistTop else PsrNeonBlue
                    ),
                    modifier = Modifier.weight(1f).testTag("chord_looper_play_btn")
                ) {
                    Icon(if (state.isChordLooperPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (state.isChordLooperPlaying) "STOP LOOP" else "LOOP PLAY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onClearChordLoop) {
                    Text("CLEAR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "RECORDED PROGRESSION (${state.chordLoopSteps.size} BARS)",
                color = SilkscreenWhite,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ChassisDark)
                    .border(1.dp, ChassisBorder, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                if (state.chordLoopSteps.isEmpty()) {
                    Text(
                        text = "Press REC CHORDS, return to keyboard and play a worship chord progression (e.g., C -> G -> Am -> F), then tap LOOP PLAY to loop chords hands-free while playing melody!",
                        color = SilkscreenMuted,
                        fontFamily = InterFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                } else {
                    Text(
                        text = state.chordLoopSteps.mapIndexed { idx, step ->
                            "Bar ${idx + 1}: ${step.chord.displayName}"
                        }.joinToString("   |   "),
                        color = LedAmber,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
