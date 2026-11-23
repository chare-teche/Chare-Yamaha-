package com.example.data.parser

import com.example.audio.LoadedWavSample
import com.example.audio.WavSampleLoader
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerSection
import com.example.domain.model.DrumSound
import com.example.domain.model.StyleDefinition
import com.example.domain.model.StyleSectionPattern
import com.example.domain.model.StyleSourceType
import com.example.domain.model.StyleStepEvent
import java.nio.charset.StandardCharsets

/**
 * Dedicated Yamaha SET & Style binary inspector and parser (.sty, .prs, .sst, .bcs, .fps, .pcs, .mid, .wav).
 * Validates MThd/MTrk headers, extracts tempo, time signature, Yamaha SFF1/SFF2 section markers,
 * CASM presence, and real channel note events while honestly reporting unsupported/companion files.
 */
object YamahaStyleParser {

    data class ParseInspectionResult(
        val displayName: String,
        val fileFormat: String,
        val isSupported: Boolean,
        val compatibilityStatus: String, // "PLAYABLE", "SAMPLE_LOADED", "UNSUPPORTED"
        val tempoBpm: Int,
        val timeSigNumerator: Int,
        val timeSigDenominator: Int,
        val sectionSummary: String,
        val diagnostics: String,
        val missingResources: String,
        val styleDefinition: StyleDefinition?,
        val loadedWavSample: LoadedWavSample? = null
    )

    fun inspectAndParse(
        id: String,
        fileName: String,
        uriString: String,
        bytes: ByteArray,
        categoryLabel: String = "Yamaha SET"
    ): ParseInspectionResult {
        val cleanName = fileName.substringBeforeLast('.').ifBlank { fileName }
        val ext = fileName.substringAfterLast('.', "").lowercase()

        if (bytes.size < 14) {
            return unsupportedResult(
                cleanName,
                ext.uppercase().ifBlank { "BIN" },
                "File too small (${bytes.size} bytes) to contain a valid Yamaha style or MIDI header.",
                "Valid MThd/RIFF header missing"
            )
        }

        // Support user-imported PCM WAV samples alongside Yamaha SET content
        if (ext == "wav") {
            val wav = WavSampleLoader.parseWav(id, fileName, bytes)
            return if (wav != null) {
                ParseInspectionResult(
                    displayName = wav.name,
                    fileFormat = "WAV_SAMPLE",
                    isSupported = true,
                    compatibilityStatus = "SAMPLE_LOADED",
                    tempoBpm = 120,
                    timeSigNumerator = 4,
                    timeSigDenominator = 4,
                    sectionSummary = "Playable Audio Sample (${wav.sampleRate} Hz)",
                    diagnostics = "PCM WAV sample loaded cleanly into User Samples voice bank.",
                    missingResources = "None",
                    styleDefinition = null,
                    loadedWavSample = wav
                )
            } else {
                unsupportedResult(
                    cleanName,
                    "WAV_UNSUPPORTED",
                    "WAV file is not uncompressed 8/16-bit PCM.",
                    "Uncompressed PCM WAV required"
                )
            }
        }

        when (ext) {
            "ppf", "cpi" -> return unsupportedResult(
                cleanName,
                "YAMAHA_${ext.uppercase()}",
                "Yamaha Expansion Pack container (.$ext). Proprietary compressed archive — extract or import unpacked .sty/.prs files.",
                "Proprietary encrypted Expansion Pack decoder not distributed"
            )
            "uvn", "vce", "tvn" -> return unsupportedResult(
                cleanName,
                "YAMAHA_VOICE",
                "Yamaha Voice/Sample bank (.$ext) detected in SET folder.",
                "Proprietary Yamaha UVN/VCE sample format — use PCM WAV samples for custom voices"
            )
            "rgt" -> return unsupportedResult(
                cleanName,
                "YAMAHA_RGT",
                "Yamaha Registration Bank (.rgt) detected. Inspection only — use ETHIO YAMAHA Banks 1–4 for local registrations.",
                "Hardware-specific RGT memory layout"
            )
            "pcg", "kmp", "ksf", "prf" -> return unsupportedResult(
                cleanName,
                "KORG_${ext.uppercase()}",
                "Korg/ORG resource (.$ext) found inside Yamaha folder. Use 'Import ORG.SET Folder' for ORG libraries.",
                "Not a Yamaha SFF style file"
            )
        }

        val magic = String(bytes, 0, 4, StandardCharsets.US_ASCII)
        if (magic != "MThd") {
            return unsupportedResult(
                cleanName,
                ext.uppercase().ifBlank { "UNKNOWN" },
                "Missing SMF 'MThd' header (found '${magic.replace(Regex("[^\\x20-\\x7E]"), ".")}'). Unsupported binary format.",
                "SMF MThd header required for style event extraction"
            )
        }

        val hasCasm = containsAscii(bytes, "CASM")
        val hasSff2 = containsAscii(bytes, "SFF2") || containsAscii(bytes, "Ctab")
        val hasOts = containsAscii(bytes, "OTSc")
        val formatLabel = when {
            ext == "pad" -> "YAMAHA_PAD"
            hasCasm && hasSff2 -> "YAMAHA_SFF2"
            hasCasm -> "YAMAHA_SFF1"
            ext in setOf("sty", "prs", "sst", "bcs", "fps", "pcs") -> "YAMAHA_STY_SMF"
            else -> "SMF_MIDI"
        }

        return try {
            parseSmfTracks(
                id = id,
                cleanName = cleanName,
                categoryLabel = categoryLabel,
                uriString = uriString,
                formatLabel = formatLabel,
                hasCasm = hasCasm,
                hasOts = hasOts,
                bytes = bytes
            )
        } catch (e: Exception) {
            unsupportedResult(
                cleanName,
                formatLabel,
                "Corrupt or unreadable MIDI/SFF track structure: ${e.message ?: "parse error"}",
                "Valid MTrk event stream"
            )
        }
    }

