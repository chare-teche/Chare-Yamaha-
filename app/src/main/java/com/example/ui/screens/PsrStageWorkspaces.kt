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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerScreen
import com.example.domain.model.ArrangerSection
import com.example.domain.model.ArrangerUiState
import com.example.domain.model.DetectedChord
import com.example.domain.model.KeyboardPartTarget
import com.example.domain.model.RegistrationFreezeTarget
import com.example.domain.model.SplitChordMode
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldBright
import com.example.ui.theme.ChampagneGoldContainer
import com.example.ui.theme.ChassisBorder
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.ChassisPanel
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.GraphiteElevated
import com.example.ui.theme.GraphitePanel
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LcdCardBorder
import com.example.ui.theme.LedAmber
import com.example.ui.theme.LedCrimson
import com.example.ui.theme.LedEmerald
import com.example.ui.theme.PsrActiveOrange
import com.example.ui.theme.PsrNeonBlue
import com.example.ui.theme.SilkscreenMuted
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.StatusLiveGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TileRegistTop

private data class MenuTileItem(
    val title: String,
    val subtitle: String,
    val screen: ArrangerScreen,
    val icon: ImageVector,
    val isPlanned: Boolean = false
)

@Composable
fun MainMenuWorkspace(
    state: ArrangerUiState,
    onSetMainMenuPage: (Int) -> Unit,
    onOpenScreen: (ArrangerScreen) -> Unit
) {
    val page1Items = remember {
        listOf(
            MenuTileItem("Channel On/Off", "8 Accomp buses + Solo/Mute", ArrangerScreen.CHANNEL_ON_OFF, Icons.Default.Tune),
            MenuTileItem("Voice Part Setup", "LEFT & RIGHT 1–3 routing", ArrangerScreen.VOICE_PART_SETUP, Icons.Default.Piano),
            MenuTileItem("Mixer / EQ", "9-bus faders, pan, FX & EQ", ArrangerScreen.MIXER, Icons.Default.Equalizer),
            MenuTileItem("Split & Fingering", "ACMP, split point, chord mode", ArrangerScreen.SPLIT_FINGERING, Icons.Default.GridView),
            MenuTileItem("Registration Memory", "4 banks x 8 live memory slots", ArrangerScreen.REGISTRATION_MEMORY, Icons.Default.Bookmark),
            MenuTileItem("Registration Freeze", "Lock Style/Voice/Tempo/Trans/Split", ArrangerScreen.REGISTRATION_FREEZE, Icons.Default.Lock),
            MenuTileItem("Registration Sequence", "Custom ordered registration chain", ArrangerScreen.REGISTRATION_SEQUENCE, Icons.Default.Repeat),
            MenuTileItem("Style Settings", "Sections, Fill, Sync, Balance, OTS", ArrangerScreen.STYLE_SETTINGS, Icons.Default.MusicNote),
            MenuTileItem("Multi Pads", "4 synced phrases & Chord Looper", ArrangerScreen.MULTI_PAD_LOOPER, Icons.Default.GraphicEq),
            MenuTileItem("Church Worship", "Offline setlist & key presets", ArrangerScreen.PLAYLIST, Icons.AutoMirrored.Filled.QueueMusic)
        )
    }
    val page2Items = remember {
        listOf(
            MenuTileItem("Style & SET Library", "Built-in, Yamaha STY & ORG.SET", ArrangerScreen.STYLE_LIBRARY, Icons.Default.LibraryMusic),
            MenuTileItem("Voice Selection", "Browse all voices & WAV samples", ArrangerScreen.VOICE_SELECT, Icons.Default.Piano),
            MenuTileItem("Song Player", "SMF / Audio playback deck", ArrangerScreen.SONG_PLAYER, Icons.Default.PlayArrow, isPlanned = true),
            MenuTileItem("Song Recording", "Audio & MIDI performance capture", ArrangerScreen.SONG_RECORDING, Icons.Default.FiberManualRecord, isPlanned = true),
            MenuTileItem("MIDI", "USB / Bluetooth MIDI device status", ArrangerScreen.MIDI_SETTINGS, Icons.Default.Usb),
            MenuTileItem("File Manager", "Import SAF SET folders & files", ArrangerScreen.FILE_MANAGER, Icons.Default.FolderOpen),
            MenuTileItem("Utility", "Keyboard compass, curves & audio", ArrangerScreen.SETTINGS, Icons.Default.Settings),
            MenuTileItem("System Settings", "Master Tune, latency & scope", ArrangerScreen.SYSTEM_SETTINGS, Icons.Default.Tune)
        )
    }

    val items = if (state.mainMenuPage == 1) page1Items else page2Items

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PSR-SX900 WORKSTATION MENU — PAGE ${state.mainMenuPage} OF 2",
                color = ChampagneGoldBright,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MenuTabButton("MENU 1", state.mainMenuPage == 1, "menu_page_1") { onSetMainMenuPage(1) }
                MenuTabButton("MENU 2", state.mainMenuPage == 2, "menu_page_2") { onSetMainMenuPage(2) }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 155.dp),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.screen.name }) { item ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GraphitePanel)
                        .border(
                            1.dp,
                            if (item.isPlanned) StatusWarningAmber.copy(alpha = 0.5f) else LcdCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("menu_tile_${item.screen.name.lowercase()}")
                        .clickable { onOpenScreen(item.screen) }
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (item.isPlanned) StatusWarningAmber else ElectricBlueGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        if (item.isPlanned) {
                            Text(
                                text = "PLANNED",
                                color = StatusWarningAmber,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ChampagneGoldContainer)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subtitle,
                        color = SilkscreenMuted,
                        fontFamily = InterFamily,
                        fontSize = 9.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelOnOffWorkspace(
    state: ArrangerUiState,
    onToggleChannelMute: (AccompPart) -> Unit,
    onToggleChannelSolo: (AccompPart) -> Unit,
    onClearAccompSolo: () -> Unit,
    onTogglePartEnabled: (KeyboardPartTarget) -> Unit,
    onOpenMixer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "8-CHANNEL STYLE ACCOMPANIMENT ON/OFF",
                color = ElectricBlueGlow,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (state.soloedAccompPart != null) {
                    OutlinedButton(onClick = onClearAccompSolo, modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("CLEAR SOLO", color = PsrActiveOrange, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(onClick = onOpenMixer, modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("OPEN MIXER", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 8 Accompaniment Channels in 2 clean rows of 4 (works smoothly in both landscape and portrait)
        state.eightAccompChannels.chunked(4).forEach { rowChannels ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowChannels.forEach { ch ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChassisPanel)
                            .border(
                                1.dp,
                                when {
                                    ch.isSoloed -> ChampagneGoldBright
                                    ch.isMuted -> LedCrimson.copy(alpha = 0.55f)
                                    else -> LcdCardBorder
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = ch.part.psrChannelName.uppercase(),
                            color = if (ch.isSoloed) ChampagneGoldBright else Color.White,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "VOL ${(ch.volume * 127).toInt()} • ${if (ch.isMuted) "MUTED" else "ACTIVE"}",
                            color = if (ch.isMuted) LedCrimson else StatusLiveGreen,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (ch.isMuted) LedCrimson else TileRegistTop)
                                    .clickable { onToggleChannelMute(ch.part) }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (ch.isMuted) "OFF" else "ON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (ch.isSoloed) ChampagneGold else ChassisDark)
                                    .border(1.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clickable { onToggleChannelSolo(ch.part) }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SOLO",
                                    color = if (ch.isSoloed) Color.Black else ChampagneGoldBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "KEYBOARD PARTS (4-PART ROUTING)",
            color = ChampagneGoldBright,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KeyboardPartTarget.entries.forEach { target ->
                val cfg = state.partConfig(target)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (cfg.isEnabled) ElectricBlueContainer else ChassisPanel)
                        .border(1.dp, if (cfg.isEnabled) ElectricBlueGlow else ChassisBorder, RoundedCornerShape(7.dp))
                        .clickable { onTogglePartEnabled(target) }
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(target.label, color = ChampagneGoldBright, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text(cfg.voice.name, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (cfg.isEnabled) "ON" else "OFF", color = if (cfg.isEnabled) LedEmerald else LedCrimson, fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VoicePartSetupWorkspace(
    state: ArrangerUiState,
    onTogglePartEnabled: (KeyboardPartTarget) -> Unit,
    onToggleKeyboardPartSolo: (KeyboardPartTarget) -> Unit,
    onSetPartVolume: (KeyboardPartTarget, Float) -> Unit,
    onSetPartPan: (KeyboardPartTarget, Float) -> Unit,
    onSetPartOctave: (KeyboardPartTarget, Int) -> Unit,
    onSetPartNoteRange: (KeyboardPartTarget, Int, Int) -> Unit,
    onToggleHarmony: () -> Unit,
    onOpenVoiceBrowser: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("FOUR-PART KEYBOARD SETUP (LEFT, R1, R2, R3)", color = ElectricBlueGlow, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    text = "Each part has independent enable, voice, volume, pan, octave & range. Note: R1/R2/R3 share the RIGHT_VOICE bus and LEFT routes to the PAD bus in SynthAudioEngine.",
                    color = SilkscreenMuted, fontSize = 9.5.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MenuTabButton(if (state.isHarmonyEnabled) "HARMONY ON" else "HARMONY OFF", state.isHarmonyEnabled, "setup_harmony_btn", onToggleHarmony)
                MenuTabButton("ASSIGN VOICES", true, "setup_voice_browser_btn", onOpenVoiceBrowser)
            }
        }

        KeyboardPartTarget.entries.forEach { target ->
            val cfg = state.partConfig(target)
            val isSolo = state.soloedKeyboardPart == target
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisPanel)
                    .border(1.dp, if (isSolo) ChampagneGoldBright else LcdCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${target.label} — ${cfg.voice.name}", color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Text("Audio Bus: ${if (target == KeyboardPartTarget.LEFT) "PAD" else "RIGHT_VOICE"} • Range: ${cfg.rangeLabel}", color = SilkscreenMuted, fontFamily = JetBrainsMonoFamily, fontSize = 9.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        MenuTabButton(if (cfg.isEnabled) "ON" else "OFF", cfg.isEnabled, "setup_toggle_${target.shortLabel.lowercase()}") { onTogglePartEnabled(target) }
                        MenuTabButton(if (isSolo) "SOLO ON" else "SOLO", isSolo, "setup_solo_${target.shortLabel.lowercase()}") { onToggleKeyboardPartSolo(target) }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VOL ${(cfg.volume * 100).toInt()}%", color = Color.White, fontFamily = JetBrainsMonoFamily, fontSize = 9.sp)
                        Slider(value = cfg.volume, onValueChange = { onSetPartVolume(target, it) }, modifier = Modifier.height(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        val panTxt = if (cfg.pan < -0.05f) "L${(-cfg.pan * 64).toInt()}" else if (cfg.pan > 0.05f) "R${(cfg.pan * 64).toInt()}" else "C"
                        Text("PAN $panTxt", color = Color.White, fontFamily = JetBrainsMonoFamily, fontSize = 9.sp)
                        Slider(value = cfg.pan, valueRange = -1f..1f, onValueChange = { onSetPartPan(target, it) }, modifier = Modifier.height(22.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        MenuTabButton("-", false, "") { onSetPartOctave(target, cfg.octaveShift - 1) }
                        Text("OCT ${if (cfg.octaveShift > 0) "+" else ""}${cfg.octaveShift}", color = ChampagneGoldBright, fontFamily = JetBrainsMonoFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        MenuTabButton("+", false, "") { onSetPartOctave(target, cfg.octaveShift + 1) }
                        MenuTabButton("FULL", false, "") { onSetPartNoteRange(target, 21, 108) }
                    }
                }
            }
        }
    }
}

@Composable
fun SplitAndFingeringWorkspace(
    state: ArrangerUiState,
    onToggleAcmp: () -> Unit,
    onToggleSplit: () -> Unit,
    onSetChordMode: (SplitChordMode) -> Unit,
    onSetSplitNote: (Int) -> Unit,
    onAdjustOctave: (Int) -> Unit,
    onAdjustTranspose: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("SPLIT POINT & CHORD FINGERING", color = ChampagneGoldBright, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisPanel)
                    .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACMP Auto-Chord", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Drive style chord changes from chord zone", color = SilkscreenMuted, fontSize = 9.5.sp)
                }
                Switch(checked = state.isAcmpEnabled, onCheckedChange = { onToggleAcmp() }, colors = SwitchDefaults.colors(checkedTrackColor = TileRegistTop))
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisPanel)
                    .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Keyboard Split", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("LEFT <= ${DetectedChord.midiNoteName(state.splitNoteMidi)} < RIGHT 1–3", color = SilkscreenMuted, fontSize = 9.5.sp)
                }
                Switch(checked = state.isSplitEnabled, onCheckedChange = { onToggleSplit() }, colors = SwitchDefaults.colors(checkedTrackColor = TileRegistTop))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Split Point Note", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(DetectedChord.midiNoteName(state.splitNoteMidi), color = ElectricBlueGlow, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Slider(
                value = state.splitNoteMidi.toFloat(),
                valueRange = 48f..72f,
                onValueChange = { onSetSplitNote(it.toInt()) },
                colors = SliderDefaults.colors(thumbColor = PsrActiveOrange, activeTrackColor = ElectricBlueGlow)
            )
            Text("Chord Recognition Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            SplitChordMode.entries.forEach { mode ->
                val selected = state.chordMode == mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) ElectricBlueContainer else ChassisDark)
                        .border(1.dp, if (selected) ElectricBlueGlow else ChassisBorder, RoundedCornerShape(6.dp))
                        .clickable { onSetChordMode(mode) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mode.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        Text(mode.description, color = SilkscreenMuted, fontSize = 9.5.sp)
                    }
                    Text(if (selected) "SELECTED" else "SELECT", color = if (selected) ChampagneGoldBright else SilkscreenMuted, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisPanel)
                    .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Global Octave: ${if (state.octaveShift > 0) "+" else ""}${state.octaveShift}", color = Color.White, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MenuTabButton("-1", false, "") { onAdjustOctave(-1) }
                    MenuTabButton("+1", false, "") { onAdjustOctave(1) }
                }
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisPanel)
                    .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transpose: ${if (state.transposeSemitones > 0) "+" else ""}${state.transposeSemitones}", color = Color.White, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MenuTabButton("-1", false, "") { onAdjustTranspose(-1) }
                    MenuTabButton("+1", false, "") { onAdjustTranspose(1) }
                }
            }
        }
    }
}

@Composable
fun RegistrationFreezeWorkspace(
    state: ArrangerUiState,
    onToggleRegFreeze: () -> Unit,
    onToggleFreezeTarget: (RegistrationFreezeTarget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (state.isRegistrationFreezeEnabled) ChampagneGoldContainer else ChassisPanel)
                .border(1.dp, if (state.isRegistrationFreezeEnabled) ChampagneGoldBright else LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("MASTER REGISTRATION FREEZE", color = ChampagneGoldBright, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Active Protected Targets: ${state.activeFreezeTargetsSummary}", color = Color.White, fontSize = 10.5.sp)
            }
            Switch(
                checked = state.isRegistrationFreezeEnabled,
                onCheckedChange = { onToggleRegFreeze() },
                colors = SwitchDefaults.colors(checkedTrackColor = PsrActiveOrange)
            )
        }

        RegistrationFreezeTarget.entries.forEach { target ->
            val enabled = state.isFreezeTargetEnabled(target)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisPanel)
                    .border(1.dp, if (enabled) ElectricBlueGlow else LcdCardBorder, RoundedCornerShape(8.dp))
                    .clickable { onToggleFreezeTarget(target) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(target.displayName, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    Text(target.description, color = SilkscreenMuted, fontSize = 10.sp)
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggleFreezeTarget(target) },
                    colors = SwitchDefaults.colors(checkedTrackColor = ElectricBlue)
                )
            }
        }
    }
}

