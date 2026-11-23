package com.example.data.parser

import com.example.audio.LoadedWavSample
import com.example.audio.WavSampleLoader
import com.example.domain.model.StyleDefinition
import com.example.domain.model.StyleSourceType
import java.nio.charset.StandardCharsets

/**
 * Dedicated inspector and parser for ORG.SET / Korg-style mobile arranger SET folders and files.
 * Kept strictly separate from YamahaStyleParser so ORG.SET directory conventions
 * (STYLE/, SOUND/, PROGRAM/, MULTISMP/, PCM/, DRUM/, .ost, .sty, .wav, .sf2, .pcg, .kmp)
 * are inspected honestly with clear playback compatibility and missing-resource reporting.
 */
object OrgSetParser {

    data class OrgInspectionResult(
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
        val styleDefinition: StyleDefinition? = null,
        val loadedWavSample: LoadedWavSample? = null
    )

    fun inspectOrgFile(
        id: String,
        fileName: String,
        parentFolder: String,
        uriString: String,
        bytes: ByteArray
    ): OrgInspectionResult {
        val cleanName = fileName.substringBeforeLast('.').ifBlank { fileName }
        val ext = fileName.substringAfterLast('.', "").lowercase()

        if (bytes.size < 12) {
            return unsupported(
                cleanName,
                "ORG_${ext.uppercase().ifBlank { "BIN" }}",
                "File is empty or truncated (${bytes.size} bytes).",
                "Valid header missing"
            )
        }

        // 1. Compatible PCM WAV drum/instrument sample inside ORG.SET
        if (ext == "wav") {
            val wav = WavSampleLoader.parseWav(id, fileName, bytes)
            return if (wav != null) {
                OrgInspectionResult(
                    displayName = wav.name,
                    fileFormat = "ORG_WAV_SAMPLE",
                    isSupported = true,
                    compatibilityStatus = "SAMPLE_LOADED",
                    tempoBpm = 120,
                    timeSigNumerator = 4,
                    timeSigDenominator = 4,
                    sectionSummary = "Playable Sample (${wav.sampleRate} Hz, ${wav.samples.size} frames)",
                    diagnostics = "PCM WAV sample extracted from ORG.SET ($parentFolder). Loaded into User Samples voice category.",
                    missingResources = "None",
                    loadedWavSample = wav
                )
            } else {
                unsupported(
                    cleanName,
                    "ORG_WAV_COMPRESSED",
                    "WAV container uses compressed codec (ADPCM/MP3) rather than uncompressed PCM.",
                    "Uncompressed 16-bit PCM WAV required"
                )
            }
        }

        // 2. Check if ORG style file (.sty, .ost, .mid, .pad) contains Standard MIDI (MThd) track data
        val magic4 = String(bytes, 0, 4, StandardCharsets.US_ASCII)
        if (magic4 == "MThd") {
            val parsed = YamahaStyleParser.inspectAndParse(
                id = id,
                fileName = fileName,
                uriString = uriString,
                bytes = bytes,
                categoryLabel = "ORG.SET ($parentFolder)"
            )
            if (parsed.isSupported && parsed.styleDefinition != null) {
                val orgStyle = parsed.styleDefinition.copy(
                    sourceType = StyleSourceType.IMPORTED_ORG_STYLE,
                    fileFormat = "ORG_SMF_STYLE"
                )
                return OrgInspectionResult(
                    displayName = parsed.displayName,
                    fileFormat = "ORG_SMF_STYLE",
                    isSupported = true,
                    compatibilityStatus = "PLAYABLE",
                    tempoBpm = parsed.tempoBpm,
                    timeSigNumerator = parsed.timeSigNumerator,
                    timeSigDenominator = parsed.timeSigDenominator,
                    sectionSummary = parsed.sectionSummary,
                    diagnostics = "ORG.SET SMF-compatible style ($parentFolder). ${parsed.diagnostics}",
                    missingResources = "Custom Korg/ORG PCM drum samples fall back to built-in kit",
                    styleDefinition = orgStyle
                )
            }
        }

        // 3. Korg Pa / ORG proprietary binary chunks (KORF / KORG / .STY / .PCG / .KMP / .KSF / .PAD / .SET)
        val headerStr = String(bytes, 0, minOf(bytes.size, 16), StandardCharsets.US_ASCII)
        return when {
            ext == "sf2" -> unsupported(
                cleanName,
                "SOUNDFONT_SF2",
                "SoundFont2 bank (.sf2) detected in ORG.SET ($parentFolder). Inspection only in this build.",
                "SF2 multi-zone wavetable engine scheduled for next DSP upgrade"
            )
            ext in setOf("pcg", "prg", "prf") -> unsupported(
                cleanName,
                "ORG_PROGRAM_${ext.uppercase()}",
                "ORG/Korg Program/Performance bank (.$ext in $parentFolder) detected. Contains voice routing metadata.",
                "Requires matching MULTISMP/PCM sample bank decoder"
            )
            ext in setOf("kmp", "ksf", "pcm") -> unsupported(
                cleanName,
                "ORG_SAMPLE_${ext.uppercase()}",
                "Korg/ORG binary multisample resource (.$ext in $parentFolder, ${bytes.size / 1024} KB).",
                "Proprietary KMP/KSF PCM decoder not yet implemented; convert to PCM WAV for immediate playback"
            )
            ext in setOf("sty", "ost", "pad") && (headerStr.contains("KORF") || headerStr.contains("KORG")) -> unsupported(
                cleanName,
                "KORG_PA_STYLE",
                "Korg Pa-series proprietary binary style (KORF header in $parentFolder). Distinct from Yamaha SFF/SMF.",
                "Native Korg KORF event decoder planned; SMF-based styles play directly"
            )
            ext == "set" || magic4 == "PK\u0003\u0004" -> unsupported(
                cleanName,
                "ORG_SET_ARCHIVE",
                "Compressed .SET archive detected. Unzip the .SET folder on your device and import via 'Import ORG.SET Folder'.",
                "Unpacked folder tree required for SAF inspection"
            )
            else -> unsupported(
                cleanName,
                "ORG_${ext.uppercase().ifBlank { "BIN" }}",
                "Non-SMF binary resource inside ORG.SET ($parentFolder, ${bytes.size / 1024} KB). Preserved untouched.",
                "Unsupported proprietary ORG binary structure"
            )
        }
    }

    private fun unsupported(
        name: String,
        format: String,
        diagnostics: String,
        missing: String
    ) = OrgInspectionResult(
        displayName = name,
        fileFormat = format,
        isSupported = false,
        compatibilityStatus = "UNSUPPORTED",
        tempoBpm = 120,
        timeSigNumerator = 4,
        timeSigDenominator = 4,
        sectionSummary = "Inspection Only",
        diagnostics = diagnostics,
        missingResources = missing
    )
}