    private fun parseSmfTracks(
        id: String,
        cleanName: String,
        categoryLabel: String,
        uriString: String,
        formatLabel: String,
        hasCasm: Boolean,
        hasOts: Boolean,
        bytes: ByteArray
    ): ParseInspectionResult {
        val headerLength = readInt32BE(bytes, 4)
        val numTracks = readUInt16BE(bytes, 10)
        val rawDivision = readUInt16BE(bytes, 12)
        val ppqn = if ((rawDivision and 0x8000) == 0 && rawDivision > 0) rawDivision else 480

        var tempoBpm = 115
        var timeSigNum = 4
        var timeSigDen = 4

        data class RawMarker(val tick: Long, val name: String)
        data class RawNote(val tick: Long, val channel: Int, val note: Int, val velocity: Int)

        val markers = mutableListOf<RawMarker>()
        val notes = mutableListOf<RawNote>()

        var offset = 8 + headerLength
        var tracksParsed = 0

        while (offset + 8 <= bytes.size && tracksParsed < numTracks) {
            val chunkId = String(bytes, offset, 4, StandardCharsets.US_ASCII)
            val chunkLen = readInt32BE(bytes, offset + 4)
            offset += 8
            if (chunkLen < 0 || offset + chunkLen > bytes.size) break

            if (chunkId == "MTrk") {
                tracksParsed++
                val trackEnd = offset + chunkLen
                var pos = offset
                var tick = 0L
                var runningStatus = 0

                while (pos < trackEnd) {
                    val (delta, nextPos) = readVarLen(bytes, pos, trackEnd)
                    tick += delta
                    pos = nextPos
                    if (pos >= trackEnd) break

                    var status = bytes[pos].toInt() and 0xFF
                    if (status < 0x80) {
                        status = runningStatus
                    } else {
                        pos++
                        if (status < 0xF0) runningStatus = status
                    }

                    when {
                        status == 0xFF -> {
                            if (pos >= trackEnd) break
                            val metaType = bytes[pos++].toInt() and 0xFF
                            val (len, afterLen) = readVarLen(bytes, pos, trackEnd)
                            pos = afterLen
                            if (pos + len > trackEnd) break

                            when (metaType) {
                                0x51 -> if (len == 3) {
                                    val usPerBeat = ((bytes[pos].toInt() and 0xFF) shl 16) or
                                        ((bytes[pos + 1].toInt() and 0xFF) shl 8) or
                                        (bytes[pos + 2].toInt() and 0xFF)
                                    if (usPerBeat > 0) tempoBpm = (60_000_000 / usPerBeat).coerceIn(40, 240)
                                }
                                0x58 -> if (len >= 2) {
                                    timeSigNum = (bytes[pos].toInt() and 0xFF).coerceIn(2, 12)
                                    val denPow = (bytes[pos + 1].toInt() and 0xFF).coerceIn(0, 4)
                                    timeSigDen = 1 shl denPow
                                }
                                0x06, 0x01 -> {
                                    val text = String(bytes, pos, len, StandardCharsets.UTF_8).trim()
                                    if (text.isNotEmpty()) markers.add(RawMarker(tick, text))
                                }
                            }
                            pos += len
                        }
                        status == 0xF0 || status == 0xF7 -> {
                            val (len, afterLen) = readVarLen(bytes, pos, trackEnd)
                            pos = (afterLen + len).coerceAtMost(trackEnd)
                        }
                        (status and 0xF0) == 0x90 -> {
                            if (pos + 2 > trackEnd) break
                            val note = bytes[pos++].toInt() and 0x7F
                            val vel = bytes[pos++].toInt() and 0x7F
                            if (vel > 0) notes.add(RawNote(tick, status and 0x0F, note, vel))
                        }
                        (status and 0xF0) in listOf(0x80, 0xA0, 0xB0, 0xE0) -> {
                            pos = (pos + 2).coerceAtMost(trackEnd)
                        }
                        (status and 0xF0) in listOf(0xC0, 0xD0) -> {
                            pos = (pos + 1).coerceAtMost(trackEnd)
                        }
                        else -> break
                    }
                }
            }
            offset += chunkLen
        }

        if (notes.isEmpty()) {
            return unsupportedResult(
                cleanName,
                formatLabel,
                "Valid SMF header found ($tracksParsed tracks, PPQN=$ppqn), but no playable MIDI note events were extracted.",
                "Playable MIDI note events"
            )
        }

        val stepsPerBar = if ((timeSigDen == 8 && timeSigNum == 6) || timeSigNum == 3) 12 else 16
        val ticksPerBar = (ppqn.toLong() * timeSigNum * 4L / timeSigDen.coerceAtLeast(1)).coerceAtLeast(ppqn.toLong())
        val ticksPerStep = (ticksPerBar.toDouble() / stepsPerBar).coerceAtLeast(1.0)

        val sortedMarkers = markers
            .mapNotNull { m -> mapYamahaMarker(m.name)?.let { sec -> m.tick to sec } }
            .sortedBy { it.first }

        val sectionsMap = mutableMapOf<ArrangerSection, StyleSectionPattern>()

        if (sortedMarkers.isNotEmpty()) {
            for (i in sortedMarkers.indices) {
                val (startTick, section) = sortedMarkers[i]
                val endTick = if (i + 1 < sortedMarkers.size) sortedMarkers[i + 1].first else (notes.maxOf { it.tick } + ticksPerBar)
                val barSpan = (((endTick - startTick).coerceAtLeast(ticksPerBar)) / ticksPerBar).toInt().coerceIn(1, 4)
                val totalSteps = barSpan * stepsPerBar

                val sectionEvents = notes
                    .filter { it.tick in startTick until endTick }
                    .mapNotNull { n ->
                        val stepIdx = (((n.tick - startTick) / ticksPerStep).toInt()).coerceIn(0, totalSteps - 1)
                        convertMidiNoteToStepEvent(stepIdx, n.channel, n.note, n.velocity)
                    }
                    .take(280)

                if (sectionEvents.isNotEmpty()) {
                    sectionsMap[section] = StyleSectionPattern(section, barSpan, sectionEvents)
                }
            }
        }

        if (sectionsMap.isEmpty()) {
            val firstBarEvents = notes
                .filter { it.tick < ticksPerBar * 2 }
                .mapNotNull { n ->
                    val stepIdx = ((n.tick / ticksPerStep).toInt()) % stepsPerBar
                    convertMidiNoteToStepEvent(stepIdx, n.channel, n.note, n.velocity)
                }
                .take(280)
            sectionsMap[ArrangerSection.MAIN_A] = StyleSectionPattern(ArrangerSection.MAIN_A, 1, firstBarEvents)
            sectionsMap[ArrangerSection.MAIN_B] = StyleSectionPattern(ArrangerSection.MAIN_B, 1, firstBarEvents)
        }

        val detectedNames = sectionsMap.keys.joinToString(", ") { it.displayLabel }
        val casmTag = if (hasCasm) "CASM detected" else "No CASM chunk"
        val otsTag = if (hasOts) "OTSc chunk present" else "Default OTS mapping"
        val diagnostics = "Parsed $formatLabel: $tracksParsed tracks, ${notes.size} note events, PPQN=$ppqn, $tempoBpm BPM ($timeSigNum/$timeSigDen). $casmTag • $otsTag. Sections: $detectedNames."
        val missing = if (hasCasm) {
            "Hardware MegaVoice/Expansion kits mapped to built-in GM/Ethio synth voices"
        } else {
            "No Yamaha CASM chord table; uses root-transposition playback"
        }

        val styleDef = StyleDefinition(
            id = id,
            name = cleanName,
            category = categoryLabel,
            tempoBpm = tempoBpm,
            timeSigNumerator = timeSigNum,
            timeSigDenominator = timeSigDen,
            stepsPerBar = stepsPerBar,
            sourceType = if (formatLabel.startsWith("YAMAHA")) StyleSourceType.IMPORTED_YAMAHA_STY else StyleSourceType.IMPORTED_SMF,
            sourceUri = uriString,
            fileFormat = formatLabel,
            parseDiagnostics = diagnostics,
            sections = sectionsMap
        )

        return ParseInspectionResult(
            displayName = cleanName,
            fileFormat = formatLabel,
            isSupported = true,
            compatibilityStatus = "PLAYABLE",
            tempoBpm = tempoBpm,
            timeSigNumerator = timeSigNum,
            timeSigDenominator = timeSigDen,
            sectionSummary = detectedNames.ifBlank { "MAIN A" },
            diagnostics = diagnostics,
            missingResources = missing,
            styleDefinition = styleDef
        )
    }

