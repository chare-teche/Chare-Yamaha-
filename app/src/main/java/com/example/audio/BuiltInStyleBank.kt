package com.example.audio

import com.example.domain.model.AccompPart
import com.example.domain.model.ArrangerSection
import com.example.domain.model.DrumSound
import com.example.domain.model.MultiPadPreset
import com.example.domain.model.StyleDefinition
import com.example.domain.model.StyleSectionPattern
import com.example.domain.model.StyleSourceType
import com.example.domain.model.StyleStepEvent

/**
 * Procedural built-in Ethiopian, Church Worship, Gospel, Hymn, and International styles
 * utilizing all 8 accompaniment parts (Drum, Percussion, Bass, Chord 1, Chord 2, Pad, Phrase 1, Phrase 2).
 */
object BuiltInStyleBank {

    val multiPads: List<MultiPadPreset> = listOf(
        MultiPadPreset(
            slot = 1,
            name = "Kebero Worship Roll",
            shortLabel = "KEBERO",
            lengthSteps = 12,
            events = listOf(
                StyleStepEvent(0, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = 0.95f),
                StyleStepEvent(2, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.82f),
                StyleStepEvent(3, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.9f),
                StyleStepEvent(5, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = 0.92f),
                StyleStepEvent(6, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.88f),
                StyleStepEvent(8, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.94f),
                StyleStepEvent(9, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = 0.98f),
                StyleStepEvent(10, AccompPart.PERCUSSION, DrumSound.CLAP, velocity = 0.85f)
            )
        ),
        MultiPadPreset(
            slot = 2,
            name = "Worship Swell & Chime",
            shortLabel = "SWELL",
            lengthSteps = 12,
            events = listOf(
                StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 4, velocity = 0.72f, durationSteps = 10),
                StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 2, octaveBase = 4, velocity = 0.68f, durationSteps = 10),
                StyleStepEvent(2, AccompPart.PHRASE_2, chordToneIndex = 0, octaveBase = 5, velocity = 0.78f, durationSteps = 4),
                StyleStepEvent(6, AccompPart.PHRASE_2, chordToneIndex = 2, octaveBase = 5, velocity = 0.82f, durationSteps = 5)
            )
        ),
        MultiPadPreset(
            slot = 3,
            name = "Krar Arp Cascade",
            shortLabel = "KRAR",
            lengthSteps = 12,
            events = listOf(
                StyleStepEvent(0, AccompPart.CHORD_2, chordToneIndex = 0, octaveBase = 4, velocity = 0.86f, durationSteps = 2),
                StyleStepEvent(2, AccompPart.CHORD_2, chordToneIndex = 1, octaveBase = 4, velocity = 0.82f, durationSteps = 2),
                StyleStepEvent(4, AccompPart.CHORD_2, chordToneIndex = 2, octaveBase = 4, velocity = 0.86f, durationSteps = 2),
                StyleStepEvent(6, AccompPart.CHORD_2, chordToneIndex = 0, octaveBase = 5, velocity = 0.9f, durationSteps = 2),
                StyleStepEvent(8, AccompPart.CHORD_2, chordToneIndex = 2, octaveBase = 4, velocity = 0.82f, durationSteps = 2),
                StyleStepEvent(10, AccompPart.CHORD_2, chordToneIndex = 1, octaveBase = 4, velocity = 0.78f, durationSteps = 2)
            )
        ),
        MultiPadPreset(
            slot = 4,
            name = "Praise Claps & Brass",
            shortLabel = "PRAISE",
            lengthSteps = 12,
            events = listOf(
                StyleStepEvent(0, AccompPart.PERCUSSION, DrumSound.CRASH, velocity = 0.84f),
                StyleStepEvent(0, AccompPart.PHRASE_1, chordToneIndex = 0, octaveBase = 4, velocity = 0.9f, durationSteps = 3),
                StyleStepEvent(0, AccompPart.PHRASE_1, chordToneIndex = 2, octaveBase = 4, velocity = 0.88f, durationSteps = 3),
                StyleStepEvent(3, AccompPart.PERCUSSION, DrumSound.CLAP, velocity = 0.85f),
                StyleStepEvent(6, AccompPart.PERCUSSION, DrumSound.CLAP, velocity = 0.92f),
                StyleStepEvent(9, AccompPart.PERCUSSION, DrumSound.CLAP, velocity = 0.88f)
            )
        )
    )

    val styles: List<StyleDefinition> = listOf(
        buildAddisJazzSwing44(),
        buildEthioWorshipSlow68(),
        buildEthioPraiseFast68(),
        buildGospelWorship44(),
        buildChoirHymn34(),
        buildTizitaBallad44(),
        buildBatiGroove68(),
        buildAfroReggae44()
    )

    fun defaultStyle(): StyleDefinition = styles.first()

    fun byId(id: String): StyleDefinition? = styles.find { it.id == id }

    private fun buildEthioWorshipSlow68(): StyleDefinition {
        val base = buildEthio68Core(
            id = "builtin_ethio_worship_slow",
            name = "Ethio Worship 6/8",
            category = "Church Worship",
            tempoBpm = 72,
            otsVoices = listOf("piano_worship", "strings_cinema", "ethio_washint", "pad_worship"),
            softWorshipFeel = true
        )
        return base
    }

    private fun buildEthioPraiseFast68(): StyleDefinition = buildEthio68Core(
        id = "builtin_ethio_praise_68",
        name = "Ethio Praise Eskista",
        category = "Church & Ethio",
        tempoBpm = 118,
        otsVoices = listOf("piano_bright", "brass_addis", "ethio_masinko", "reed_accordion"),
        softWorshipFeel = false
    )

    private fun buildBatiGroove68(): StyleDefinition = buildEthio68Core(
        id = "builtin_bati_groove",
        name = "Bati Groove 6/8",
        category = "Ethio Traditional",
        tempoBpm = 104,
        otsVoices = listOf("ethio_krar", "ethio_masinko", "reed_accordion", "synth_lead"),
        softWorshipFeel = false
    )

    private fun buildGospelWorship44(): StyleDefinition = buildStandard44Style(
        id = "builtin_gospel_worship",
        name = "Gospel Worship 4/4",
        category = "Church Worship",
        tempoBpm = 76,
        otsVoices = listOf("piano_worship", "ep_dx_modern", "organ_gospel", "strings_cinema"),
        useKebero = false,
        isSwing = false,
        isReggae = false,
        worshipBallad = true
    )

    private fun buildChoirHymn34(): StyleDefinition {
        // Authentic 3/4 Church Hymn / Waltz (12 sixteenth-triplet / eighth grid steps per bar)
        fun hymnMain(intensity: Int): List<StyleStepEvent> = buildList {
            add(StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.78f))
            add(StyleStepEvent(4, AccompPart.DRUMS, DrumSound.RIM, velocity = 0.62f))
            add(StyleStepEvent(8, AccompPart.DRUMS, DrumSound.RIM, velocity = 0.62f))
            if (intensity >= 2) {
                for (s in listOf(0, 2, 4, 6, 8, 10)) {
                    add(StyleStepEvent(s, AccompPart.DRUMS, DrumSound.CLOSED_HAT, velocity = 0.44f))
                }
            }
            add(StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.85f, durationSteps = 4))
            add(StyleStepEvent(4, AccompPart.BASS, chordToneIndex = 2, octaveBase = 2, velocity = 0.72f, durationSteps = 4))
            add(StyleStepEvent(8, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.74f, durationSteps = 4))

            // Piano / Guitar 3/4 waltz chord comping
            for (s in listOf(4, 8)) {
                add(StyleStepEvent(s, AccompPart.CHORD_1, chordToneIndex = 0, octaveBase = 3, velocity = 0.58f, durationSteps = 3))
                add(StyleStepEvent(s, AccompPart.CHORD_1, chordToneIndex = 1, octaveBase = 3, velocity = 0.54f, durationSteps = 3))
                add(StyleStepEvent(s, AccompPart.CHORD_1, chordToneIndex = 2, octaveBase = 3, velocity = 0.56f, durationSteps = 3))
            }
            if (intensity >= 2) {
                add(StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 4, velocity = 0.48f, durationSteps = 12))
                add(StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 2, octaveBase = 4, velocity = 0.44f, durationSteps = 12))
            }
            if (intensity >= 3) {
                add(StyleStepEvent(0, AccompPart.PHRASE_2, chordToneIndex = 1, octaveBase = 4, velocity = 0.55f, durationSteps = 6))
                add(StyleStepEvent(6, AccompPart.PHRASE_2, chordToneIndex = 2, octaveBase = 4, velocity = 0.58f, durationSteps = 6))
            }
        }
        val fill = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.82f),
            StyleStepEvent(4, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.75f),
            StyleStepEvent(8, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.82f),
            StyleStepEvent(10, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.88f),
            StyleStepEvent(11, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.8f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.85f, durationSteps = 8)
        )
        val intro = listOf(
            StyleStepEvent(0, AccompPart.PHRASE_1, chordToneIndex = 0, octaveBase = 4, velocity = 0.76f, durationSteps = 4),
            StyleStepEvent(4, AccompPart.PHRASE_1, chordToneIndex = 1, octaveBase = 4, velocity = 0.76f, durationSteps = 4),
            StyleStepEvent(8, AccompPart.PHRASE_1, chordToneIndex = 2, octaveBase = 4, velocity = 0.8f, durationSteps = 4),
            StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 3, velocity = 0.5f, durationSteps = 12)
        )
        val ending = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.78f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.88f, durationSteps = 10),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 0, octaveBase = 3, velocity = 0.72f, durationSteps = 10),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 1, octaveBase = 3, velocity = 0.7f, durationSteps = 10),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 2, octaveBase = 3, velocity = 0.72f, durationSteps = 10),
            StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 4, velocity = 0.6f, durationSteps = 12)
        )
        return StyleDefinition(
            id = "builtin_choir_hymn_34",
            name = "Choir Hymn 3/4",
            category = "Church Hymn",
            tempoBpm = 86,
            timeSigNumerator = 3,
            timeSigDenominator = 4,
            stepsPerBar = 12,
            sourceType = StyleSourceType.BUILTIN,
            otsVoiceIds = listOf("organ_church", "piano_worship", "strings_cinema", "guitar_nylon"),
            sections = mapOf(
                ArrangerSection.INTRO_1 to StyleSectionPattern(ArrangerSection.INTRO_1, 1, intro),
                ArrangerSection.INTRO_2 to StyleSectionPattern(ArrangerSection.INTRO_2, 1, intro),
                ArrangerSection.INTRO_3 to StyleSectionPattern(ArrangerSection.INTRO_3, 1, intro),
                ArrangerSection.MAIN_A to StyleSectionPattern(ArrangerSection.MAIN_A, 1, hymnMain(1)),
                ArrangerSection.MAIN_B to StyleSectionPattern(ArrangerSection.MAIN_B, 1, hymnMain(2)),
                ArrangerSection.MAIN_C to StyleSectionPattern(ArrangerSection.MAIN_C, 1, hymnMain(3)),
                ArrangerSection.MAIN_D to StyleSectionPattern(ArrangerSection.MAIN_D, 1, hymnMain(4)),
                ArrangerSection.FILL_A to StyleSectionPattern(ArrangerSection.FILL_A, 1, fill),
                ArrangerSection.FILL_B to StyleSectionPattern(ArrangerSection.FILL_B, 1, fill),
                ArrangerSection.FILL_C to StyleSectionPattern(ArrangerSection.FILL_C, 1, fill),
                ArrangerSection.FILL_D to StyleSectionPattern(ArrangerSection.FILL_D, 1, fill),
                ArrangerSection.BREAK to StyleSectionPattern(ArrangerSection.BREAK, 1, fill.take(2)),
                ArrangerSection.ENDING_1 to StyleSectionPattern(ArrangerSection.ENDING_1, 1, ending),
                ArrangerSection.ENDING_2 to StyleSectionPattern(ArrangerSection.ENDING_2, 1, ending),
                ArrangerSection.ENDING_3 to StyleSectionPattern(ArrangerSection.ENDING_3, 1, ending)
            )
        )
    }

    private fun buildEthio68Core(
        id: String,
        name: String,
        category: String,
        tempoBpm: Int,
        otsVoices: List<String>,
        softWorshipFeel: Boolean
    ): StyleDefinition {
        fun keberoMain(intensity: Int): List<StyleStepEvent> = buildList {
            val kVel = if (softWorshipFeel) 0.78f else 0.94f
            add(StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = kVel))
            add(StyleStepEvent(0, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = kVel))
            add(StyleStepEvent(2, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = kVel * 0.8f))
            add(StyleStepEvent(4, AccompPart.PERCUSSION, DrumSound.SHAKER, velocity = 0.58f))
            add(StyleStepEvent(5, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = kVel * 0.84f))
            add(StyleStepEvent(6, AccompPart.DRUMS, DrumSound.KICK, velocity = kVel * 0.88f))
            add(StyleStepEvent(6, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = kVel * 0.9f))
            add(StyleStepEvent(8, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = kVel * 0.86f))
            add(StyleStepEvent(10, AccompPart.DRUMS, if (softWorshipFeel) DrumSound.RIM else DrumSound.SNARE, velocity = 0.74f))
            add(StyleStepEvent(11, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.76f))

            if (intensity >= 2) {
                for (s in listOf(0, 2, 4, 6, 8, 10)) {
                    add(StyleStepEvent(s, AccompPart.DRUMS, DrumSound.CLOSED_HAT, velocity = 0.56f))
                }
                if (!softWorshipFeel) {
                    add(StyleStepEvent(3, AccompPart.PERCUSSION, DrumSound.CLAP, velocity = 0.68f))
                    add(StyleStepEvent(9, AccompPart.PERCUSSION, DrumSound.CLAP, velocity = 0.74f))
                }
            }
            if (intensity >= 3) {
                add(StyleStepEvent(4, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.74f))
                add(StyleStepEvent(10, AccompPart.DRUMS, DrumSound.OPEN_HAT, velocity = 0.62f))
            }

            // Bass
            add(StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.9f, durationSteps = 3))
            add(StyleStepEvent(4, AccompPart.BASS, chordToneIndex = 2, octaveBase = 2, velocity = 0.76f, durationSteps = 2))
            add(StyleStepEvent(6, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.86f, durationSteps = 2))
            add(StyleStepEvent(9, AccompPart.BASS, chordToneIndex = 2, octaveBase = 2, velocity = 0.8f, durationSteps = 2))

            // Chord 1
            for (step in listOf(2, 5, 8, 11)) {
                add(StyleStepEvent(step, AccompPart.CHORD_1, chordToneIndex = 0, octaveBase = 3, velocity = 0.58f, durationSteps = 2))
                add(StyleStepEvent(step, AccompPart.CHORD_1, chordToneIndex = 1, octaveBase = 3, velocity = 0.54f, durationSteps = 2))
                add(StyleStepEvent(step, AccompPart.CHORD_1, chordToneIndex = 2, octaveBase = 3, velocity = 0.56f, durationSteps = 2))
            }

            // Pad
            if (intensity >= 2) {
                add(StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 4, velocity = 0.45f, durationSteps = 6))
                add(StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 2, octaveBase = 4, velocity = 0.42f, durationSteps = 6))
                add(StyleStepEvent(6, AccompPart.PAD, chordToneIndex = 1, octaveBase = 4, velocity = 0.44f, durationSteps = 6))
            }

            // Phrase 1 & Phrase 2
            if (intensity >= 3) {
                add(StyleStepEvent(0, AccompPart.PHRASE_1, chordToneIndex = 0, octaveBase = 4, velocity = 0.68f, durationSteps = 2))
                add(StyleStepEvent(3, AccompPart.PHRASE_1, chordToneIndex = 2, octaveBase = 4, velocity = 0.66f, durationSteps = 2))
                add(StyleStepEvent(6, AccompPart.PHRASE_2, chordToneIndex = 1, octaveBase = 4, velocity = 0.64f, durationSteps = 3))
                if (intensity == 4) {
                    add(StyleStepEvent(9, AccompPart.PHRASE_2, chordToneIndex = 0, octaveBase = 5, velocity = 0.74f, durationSteps = 3))
                }
            }
        }

        val fillEvents = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.9f),
            StyleStepEvent(0, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = 0.92f),
            StyleStepEvent(2, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.84f),
            StyleStepEvent(6, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.84f),
            StyleStepEvent(8, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.88f),
            StyleStepEvent(10, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.94f),
            StyleStepEvent(11, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.86f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.88f, durationSteps = 6),
            StyleStepEvent(6, AccompPart.BASS, chordToneIndex = 2, octaveBase = 2, velocity = 0.86f, durationSteps = 6)
        )

        val introEvents = listOf(
            StyleStepEvent(0, AccompPart.PERCUSSION, DrumSound.RIM, velocity = 0.82f),
            StyleStepEvent(3, AccompPart.PERCUSSION, DrumSound.RIM, velocity = 0.72f),
            StyleStepEvent(6, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = 0.9f),
            StyleStepEvent(8, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.85f),
            StyleStepEvent(10, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.86f),
            StyleStepEvent(0, AccompPart.PHRASE_1, chordToneIndex = 0, octaveBase = 4, velocity = 0.78f, durationSteps = 3),
            StyleStepEvent(3, AccompPart.PHRASE_1, chordToneIndex = 1, octaveBase = 4, velocity = 0.78f, durationSteps = 3),
            StyleStepEvent(6, AccompPart.PHRASE_2, chordToneIndex = 2, octaveBase = 4, velocity = 0.82f, durationSteps = 3),
            StyleStepEvent(9, AccompPart.PHRASE_2, chordToneIndex = 0, octaveBase = 5, velocity = 0.86f, durationSteps = 3)
        )

        val endingEvents = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.92f),
            StyleStepEvent(0, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = 0.92f),
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.88f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.9f, durationSteps = 9),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 0, octaveBase = 3, velocity = 0.78f, durationSteps = 9),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 1, octaveBase = 3, velocity = 0.75f, durationSteps = 9),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 2, octaveBase = 3, velocity = 0.78f, durationSteps = 9),
            StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 4, velocity = 0.65f, durationSteps = 11)
        )

        val breakEvents = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.82f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.88f, durationSteps = 2),
            StyleStepEvent(9, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.84f),
            StyleStepEvent(10, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.88f)
        )

        return StyleDefinition(
            id = id,
            name = name,
            category = category,
            tempoBpm = tempoBpm,
            timeSigNumerator = 6,
            timeSigDenominator = 8,
            stepsPerBar = 12,
            sourceType = StyleSourceType.BUILTIN,
            otsVoiceIds = otsVoices,
            sections = mapOf(
                ArrangerSection.INTRO_1 to StyleSectionPattern(ArrangerSection.INTRO_1, 1, introEvents),
                ArrangerSection.INTRO_2 to StyleSectionPattern(ArrangerSection.INTRO_2, 1, introEvents),
                ArrangerSection.INTRO_3 to StyleSectionPattern(ArrangerSection.INTRO_3, 1, introEvents),
                ArrangerSection.MAIN_A to StyleSectionPattern(ArrangerSection.MAIN_A, 1, keberoMain(1)),
                ArrangerSection.MAIN_B to StyleSectionPattern(ArrangerSection.MAIN_B, 1, keberoMain(2)),
                ArrangerSection.MAIN_C to StyleSectionPattern(ArrangerSection.MAIN_C, 1, keberoMain(3)),
                ArrangerSection.MAIN_D to StyleSectionPattern(ArrangerSection.MAIN_D, 1, keberoMain(4)),
                ArrangerSection.FILL_A to StyleSectionPattern(ArrangerSection.FILL_A, 1, fillEvents),
                ArrangerSection.FILL_B to StyleSectionPattern(ArrangerSection.FILL_B, 1, fillEvents),
                ArrangerSection.FILL_C to StyleSectionPattern(ArrangerSection.FILL_C, 1, fillEvents),
                ArrangerSection.FILL_D to StyleSectionPattern(ArrangerSection.FILL_D, 1, fillEvents),
                ArrangerSection.BREAK to StyleSectionPattern(ArrangerSection.BREAK, 1, breakEvents),
                ArrangerSection.ENDING_1 to StyleSectionPattern(ArrangerSection.ENDING_1, 1, endingEvents),
                ArrangerSection.ENDING_2 to StyleSectionPattern(ArrangerSection.ENDING_2, 1, endingEvents),
                ArrangerSection.ENDING_3 to StyleSectionPattern(ArrangerSection.ENDING_3, 1, endingEvents)
            )
        )
    }

    private fun buildTizitaBallad44(): StyleDefinition = buildStandard44Style(
        id = "builtin_tizita_ballad",
        name = "Tizita Ballad",
        category = "Ethio Slow",
        tempoBpm = 74,
        otsVoices = listOf("ep_dx_modern", "ethio_washint", "sax_tenor", "strings_cinema"),
        useKebero = true,
        isSwing = false,
        isReggae = false,
        worshipBallad = true
    )

    private fun buildAddisJazzSwing44(): StyleDefinition = buildStandard44Style(
        id = "builtin_addis_jazz",
        name = "Ethio Jazz",
        category = "ModernPop",
        tempoBpm = 120,
        otsVoices = listOf("piano_cfx", "sax_tenor", "organ_gospel", "brass_addis"),
        useKebero = false,
        isSwing = true,
        isReggae = false,
        worshipBallad = false
    )

    private fun buildAfroReggae44(): StyleDefinition = buildStandard44Style(
        id = "builtin_afro_reggae",
        name = "Afro Reggae Praise",
        category = "World & Praise",
        tempoBpm = 96,
        otsVoices = listOf("organ_gospel", "brass_addis", "ethio_krar", "synth_lead"),
        useKebero = true,
        isSwing = false,
        isReggae = true,
        worshipBallad = false
    )

    private fun buildStandard44Style(
        id: String,
        name: String,
        category: String,
        tempoBpm: Int,
        otsVoices: List<String>,
        useKebero: Boolean,
        isSwing: Boolean,
        isReggae: Boolean,
        worshipBallad: Boolean
    ): StyleDefinition {
        fun mainEvents(intensity: Int): List<StyleStepEvent> = buildList {
            if (isReggae) {
                add(StyleStepEvent(4, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.92f))
                add(StyleStepEvent(12, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.92f))
                add(StyleStepEvent(12, AccompPart.DRUMS, DrumSound.RIM, velocity = 0.85f))
                for (s in listOf(0, 2, 4, 6, 8, 10, 12, 14)) {
                    add(StyleStepEvent(s, AccompPart.DRUMS, DrumSound.CLOSED_HAT, velocity = if (s % 4 == 0) 0.72f else 0.5f))
                }
            } else {
                add(StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = if (worshipBallad) 0.82f else 0.94f))
                add(StyleStepEvent(4, AccompPart.DRUMS, if (worshipBallad && intensity == 1) DrumSound.RIM else DrumSound.SNARE, velocity = 0.82f))
                add(StyleStepEvent(8, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.82f))
                add(StyleStepEvent(12, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.85f))
                if (intensity >= 2) {
                    add(StyleStepEvent(10, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.76f))
                }
                val hatSteps = if (isSwing) listOf(0, 4, 7, 8, 12, 15) else listOf(0, 2, 4, 6, 8, 10, 12, 14)
                for (s in hatSteps) {
                    add(StyleStepEvent(s, AccompPart.DRUMS, DrumSound.CLOSED_HAT, velocity = if (s % 4 == 0) 0.66f else 0.48f))
                }
            }

            if (useKebero) {
                add(StyleStepEvent(0, AccompPart.PERCUSSION, DrumSound.KEBERO_LOW, velocity = 0.76f))
                add(StyleStepEvent(6, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.7f))
                add(StyleStepEvent(14, AccompPart.PERCUSSION, DrumSound.KEBERO_HIGH, velocity = 0.74f))
            } else if (intensity >= 2) {
                for (s in listOf(2, 6, 10, 14)) {
                    add(StyleStepEvent(s, AccompPart.PERCUSSION, DrumSound.SHAKER, velocity = 0.5f))
                }
            }

            // Bass
            add(StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.88f, durationSteps = 4))
            add(StyleStepEvent(6, AccompPart.BASS, chordToneIndex = 2, octaveBase = 2, velocity = 0.76f, durationSteps = 2))
            add(StyleStepEvent(8, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.86f, durationSteps = 4))
            if (intensity >= 2) {
                add(StyleStepEvent(12, AccompPart.BASS, chordToneIndex = 1, octaveBase = 2, velocity = 0.78f, durationSteps = 2))
                add(StyleStepEvent(14, AccompPart.BASS, chordToneIndex = 2, octaveBase = 2, velocity = 0.8f, durationSteps = 2))
            }

            // Chord 1
            val compSteps = if (isReggae) listOf(2, 6, 10, 14) else listOf(0, 6, 10)
            for (s in compSteps) {
                val dur = if (isReggae) 1 else 3
                add(StyleStepEvent(s, AccompPart.CHORD_1, chordToneIndex = 0, octaveBase = 3, velocity = 0.6f, durationSteps = dur))
                add(StyleStepEvent(s, AccompPart.CHORD_1, chordToneIndex = 1, octaveBase = 3, velocity = 0.56f, durationSteps = dur))
                add(StyleStepEvent(s, AccompPart.CHORD_1, chordToneIndex = 2, octaveBase = 3, velocity = 0.58f, durationSteps = dur))
            }

            // Pad
            if (intensity >= 2) {
                add(StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 4, velocity = 0.44f, durationSteps = 16))
                add(StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 2, octaveBase = 4, velocity = 0.4f, durationSteps = 16))
            }

            // Chord 2 arpeggio + Phrase 1 + Phrase 2
            if (intensity >= 3) {
                add(StyleStepEvent(0, AccompPart.CHORD_2, chordToneIndex = 0, octaveBase = 4, velocity = 0.62f, durationSteps = 2))
                add(StyleStepEvent(4, AccompPart.CHORD_2, chordToneIndex = 1, octaveBase = 4, velocity = 0.6f, durationSteps = 2))
                add(StyleStepEvent(8, AccompPart.CHORD_2, chordToneIndex = 2, octaveBase = 4, velocity = 0.62f, durationSteps = 2))
                add(StyleStepEvent(12, AccompPart.CHORD_2, chordToneIndex = 0, octaveBase = 5, velocity = 0.65f, durationSteps = 2))
                add(StyleStepEvent(0, AccompPart.PHRASE_1, chordToneIndex = 2, octaveBase = 4, velocity = 0.68f, durationSteps = 3))
            }
            if (intensity == 4) {
                add(StyleStepEvent(10, AccompPart.PHRASE_2, chordToneIndex = 0, octaveBase = 5, velocity = 0.72f, durationSteps = 4))
            }
        }

        val fillEvents = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.9f),
            StyleStepEvent(4, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.82f),
            StyleStepEvent(8, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.85f),
            StyleStepEvent(10, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.88f),
            StyleStepEvent(12, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.92f),
            StyleStepEvent(14, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.95f),
            StyleStepEvent(15, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.86f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.88f, durationSteps = 8),
            StyleStepEvent(8, AccompPart.BASS, chordToneIndex = 2, octaveBase = 2, velocity = 0.88f, durationSteps = 8)
        )

        val introEvents = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.RIM, velocity = 0.84f),
            StyleStepEvent(4, AccompPart.DRUMS, DrumSound.RIM, velocity = 0.8f),
            StyleStepEvent(8, AccompPart.DRUMS, DrumSound.RIM, velocity = 0.82f),
            StyleStepEvent(12, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.88f),
            StyleStepEvent(14, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.92f),
            StyleStepEvent(0, AccompPart.PHRASE_1, chordToneIndex = 0, octaveBase = 4, velocity = 0.78f, durationSteps = 4),
            StyleStepEvent(4, AccompPart.PHRASE_1, chordToneIndex = 1, octaveBase = 4, velocity = 0.8f, durationSteps = 4),
            StyleStepEvent(8, AccompPart.PHRASE_2, chordToneIndex = 2, octaveBase = 4, velocity = 0.82f, durationSteps = 4),
            StyleStepEvent(12, AccompPart.PHRASE_2, chordToneIndex = 0, octaveBase = 5, velocity = 0.86f, durationSteps = 4)
        )

        val breakEvents = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.84f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.88f, durationSteps = 3),
            StyleStepEvent(12, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.88f),
            StyleStepEvent(14, AccompPart.DRUMS, DrumSound.SNARE, velocity = 0.92f)
        )

        val endingEvents = listOf(
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.KICK, velocity = 0.94f),
            StyleStepEvent(0, AccompPart.DRUMS, DrumSound.CRASH, velocity = 0.9f),
            StyleStepEvent(0, AccompPart.BASS, chordToneIndex = 0, octaveBase = 2, velocity = 0.9f, durationSteps = 12),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 0, octaveBase = 3, velocity = 0.78f, durationSteps = 12),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 1, octaveBase = 3, velocity = 0.75f, durationSteps = 12),
            StyleStepEvent(0, AccompPart.CHORD_1, chordToneIndex = 2, octaveBase = 3, velocity = 0.78f, durationSteps = 12),
            StyleStepEvent(0, AccompPart.PAD, chordToneIndex = 0, octaveBase = 4, velocity = 0.65f, durationSteps = 14)
        )

        return StyleDefinition(
            id = id,
            name = name,
            category = category,
            tempoBpm = tempoBpm,
            timeSigNumerator = 4,
            timeSigDenominator = 4,
            stepsPerBar = 16,
            sourceType = StyleSourceType.BUILTIN,
            otsVoiceIds = otsVoices,
            sections = mapOf(
                ArrangerSection.INTRO_1 to StyleSectionPattern(ArrangerSection.INTRO_1, 1, introEvents),
                ArrangerSection.INTRO_2 to StyleSectionPattern(ArrangerSection.INTRO_2, 1, introEvents),
                ArrangerSection.INTRO_3 to StyleSectionPattern(ArrangerSection.INTRO_3, 1, introEvents),
                ArrangerSection.MAIN_A to StyleSectionPattern(ArrangerSection.MAIN_A, 1, mainEvents(1)),
                ArrangerSection.MAIN_B to StyleSectionPattern(ArrangerSection.MAIN_B, 1, mainEvents(2)),
                ArrangerSection.MAIN_C to StyleSectionPattern(ArrangerSection.MAIN_C, 1, mainEvents(3)),
                ArrangerSection.MAIN_D to StyleSectionPattern(ArrangerSection.MAIN_D, 1, mainEvents(4)),
                ArrangerSection.FILL_A to StyleSectionPattern(ArrangerSection.FILL_A, 1, fillEvents),
                ArrangerSection.FILL_B to StyleSectionPattern(ArrangerSection.FILL_B, 1, fillEvents),
                ArrangerSection.FILL_C to StyleSectionPattern(ArrangerSection.FILL_C, 1, fillEvents),
                ArrangerSection.FILL_D to StyleSectionPattern(ArrangerSection.FILL_D, 1, fillEvents),
                ArrangerSection.BREAK to StyleSectionPattern(ArrangerSection.BREAK, 1, breakEvents),
                ArrangerSection.ENDING_1 to StyleSectionPattern(ArrangerSection.ENDING_1, 1, endingEvents),
                ArrangerSection.ENDING_2 to StyleSectionPattern(ArrangerSection.ENDING_2, 1, endingEvents),
                ArrangerSection.ENDING_3 to StyleSectionPattern(ArrangerSection.ENDING_3, 1, endingEvents)
            )
        )
    }
}