@Composable
fun RegistrationSequenceWorkspace(
    state: ArrangerUiState,
    onToggleSequenceEnabled: () -> Unit,
    onAddStep: (Int, Int) -> Unit,
    onRemoveStepAt: (Int) -> Unit,
    onMoveStep: (Int, Int) -> Unit,
    onClearSequence: () -> Unit,
    onJumpToStep: (Int) -> Unit,
    onStepSequence: (Int) -> Unit
) {
    var pickBank by remember { mutableIntStateOf(state.activeRegistrationBank) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("REGISTRATION SEQUENCE", color = ChampagneGoldBright, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                Text("${state.registrationSequence.size} steps • Step Prev/Next cycles your song order", color = SilkscreenMuted, fontSize = 10.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = state.isRegistrationSequenceEnabled, onCheckedChange = { onToggleSequenceEnabled() })
                OutlinedButton(onClick = { onStepSequence(-1) }, modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("◀ PREV", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { onStepSequence(1) }, modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("NEXT ▶", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
                if (state.registrationSequence.isNotEmpty()) {
                    OutlinedButton(onClick = onClearSequence, modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("CLEAR", color = LedCrimson, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Picker bar: Bank 1-4 + Add Slot 1-8
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(GraphitePanel)
                .border(1.dp, ChassisBorder, RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ADD:", color = ChampagneGoldBright, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            for (b in 1..4) {
                MenuTabButton("B$b", pickBank == b, "") { pickBank = b }
            }
            Spacer(Modifier.width(4.dp))
            for (slot in 1..8) {
                MenuTabButton("+$slot", false, "") { onAddStep(pickBank, slot) }
            }
        }

        if (state.registrationSequence.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChassisPanel)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sequence steps yet. Choose Bank B1–B4 and tap +1..+8 above to build your live performance sequence.",
                    color = SilkscreenMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(state.registrationSequence) { idx, step ->
                    val active = idx == state.activeSequenceIndex && state.isRegistrationSequenceEnabled
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (active) ElectricBlueContainer else ChassisPanel)
                            .border(1.dp, if (active) ElectricBlueGlow else LcdCardBorder, RoundedCornerShape(7.dp))
                            .clickable { onJumpToStep(idx) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "#${idx + 1}  •  Bank ${step.bankNumber} — Registration ${step.slotNumber} (${step.shortLabel})",
                            color = Color.White,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Row {
                            IconButton(onClick = { onMoveStep(idx, idx - 1) }, enabled = idx > 0, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onMoveStep(idx, idx + 1) }, enabled = idx < state.registrationSequence.lastIndex, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onRemoveStepAt(idx) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Step", tint = LedCrimson, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StyleSettingsWorkspace(
    state: ArrangerUiState,
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
    onSetAcmpBalance: (Float) -> Unit,
    onSetStyleVolume: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("STYLE ENGINE & ARRANGER CONTROL", color = PsrActiveOrange, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(
            text = "${state.activeStyle.name} (${state.activeStyle.category}) • ${state.tempoBpm} BPM • ${state.activeStyle.timeSignatureLabel} • ${state.activeStyle.parseDiagnostics}",
            color = SilkscreenMuted,
            fontSize = 10.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuTabButton(if (state.isPlaying) "STOP STYLE" else "START STYLE", state.isPlaying, "", onStartStop)
            MenuTabButton("SYNC START", state.isSyncStartArmed, "", onSyncStart)
            MenuTabButton("SYNC STOP", state.isSyncStopArmed, "", onSyncStop)
            MenuTabButton("AUTO FILL", state.isAutoFillEnabled, "", onAutoFill)
            MenuTabButton("FADE", state.isFadeActive, "", onFadeInOut)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuTabButton("TEMPO LOCK", state.isTempoLocked, "", onToggleTempoLock)
            MenuTabButton("OTS LINK", state.isOtsLinkEnabled, "", onToggleOtsLink)
            MenuTabButton("TAP BPM", false, "", onTapTempo)
            MenuTabButton("RESET BPM", false, "", onResetTempo)
        }

        Text("SECTIONS", color = ChampagneGoldBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(ArrangerSection.INTRO_1, ArrangerSection.INTRO_2, ArrangerSection.INTRO_3).forEach { sec ->
                MenuTabButton(sec.displayLabel, state.activeSection == sec, "") { onIntro(sec) }
            }
            listOf(ArrangerSection.MAIN_A, ArrangerSection.MAIN_B, ArrangerSection.MAIN_C, ArrangerSection.MAIN_D).forEach { sec ->
                MenuTabButton(sec.displayLabel, state.activeSection == sec, "") { onMainVariation(sec) }
            }
            MenuTabButton("BREAK", state.activeSection == ArrangerSection.BREAK, "", onBreak)
            listOf(ArrangerSection.ENDING_1, ArrangerSection.ENDING_2, ArrangerSection.ENDING_3).forEach { sec ->
                MenuTabButton(sec.displayLabel, state.activeSection == sec, "") { onEnding(sec) }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Style Volume: ${(state.styleVolume * 100).toInt()}%", color = Color.White, fontFamily = JetBrainsMonoFamily, fontSize = 10.5.sp)
            Slider(value = state.styleVolume, onValueChange = onSetStyleVolume)
            Text("Acmp / Keyboard Balance: ${(state.acmpBalance * 100).toInt()}%", color = Color.White, fontFamily = JetBrainsMonoFamily, fontSize = 10.5.sp)
            Slider(value = state.acmpBalance, onValueChange = onSetAcmpBalance)
        }
    }
}

@Composable
fun MidiWorkspace(state: ArrangerUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("MIDI & HARDWARE CONTROLLER STATUS", color = ElectricBlueGlow, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (state.connectedMidiDevices.isEmpty()) "No external USB/Bluetooth MIDI controllers detected." else "Connected Devices:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            state.connectedMidiDevices.forEach { dev ->
                Text("• $dev", color = LedEmerald, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp)
            }
            Text(
                text = "Live MIDI Input Behavior: Incoming Note On/Off feeds Chord Detector (${state.chordMode.displayName}), Split Zone (${DetectedChord.midiNoteName(state.splitNoteMidi)}), 4-Part Router (LEFT + R1/R2/R3), CC64 Sustain, and Pitch Bend.",
                color = SilkscreenMuted,
                fontSize = 10.5.sp
            )
            Text(
                text = "Current Live Telemetry: Pitch Bend ${String.format("%+.2f", state.pitchBendSemitones)} st • Sustain ${if (state.isSustainEnabled) "ON" else "OFF"} • Last Chord ${state.detectedChord.displayName}",
                color = ChampagneGoldBright,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun FileManagerWorkspace(
    state: ArrangerUiState,
    onImportYamahaSetClick: () -> Unit,
    onImportOrgSetClick: () -> Unit,
    onImportSingleFileClick: () -> Unit,
    onRemoveSource: (Long) -> Unit,
    onOpenStyleLibrary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("SET & SAMPLE FILE MANAGER", color = ChampagneGoldBright, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onImportYamahaSetClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PsrActiveOrange)) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Yamaha SET", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onImportOrgSetClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PsrNeonBlue)) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("ORG.SET", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onImportSingleFileClick, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AudioFile, contentDescription = null, tint = LedEmerald, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(".STY/.MID/.WAV", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onOpenStyleLibrary) {
                Text("VIEW STYLES", color = ChampagneGoldBright, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.importedSources, key = { it.id }) { src ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChassisPanel)
                        .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${src.displayName} [${src.setFamily}]", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${src.totalFiles} scanned • ${src.supportedStyles} styles • ${src.supportedSamples} samples • ${src.unsupportedFiles} unsupported", color = SilkscreenMuted, fontSize = 10.sp)
                        Text(src.inspectionSummary, color = LedAmber, fontSize = 9.5.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { onRemoveSource(src.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = LedCrimson)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemSettingsWorkspace(
    state: ArrangerUiState,
    onSetMasterTuneHz: (Float) -> Unit,
    onSetAudioBufferFrames: (Int) -> Unit,
    onTogglePanelLock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("SYSTEM SETTINGS & AUDIO ENGINE", color = ElectricBlueGlow, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ChassisPanel)
                .border(1.dp, LcdCardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Master Tune (A4): ${String.format("%.1f Hz", state.masterTuneHz)}", color = Color.White, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Slider(value = state.masterTuneHz, valueRange = 415f..466f, onValueChange = onSetMasterTuneHz)

            Text("AudioTrack Buffer Frames", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(256 to "256 (~6ms)", 512 to "512 (~12ms)", 1024 to "1024 (~23ms)").forEach { (frames, lbl) ->
                    MenuTabButton(lbl, state.audioBufferFrames == frames, "") { onSetAudioBufferFrames(frames) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Panel Lock (Live Protection)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                Switch(checked = state.isPanelLocked, onCheckedChange = { onTogglePanelLock() })
            }

            Text(
                text = "Engine: ${if (state.audioEngineRunning) "RUNNING (44.1 kHz 16-bit PCM AudioTrack)" else "STANDBY"} • Synth voice pool: 64 melodic + 16 drum slots (device polyphony depends on CPU headroom).",
                color = SilkscreenMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun PlannedFeatureWorkspace(
    title: String,
    summary: String,
    details: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(10.dp))
                .background(ChassisPanel)
                .border(1.dp, StatusWarningAmber.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("PLANNED WORKSTATION MODULE", color = StatusWarningAmber, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(title, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(summary, color = ChampagneGoldBright, fontSize = 12.sp)
            Text(details, color = SilkscreenMuted, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun MenuTabButton(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) ElectricBlue else ChassisDark)
            .border(1.dp, if (selected) ChampagneGoldBright else GraphiteElevated, RoundedCornerShape(5.dp))
            .then(if (tag.isNotEmpty()) Modifier.testTag(tag) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, maxLines = 1)
    }
}