    private fun convertMidiNoteToStepEvent(
        stepIndex: Int,
        channel: Int,
        midiNote: Int,
        velocity: Int
    ): StyleStepEvent {
        val vel = (velocity / 127f).coerceIn(0.2f, 1f)
        if (channel == 9 || channel == 8) {
            return StyleStepEvent(
                stepIndex = stepIndex,
                part = if (channel == 8) AccompPart.PERCUSSION else AccompPart.DRUMS,
                drumSound = mapGmDrum(midiNote),
                velocity = vel
            )
        }
        val part = when (channel) {
            10 -> AccompPart.BASS
            11 -> AccompPart.CHORD_1
            12 -> AccompPart.CHORD_2
            13 -> AccompPart.PAD
            14 -> AccompPart.PHRASE_1
            15 -> AccompPart.PHRASE_2
            else -> if (midiNote < 48) AccompPart.BASS else AccompPart.CHORD_1
        }
        return StyleStepEvent(
            stepIndex = stepIndex,
            part = part,
            velocity = vel * 0.85f,
            durationSteps = if (part == AccompPart.PAD) 4 else 2,
            absoluteMidiNote = midiNote
        )
    }

    private fun mapGmDrum(note: Int): DrumSound = when (note) {
        35, 36 -> DrumSound.KICK
        37 -> DrumSound.RIM
        38, 40 -> DrumSound.SNARE
        39 -> DrumSound.CLAP
        42, 44 -> DrumSound.CLOSED_HAT
        46 -> DrumSound.OPEN_HAT
        49, 51, 52, 55, 57 -> DrumSound.CRASH
        41, 43, 45, 61, 64 -> DrumSound.KEBERO_LOW
        47, 48, 50, 60, 62, 63 -> DrumSound.KEBERO_HIGH
        69, 70, 82 -> DrumSound.SHAKER
        else -> DrumSound.KEBERO_HIGH
    }

