package com.example

import com.example.audio.BuiltInStyleBank
import com.example.audio.ChordDetector
import com.example.audio.WavSampleLoader
import com.example.data.parser.OrgSetParser
import com.example.data.parser.YamahaStyleParser
import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerSection
import com.example.domain.model.ChordQuality
import com.example.domain.model.DetectedChord
import com.example.domain.model.SplitChordMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ArrangerEngineUnitTest {

    @Test
    fun chordDetector_recognizesMajorMinorAndSeventhInversions() {
        val cMaj = ChordDetector.detect(setOf(48, 52, 55), SplitChordMode.FINGERED, DetectedChord.DEFAULT_C_MAJOR)
        assertEquals("C Maj", cMaj.displayName)
        assertEquals(ChordQuality.MAJOR, cMaj.quality)

        // A minor first inversion (C3, E3, A3)
        val aMinInv = ChordDetector.detect(setOf(48, 52, 57), SplitChordMode.FINGERED, DetectedChord.DEFAULT_C_MAJOR)
        assertEquals(ChordQuality.MINOR, aMinInv.quality)
        assertEquals(9, aMinInv.rootPitchClass) // A

        // G7 (G2, B2, D3, F3)
        val g7 = ChordDetector.detect(setOf(43, 47, 50, 53), SplitChordMode.FINGERED, DetectedChord.DEFAULT_C_MAJOR)
        assertEquals("G7", g7.displayName)
    }

    @Test
    fun chordDetector_supportsSingleFingerYamahaMode() {
        // C3 + Bb2 (black key to left) = C minor in Single Finger mode
        val cMin = ChordDetector.detect(setOf(46, 48), SplitChordMode.SINGLE_FINGER, DetectedChord.DEFAULT_C_MAJOR)
        assertEquals("Cm", cMin.displayName)

        // C3 + B2 (white key to left) = C7 in Single Finger mode
        val c7 = ChordDetector.detect(setOf(47, 48), SplitChordMode.SINGLE_FINGER, DetectedChord.DEFAULT_C_MAJOR)
        assertEquals("C7", c7.displayName)
    }

    @Test
    fun yamahaStyleParser_parsesValidSmfWithYamahaMarkersAndRejectsUnsupportedBinary() {
        val smf = buildMinimalSmfStyle(withCasm = true)

        val parsed = YamahaStyleParser.inspectAndParse(
            id = "test_sty",
            fileName = "EthioTest.sty",
            uriString = "content://test/EthioTest.sty",
            bytes = smf
        )

        assertTrue(parsed.isSupported)
        assertEquals("YAMAHA_SFF1", parsed.fileFormat)
        assertEquals(120, parsed.tempoBpm)
        assertNotNull(parsed.styleDefinition)
        assertTrue(parsed.sectionSummary.contains("MAIN A"))

        // Unsupported encrypted .ppf file should be cleanly rejected with honest diagnostics
        val unsupported = YamahaStyleParser.inspectAndParse(
            id = "test_ppf",
            fileName = "Pack.ppf",
            uriString = "content://test/Pack.ppf",
            bytes = ByteArray(64) { 0x55 }
        )
        assertFalse(unsupported.isSupported)
        assertEquals("YAMAHA_PPF", unsupported.fileFormat)
    }

    @Test
    fun wavSampleLoader_parsesValid16BitPcmAndRejectsInvalidData() {
        val wavBytes = buildMinimalPcm16Wav(sampleRate = 44100, frameCount = 128)
        val loaded = WavSampleLoader.parseWav("sample_1", "KeberoHit.wav", wavBytes)
        assertNotNull(loaded)
        assertEquals("KeberoHit", loaded!!.name)
        assertEquals(44100, loaded.sampleRate)
        assertEquals(128, loaded.samples.size)
        assertEquals(0.5f, loaded.samples[0], 0.01f)

        val invalid = WavSampleLoader.parseWav("bad", "Broken.wav", ByteArray(20) { 0x00 })
        assertNull(invalid)
    }

    @Test
    fun orgSetParser_separatesPlayableSamplesSmfStylesAndProprietaryKorgFormats() {
        // 1. PCM WAV inside ORG.SET -> SAMPLE_LOADED
        val wavResult = OrgSetParser.inspectOrgFile(
            id = "org_wav",
            fileName = "Masinko.wav",
            parentFolder = "PCM",
            uriString = "content://org/PCM/Masinko.wav",
            bytes = buildMinimalPcm16Wav(sampleRate = 44100, frameCount = 64)
        )
        assertTrue(wavResult.isSupported)
        assertEquals("SAMPLE_LOADED", wavResult.compatibilityStatus)
        assertEquals("ORG_WAV_SAMPLE", wavResult.fileFormat)
        assertNotNull(wavResult.loadedWavSample)

        // 2. Korg Pa proprietary KORF style -> honest UNSUPPORTED report
        val korfBytes = ("KORF" + "0123456789ABCDEF").toByteArray(Charsets.US_ASCII)
        val korfResult = OrgSetParser.inspectOrgFile(
            id = "org_korf",
            fileName = "EthioPa.sty",
            parentFolder = "STYLE",
            uriString = "content://org/STYLE/EthioPa.sty",
            bytes = korfBytes
        )
        assertFalse(korfResult.isSupported)
        assertEquals("UNSUPPORTED", korfResult.compatibilityStatus)
        assertEquals("KORG_PA_STYLE", korfResult.fileFormat)

        // 3. SoundFont2 inside ORG.SET -> honest inspection
        val sf2Bytes = ("RIFF" + "0000" + "sfbk" + "LIST0000").toByteArray(Charsets.US_ASCII)
        val sf2Result = OrgSetParser.inspectOrgFile(
            id = "org_sf2",
            fileName = "WorshipBank.sf2",
            parentFolder = "SOUND",
            uriString = "content://org/SOUND/WorshipBank.sf2",
            bytes = sf2Bytes
        )
        assertFalse(sf2Result.isSupported)
        assertEquals("SOUNDFONT_SF2", sf2Result.fileFormat)

        // 4. SMF-based ORG style -> PLAYABLE
        val orgSmfResult = OrgSetParser.inspectOrgFile(
            id = "org_smf",
            fileName = "WorshipGroove.sty",
            parentFolder = "STYLE",
            uriString = "content://org/STYLE/WorshipGroove.sty",
            bytes = buildMinimalSmfStyle(withCasm = false)
        )
        assertTrue(orgSmfResult.isSupported)
        assertEquals("PLAYABLE", orgSmfResult.compatibilityStatus)
        assertEquals("ORG_SMF_STYLE", orgSmfResult.fileFormat)
        assertNotNull(orgSmfResult.styleDefinition)
    }

    @Test
    fun builtInStyleBank_providesAll8ChurchAndEthiopianStylesWithCompleteSections() {
        val styles = BuiltInStyleBank.styles
        assertTrue(styles.size >= 8)
        assertEquals("Ethio Jazz", BuiltInStyleBank.defaultStyle().name)

        // Verify 3/4 hymn, 6/8 worship, and 4/4 gospel styles exist
        assertTrue(styles.any { it.name.contains("Choir Hymn") && it.timeSigNumerator == 3 })
        assertTrue(styles.any { it.name.contains("Ethio Worship") && it.timeSigNumerator == 6 })
        assertTrue(styles.any { it.name.contains("Gospel Worship") && it.timeSigNumerator == 4 })

        // Verify all styles have Main A..D, Intro, Break, Ending, and Phrase 2 accompaniment data
        for (style in styles) {
            assertNotNull(style.sections[ArrangerSection.INTRO_1])
            assertNotNull(style.sections[ArrangerSection.MAIN_A])
            assertNotNull(style.sections[ArrangerSection.MAIN_B])
            assertNotNull(style.sections[ArrangerSection.MAIN_C])
            assertNotNull(style.sections[ArrangerSection.MAIN_D])
            assertNotNull(style.sections[ArrangerSection.BREAK])
            assertNotNull(style.sections[ArrangerSection.ENDING_1])
            val mainD = style.sections[ArrangerSection.MAIN_D]!!
            assertTrue(
                "Expected PHRASE_2 events in ${style.name} MAIN_D",
                mainD.events.any { it.part == AccompPart.PHRASE_2 }
            )
        }
    }

    @Test
    fun fourPartModelState_exposesAllFourIndependentPartsAndSummary() {
        val state = com.example.domain.model.ArrangerUiState(
            activeStyle = BuiltInStyleBank.defaultStyle()
        )
        assertEquals(4, state.allKeyboardParts.size)
        assertTrue(state.isRight1Enabled)
        assertFalse(state.isRight2Enabled)
        assertFalse(state.isRight3Enabled)
        assertTrue(state.isLeftVoiceEnabled)
        assertEquals("R1", state.activeRightPartsSummary)

        val layered = state.copy(
            right2Part = state.right2Part.copy(isEnabled = true),
            right3Part = state.right3Part.copy(isEnabled = true)
        )
        assertEquals("R1+R2+R3", layered.activeRightPartsSummary)
    }

    @Test
    fun keyboardNoteRouter_routesFourPartsWithSplitOctaveRangeAndUniqueVoiceTags() {
        val parts = mapOf(
            com.example.domain.model.KeyboardPartTarget.LEFT to com.example.domain.model.KeyboardPartConfig(
                part = com.example.domain.model.KeyboardPartTarget.LEFT,
                voice = com.example.domain.model.BuiltInVoices.byId("bass_fingered"),
                isEnabled = true,
                volume = 0.70f,
                pan = -0.30f,
                octaveShift = -1,
                lowNoteMidi = 36,
                highNoteMidi = 59
            ),
            com.example.domain.model.KeyboardPartTarget.RIGHT_1 to com.example.domain.model.KeyboardPartConfig(
                part = com.example.domain.model.KeyboardPartTarget.RIGHT_1,
                voice = com.example.domain.model.BuiltInVoices.byId("piano_cfx"),
                isEnabled = true,
                volume = 0.90f,
                pan = 0f,
                octaveShift = 0,
                lowNoteMidi = 60,
                highNoteMidi = 96
            ),
            com.example.domain.model.KeyboardPartTarget.RIGHT_2 to com.example.domain.model.KeyboardPartConfig(
                part = com.example.domain.model.KeyboardPartTarget.RIGHT_2,
                voice = com.example.domain.model.BuiltInVoices.byId("strings_cinema"),
                isEnabled = true,
                volume = 0.60f,
                pan = -0.40f,
                octaveShift = 1,
                lowNoteMidi = 60,
                highNoteMidi = 84
            ),
            com.example.domain.model.KeyboardPartTarget.RIGHT_3 to com.example.domain.model.KeyboardPartConfig(
                part = com.example.domain.model.KeyboardPartTarget.RIGHT_3,
                voice = com.example.domain.model.BuiltInVoices.byId("brass_addis"),
                isEnabled = true,
                volume = 0.55f,
                pan = 0.45f,
                octaveShift = 0,
                lowNoteMidi = 72,
                highNoteMidi = 96
            )
        )

        // 1. Key below split (C3 = 48) routes ONLY to LEFT with -1 octave shift -> C2 (36)
        val leftHit = com.example.domain.model.KeyboardNoteRouter.routeKeyDown(
            rawMidiNote = 48,
            splitNoteMidi = 59,
            isSplitEnabled = true,
            globalOctaveShift = 0,
            transposeSemitones = 0,
            parts = parts
        )
        assertEquals(1, leftHit.size)
        assertEquals(com.example.domain.model.KeyboardPartTarget.LEFT, leftHit[0].part)
        assertEquals(36, leftHit[0].midiNote)
        assertEquals(-0.30f, leftHit[0].pan, 0.001f)

        // 2. Key at C4 (60) -> RIGHT_1 (60) + RIGHT_2 (72 via +1 oct); RIGHT_3 excluded by its lowNoteMidi=72
        val midRightHit = com.example.domain.model.KeyboardNoteRouter.routeKeyDown(
            rawMidiNote = 60,
            splitNoteMidi = 59,
            isSplitEnabled = true,
            globalOctaveShift = 0,
            transposeSemitones = 0,
            parts = parts
        )
        assertEquals(2, midRightHit.size)
        assertEquals(setOf(com.example.domain.model.KeyboardPartTarget.RIGHT_1, com.example.domain.model.KeyboardPartTarget.RIGHT_2), midRightHit.map { it.part }.toSet())
        assertEquals(72, midRightHit.first { it.part == com.example.domain.model.KeyboardPartTarget.RIGHT_2 }.midiNote)

        // 3. Key at C5 (72) -> RIGHT_1 + RIGHT_2 + RIGHT_3 all sound simultaneously with distinct tags
        val highRightHit = com.example.domain.model.KeyboardNoteRouter.routeKeyDown(
            rawMidiNote = 72,
            splitNoteMidi = 59,
            isSplitEnabled = true,
            globalOctaveShift = 0,
            transposeSemitones = 0,
            parts = parts
        )
        assertEquals(3, highRightHit.size)
        val tags = highRightHit.map { it.voiceTag }.toSet()
        assertEquals(3, tags.size) // Strictly unique tags prevent note cutoff across layers
        assertFalse(tags.contains(com.example.domain.model.KeyboardNoteRouter.harmonyTagFor(72)))

        // 4. Disabling RIGHT_2 leaves RIGHT_1 and RIGHT_3 sounding cleanly
        val r2Disabled = parts.toMutableMap().apply {
            val existing = getValue(com.example.domain.model.KeyboardPartTarget.RIGHT_2)
            put(com.example.domain.model.KeyboardPartTarget.RIGHT_2, existing.copy(isEnabled = false))
        }
        val afterDisable = com.example.domain.model.KeyboardNoteRouter.routeKeyDown(
            rawMidiNote = 72,
            splitNoteMidi = 59,
            isSplitEnabled = true,
            globalOctaveShift = 0,
            transposeSemitones = 0,
            parts = r2Disabled
        )
        assertEquals(
            setOf(com.example.domain.model.KeyboardPartTarget.RIGHT_1, com.example.domain.model.KeyboardPartTarget.RIGHT_3),
            afterDisable.map { it.part }.toSet()
        )
    }

    private fun buildMinimalSmfStyle(withCasm: Boolean): ByteArray {
        val track = ByteArrayOutputStream().apply {
            // Delta 0, Meta 0x51 Tempo = 500000 us (120 BPM)
            write(byteArrayOf(0x00, 0xFF.toByte(), 0x51, 0x03, 0x07, 0xA1.toByte(), 0x20))
            // Delta 0, Meta 0x06 Marker "Main A"
            write(byteArrayOf(0x00, 0xFF.toByte(), 0x06, 0x06))
            write("Main A".toByteArray(Charsets.US_ASCII))
            // Delta 0, Ch 10 (0x99) Kick (36) vel 100
            write(byteArrayOf(0x00, 0x99.toByte(), 36, 100))
            // Delta 0, Ch 11 (0x9A) Bass C2 (36) vel 96
            write(byteArrayOf(0x00, 0x9A.toByte(), 36, 96))
            // End of Track
            write(byteArrayOf(0x00, 0xFF.toByte(), 0x2F, 0x00))
        }.toByteArray()

        return ByteArrayOutputStream().apply {
            write("MThd".toByteArray(Charsets.US_ASCII))
            write(byteArrayOf(0, 0, 0, 6))
            write(byteArrayOf(0, 0))
            write(byteArrayOf(0, 1))
            write(byteArrayOf(0x01, 0xE0.toByte()))
            write("MTrk".toByteArray(Charsets.US_ASCII))
            write(
                byteArrayOf(
                    ((track.size shr 24) and 0xFF).toByte(),
                    ((track.size shr 16) and 0xFF).toByte(),
                    ((track.size shr 8) and 0xFF).toByte(),
                    (track.size and 0xFF).toByte()
                )
            )
            write(track)
            if (withCasm) {
                write("CASM".toByteArray(Charsets.US_ASCII))
            }
        }.toByteArray()
    }

    private fun buildMinimalPcm16Wav(sampleRate: Int, frameCount: Int): ByteArray {
        val dataSize = frameCount * 2
        val bb = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        bb.put("RIFF".toByteArray(Charsets.US_ASCII))
        bb.putInt(36 + dataSize)
        bb.put("WAVE".toByteArray(Charsets.US_ASCII))
        bb.put("fmt ".toByteArray(Charsets.US_ASCII))
        bb.putInt(16) // PCM fmt chunk size
        bb.putShort(1) // AudioFormat = PCM
        bb.putShort(1) // Mono
        bb.putInt(sampleRate)
        bb.putInt(sampleRate * 2) // ByteRate
        bb.putShort(2) // BlockAlign
        bb.putShort(16) // BitsPerSample
        bb.put("data".toByteArray(Charsets.US_ASCII))
        bb.putInt(dataSize)
        for (i in 0 until frameCount) {
            bb.putShort(16384.toShort()) // ~0.5f normalized
        }
        return bb.array()
    }
}