    private fun mapYamahaMarker(marker: String): ArrangerSection? {
        val m = marker.trim().uppercase()
        return when {
            m.startsWith("INTRO A") || m == "INTRO 1" -> ArrangerSection.INTRO_1
            m.startsWith("INTRO B") || m == "INTRO 2" -> ArrangerSection.INTRO_2
            m.startsWith("INTRO C") || m == "INTRO 3" -> ArrangerSection.INTRO_3
            m.startsWith("MAIN A") -> ArrangerSection.MAIN_A
            m.startsWith("MAIN B") -> ArrangerSection.MAIN_B
            m.startsWith("MAIN C") -> ArrangerSection.MAIN_C
            m.startsWith("MAIN D") -> ArrangerSection.MAIN_D
            m.startsWith("FILL IN AA") || m == "FILL A" -> ArrangerSection.FILL_A
            m.startsWith("FILL IN BB") || m == "FILL B" -> ArrangerSection.FILL_B
            m.startsWith("FILL IN CC") || m == "FILL C" -> ArrangerSection.FILL_C
            m.startsWith("FILL IN DD") || m == "FILL D" -> ArrangerSection.FILL_D
            m.startsWith("FILL IN BA") || m.contains("BREAK") -> ArrangerSection.BREAK
            m.startsWith("ENDING A") || m == "ENDING 1" -> ArrangerSection.ENDING_1
            m.startsWith("ENDING B") || m == "ENDING 2" -> ArrangerSection.ENDING_2
            m.startsWith("ENDING C") || m == "ENDING 3" -> ArrangerSection.ENDING_3
            else -> null
        }
    }

    private fun unsupportedResult(name: String, format: String, reason: String, missing: String) =
        ParseInspectionResult(
            displayName = name,
            fileFormat = format,
            isSupported = false,
            compatibilityStatus = "UNSUPPORTED",
            tempoBpm = 120,
            timeSigNumerator = 4,
            timeSigDenominator = 4,
            sectionSummary = "Unsupported",
            diagnostics = reason,
            missingResources = missing,
            styleDefinition = null
        )

    private fun containsAscii(bytes: ByteArray, needle: String): Boolean {
        val target = needle.toByteArray(StandardCharsets.US_ASCII)
        if (target.isEmpty() || bytes.size < target.size) return false
        outer@ for (i in 0..(bytes.size - target.size)) {
            for (j in target.indices) {
                if (bytes[i + j] != target[j]) continue@outer
            }
            return true
        }
        return false
    }

    private fun readInt32BE(b: ByteArray, offset: Int): Int =
        ((b[offset].toInt() and 0xFF) shl 24) or
            ((b[offset + 1].toInt() and 0xFF) shl 16) or
            ((b[offset + 2].toInt() and 0xFF) shl 8) or
            (b[offset + 3].toInt() and 0xFF)

    private fun readUInt16BE(b: ByteArray, offset: Int): Int =
        ((b[offset].toInt() and 0xFF) shl 8) or (b[offset + 1].toInt() and 0xFF)

    private fun readVarLen(b: ByteArray, start: Int, limit: Int): Pair<Int, Int> {
        var value = 0
        var pos = start
        var count = 0
        while (pos < limit && count < 4) {
            val byte = b[pos++].toInt() and 0xFF
            value = (value shl 7) or (byte and 0x7F)
            count++
            if ((byte and 0x80) == 0) break
        }
        return value to pos
    }
}
