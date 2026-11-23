package com.example.domain.model

enum class SynthEngineType {
    CONCERT_PIANO,
    ELECTRIC_PIANO,
    MASINKO_BOWED,
    WASHINT_FLUTE,
    KRAR_PLUCKED,
    ACCORDION_REED,
    CHURCH_ORGAN,
    ACOUSTIC_GUITAR,
    BRASS_SECTION,
    SAXOPHONE,
    STRINGS_ENSEMBLE,
    SYNTH_LEAD,
    WARM_PAD,
    ELECTRIC_BASS,
    SYNTH_BASS,
    USER_WAV_SAMPLE
}

enum class VoiceCategory(val displayName: String) {
    ETHIO_TRADITIONAL("Ethio & World"),
    WORSHIP_KEYS("Worship & Piano"),
    ELECTRIC_PIANO("E.Piano"),
    ORGAN_ACCORDION("Organ / Reed"),
    GUITAR("Guitar"),
    BRASS_REEDS("Brass & Sax"),
    STRINGS_PAD("Strings & Pad"),
    SYNTH_LEAD("Synth Lead"),
    BASS("Bass"),
    USER_SAMPLES("User Samples")
}

data class VoicePreset(
    val id: String,
    val name: String,
    val category: VoiceCategory,
    val engineType: SynthEngineType,
    val description: String,
    val brightness: Float = 0.7f,
    val attackSeconds: Float = 0.01f,
    val decaySeconds: Float = 0.55f,
    val sustainLevel: Float = 0.45f,
    val releaseSeconds: Float = 0.25f,
    val vibratoRateHz: Float = 0f,
    val vibratoDepthSemitones: Float = 0f,
    val defaultGain: Float = 0.85f,
    val sampleKey: String? = null,
    val sampleRootMidi: Int = 60
)

object BuiltInVoices {
    val all: List<VoicePreset> = listOf(
        // Piano & Church Worship Keys
        VoicePreset(
            id = "piano_cfx",
            name = "Grand Piano",
            category = VoiceCategory.WORSHIP_KEYS,
            engineType = SynthEngineType.CONCERT_PIANO,
            description = "Full concert grand piano with clear hammer harmonics for worship & solo play",
            brightness = 0.76f,
            attackSeconds = 0.004f,
            decaySeconds = 1.2f,
            sustainLevel = 0.24f,
            releaseSeconds = 0.4f
        ),
        VoicePreset(
            id = "piano_worship",
            name = "Worship Warm Grand",
            category = VoiceCategory.WORSHIP_KEYS,
            engineType = SynthEngineType.CONCERT_PIANO,
            description = "Mellow felt-damped grand piano voiced for church worship & hymns",
            brightness = 0.54f,
            attackSeconds = 0.006f,
            decaySeconds = 1.35f,
            sustainLevel = 0.28f,
            releaseSeconds = 0.48f
        ),
        VoicePreset(
            id = "piano_bright",
            name = "Stage Bright Piano",
            category = VoiceCategory.WORSHIP_KEYS,
            engineType = SynthEngineType.CONCERT_PIANO,
            description = "Cutting live-stage acoustic piano for upbeat praise & ensemble mixes",
            brightness = 0.92f,
            attackSeconds = 0.003f,
            decaySeconds = 0.95f,
            sustainLevel = 0.2f,
            releaseSeconds = 0.32f
        ),
        // Ethio Traditional & World
        VoicePreset(
            id = "ethio_masinko",
            name = "Masinko Bowed",
            category = VoiceCategory.ETHIO_TRADITIONAL,
            engineType = SynthEngineType.MASINKO_BOWED,
            description = "Expressive Ethiopian single-string bowed fiddle with warm vibrato",
            brightness = 0.78f,
            attackSeconds = 0.045f,
            decaySeconds = 0.2f,
            sustainLevel = 0.88f,
            releaseSeconds = 0.22f,
            vibratoRateHz = 5.4f,
            vibratoDepthSemitones = 0.22f
        ),
        VoicePreset(
            id = "ethio_washint",
            name = "Washint Flute",
            category = VoiceCategory.ETHIO_TRADITIONAL,
            engineType = SynthEngineType.WASHINT_FLUTE,
            description = "Traditional Ethiopian bamboo flute with natural breath chiff",
            brightness = 0.62f,
            attackSeconds = 0.055f,
            decaySeconds = 0.2f,
            sustainLevel = 0.82f,
            releaseSeconds = 0.24f,
            vibratoRateHz = 4.8f,
            vibratoDepthSemitones = 0.14f
        ),
        VoicePreset(
            id = "ethio_krar",
            name = "Krar Plucked Lyre",
            category = VoiceCategory.ETHIO_TRADITIONAL,
            engineType = SynthEngineType.KRAR_PLUCKED,
            description = "Bright 6-string Ethiopian bowl lyre with metallic pluck transient",
            brightness = 0.84f,
            attackSeconds = 0.004f,
            decaySeconds = 0.65f,
            sustainLevel = 0.12f,
            releaseSeconds = 0.35f
        ),
        VoicePreset(
            id = "ethio_begena",
            name = "Begena Harp",
            category = VoiceCategory.ETHIO_TRADITIONAL,
            engineType = SynthEngineType.KRAR_PLUCKED,
            description = "Deep resonant Ethiopian spiritual harp for meditation & mezmur",
            brightness = 0.46f,
            attackSeconds = 0.008f,
            decaySeconds = 1.0f,
            sustainLevel = 0.22f,
            releaseSeconds = 0.48f
        ),
        // Electric Pianos
        VoicePreset(
            id = "ep_suitcase",
            name = "Suitcase Tine EP",
            category = VoiceCategory.ELECTRIC_PIANO,
            engineType = SynthEngineType.ELECTRIC_PIANO,
            description = "Warm vintage electric piano with bell tine overtones",
            brightness = 0.68f,
            attackSeconds = 0.005f,
            decaySeconds = 1.25f,
            sustainLevel = 0.3f,
            releaseSeconds = 0.4f
        ),
        VoicePreset(
            id = "ep_dx_modern",
            name = "DX Worship EP",
            category = VoiceCategory.ELECTRIC_PIANO,
            engineType = SynthEngineType.ELECTRIC_PIANO,
            description = "Sparkling FM electric piano ideal for worship ballads and Tizita",
            brightness = 0.9f,
            attackSeconds = 0.004f,
            decaySeconds = 1.05f,
            sustainLevel = 0.32f,
            releaseSeconds = 0.44f
        ),
        // Organ & Accordion
        VoicePreset(
            id = "organ_church",
            name = "Church Pipe Organ",
            category = VoiceCategory.ORGAN_ACCORDION,
            engineType = SynthEngineType.CHURCH_ORGAN,
            description = "Full cathedral principal & flute stops for hymns and congregational singing",
            brightness = 0.72f,
            attackSeconds = 0.025f,
            decaySeconds = 0.1f,
            sustainLevel = 0.94f,
            releaseSeconds = 0.24f
        ),
        VoicePreset(
            id = "organ_gospel",
            name = "Gospel Drawbar Organ",
            category = VoiceCategory.ORGAN_ACCORDION,
            engineType = SynthEngineType.CHURCH_ORGAN,
            description = "Warm rotary-style gospel organ for worship swells and Ethio-Jazz",
            brightness = 0.82f,
            attackSeconds = 0.01f,
            decaySeconds = 0.1f,
            sustainLevel = 0.92f,
            releaseSeconds = 0.14f,
            vibratoRateHz = 6.2f,
            vibratoDepthSemitones = 0.09f
        ),
        VoicePreset(
            id = "reed_accordion",
            name = "Ethio Accordion",
            category = VoiceCategory.ORGAN_ACCORDION,
            engineType = SynthEngineType.ACCORDION_REED,
            description = "Dual-detuned musette accordion popular in Ethiopian modern music",
            brightness = 0.8f,
            attackSeconds = 0.025f,
            decaySeconds = 0.15f,
            sustainLevel = 0.9f,
            releaseSeconds = 0.16f
        ),
        // Guitars
        VoicePreset(
            id = "guitar_nylon",
            name = "Worship Nylon Guitar",
            category = VoiceCategory.GUITAR,
            engineType = SynthEngineType.ACOUSTIC_GUITAR,
            description = "Warm fingerpicked classical nylon acoustic guitar",
            brightness = 0.56f,
            attackSeconds = 0.005f,
            decaySeconds = 0.82f,
            sustainLevel = 0.14f,
            releaseSeconds = 0.32f
        ),
        VoicePreset(
            id = "guitar_steel",
            name = "Steel Acoustic Guitar",
            category = VoiceCategory.GUITAR,
            engineType = SynthEngineType.ACOUSTIC_GUITAR,
            description = "Crisp bright steel-string guitar for strums and arpeggios",
            brightness = 0.86f,
            attackSeconds = 0.004f,
            decaySeconds = 0.78f,
            sustainLevel = 0.12f,
            releaseSeconds = 0.3f
        ),
        // Brass & Reeds
        VoicePreset(
            id = "brass_addis",
            name = "Addis Brass Section",
            category = VoiceCategory.BRASS_REEDS,
            engineType = SynthEngineType.BRASS_SECTION,
            description = "Punchy trumpet and horn section for praise, Eskista, and funk hits",
            brightness = 0.88f,
            attackSeconds = 0.032f,
            decaySeconds = 0.25f,
            sustainLevel = 0.84f,
            releaseSeconds = 0.18f
        ),
        VoicePreset(
            id = "sax_tenor",
            name = "Soul Tenor Sax",
            category = VoiceCategory.BRASS_REEDS,
            engineType = SynthEngineType.SAXOPHONE,
            description = "Warm expressive tenor saxophone with smooth vibrato",
            brightness = 0.72f,
            attackSeconds = 0.04f,
            decaySeconds = 0.2f,
            sustainLevel = 0.85f,
            releaseSeconds = 0.2f,
            vibratoRateHz = 5.0f,
            vibratoDepthSemitones = 0.16f
        ),
        // Strings & Pads
        VoicePreset(
            id = "strings_cinema",
            name = "Warm Worship Strings",
            category = VoiceCategory.STRINGS_PAD,
            engineType = SynthEngineType.STRINGS_ENSEMBLE,
            description = "Lush bowed string ensemble ideal for layering under Grand Piano",
            brightness = 0.64f,
            attackSeconds = 0.16f,
            decaySeconds = 0.3f,
            sustainLevel = 0.88f,
            releaseSeconds = 0.6f
        ),
        VoicePreset(
            id = "pad_worship",
            name = "Heavenly Worship Pad",
            category = VoiceCategory.STRINGS_PAD,
            engineType = SynthEngineType.WARM_PAD,
            description = "Warm atmospheric shimmer pad for church prayer & transitions",
            brightness = 0.48f,
            attackSeconds = 0.28f,
            decaySeconds = 0.45f,
            sustainLevel = 0.88f,
            releaseSeconds = 0.85f
        ),
        // Synth Lead
        VoicePreset(
            id = "synth_lead",
            name = "Ethio Saw Lead",
            category = VoiceCategory.SYNTH_LEAD,
            engineType = SynthEngineType.SYNTH_LEAD,
            description = "Bright expressive arranger synth lead for live melodies",
            brightness = 0.94f,
            attackSeconds = 0.015f,
            decaySeconds = 0.2f,
            sustainLevel = 0.88f,
            releaseSeconds = 0.2f,
            vibratoRateHz = 5.8f,
            vibratoDepthSemitones = 0.12f
        ),
        // Bass
        VoicePreset(
            id = "bass_fingered",
            name = "Fingered E-Bass",
            category = VoiceCategory.BASS,
            engineType = SynthEngineType.ELECTRIC_BASS,
            description = "Deep punchy electric bass guitar",
            brightness = 0.46f,
            attackSeconds = 0.006f,
            decaySeconds = 0.55f,
            sustainLevel = 0.48f,
            releaseSeconds = 0.16f
        ),
        VoicePreset(
            id = "bass_synth",
            name = "Sub Synth Bass",
            category = VoiceCategory.BASS,
            engineType = SynthEngineType.SYNTH_BASS,
            description = "Tight resonant synthesizer bass",
            brightness = 0.56f,
            attackSeconds = 0.005f,
            decaySeconds = 0.4f,
            sustainLevel = 0.6f,
            releaseSeconds = 0.15f
        )
    )

    fun byId(id: String, extraVoices: List<VoicePreset> = emptyList()): VoicePreset =
        extraVoices.find { it.id == id } ?: all.find { it.id == id } ?: all.first()
}

enum class KeyboardLayoutRange(
    val displayName: String,
    val totalKeys: Int,
    val lowestMidi: Int,
    val highestMidi: Int,
    val defaultVisibleWhites: Int
) {
    KEYS_49("49 Keys (C2–C6)", 49, 36, 84, 15),
    KEYS_61("61 Keys (C2–C7)", 61, 36, 96, 15),
    KEYS_76("76 Keys (E1–G7)", 76, 28, 103, 17),
    KEYS_88("88 Keys (A0–C8)", 88, 21, 108, 18)
}

enum class TouchVelocityCurve(val displayName: String, val description: String) {
    SOFT("Soft", "Reaches full volume easily with gentle touch"),
    NORMAL("Normal", "Balanced dynamic response across vertical key depth"),
    HARD("Hard", "Wide expressive range requiring deeper press for fortissimo"),
    FIXED("Fixed", "Consistent loud output for organ/synth & fast live worship")
}

enum class KeyboardPartTarget(
    val label: String,
    val shortLabel: String,
    val tagBase: Long
) {
    LEFT("LEFT", "L", 400_000L),
    RIGHT_1("RIGHT 1", "R1", 100_000L),
    RIGHT_2("RIGHT 2", "R2", 200_000L),
    RIGHT_3("RIGHT 3", "R3", 300_000L)
}

data class KeyboardPartConfig(
    val part: KeyboardPartTarget,
    val voice: VoicePreset,
    val isEnabled: Boolean,
    val volume: Float = 0.85f,
    val pan: Float = 0f,           // -1f (Left) .. 0f (Center) .. +1f (Right)
    val octaveShift: Int = 0,      // -2 .. +2 independent per-part octave shift
    val lowNoteMidi: Int = 21,     // Independent lower playable note (A0 = 21)
    val highNoteMidi: Int = 108    // Independent upper playable note (C8 = 108)
) {
    val rangeLabel: String
        get() = "${DetectedChord.midiNoteName(lowNoteMidi)}–${DetectedChord.midiNoteName(highNoteMidi)}"
}

data class RoutedPartNote(
    val part: KeyboardPartTarget,
    val voice: VoicePreset,
    val midiNote: Int,
    val volume: Float,
    val pan: Float,
    val audioPart: AccompPart,
    val voiceTag: Long
)

/**
 * Pure four-part keyboard note router supporting independent enable/disable, volume, pan,
 * octave shift, and note-range windows while preserving PSR-SX900 split-point behavior.
 */
object KeyboardNoteRouter {
    const val HARMONY_TAG_BASE = 500_000L

    fun routeKeyDown(
        rawMidiNote: Int,
        splitNoteMidi: Int,
        isSplitEnabled: Boolean,
        globalOctaveShift: Int,
        transposeSemitones: Int,
        parts: Map<KeyboardPartTarget, KeyboardPartConfig>
    ): List<RoutedPartNote> {
        val routed = ArrayList<RoutedPartNote>(4)
        for (target in KeyboardPartTarget.entries) {
            val cfg = parts[target] ?: continue
            if (!cfg.isEnabled) continue

            // Preserve split-point routing when Split is ON:
            // LEFT plays <= splitNoteMidi, RIGHT 1/2/3 play > splitNoteMidi.
            // When Split is OFF, all enabled parts respond across their own independent note ranges.
            val passesSplit = if (isSplitEnabled) {
                if (target == KeyboardPartTarget.LEFT) rawMidiNote <= splitNoteMidi
                else rawMidiNote > splitNoteMidi
            } else {
                true
            }
            if (!passesSplit) continue

            val low = minOf(cfg.lowNoteMidi, cfg.highNoteMidi)
            val high = maxOf(cfg.lowNoteMidi, cfg.highNoteMidi)
            if (rawMidiNote !in low..high) continue

            val shiftedMidi = (
                rawMidiNote + (globalOctaveShift + cfg.octaveShift) * 12 + transposeSemitones
            ).coerceIn(21, 108)

            val audioPart = if (target == KeyboardPartTarget.LEFT) {
                AccompPart.PAD
            } else {
                AccompPart.RIGHT_VOICE
            }

            routed.add(
                RoutedPartNote(
                    part = target,
                    voice = cfg.voice,
                    midiNote = shiftedMidi,
                    volume = cfg.volume.coerceIn(0f, 1f),
                    pan = cfg.pan.coerceIn(-1f, 1f),
                    audioPart = audioPart,
                    voiceTag = target.tagBase + rawMidiNote.toLong()
                )
            )
        }
        return routed
    }

    fun voiceTagFor(part: KeyboardPartTarget, rawMidiNote: Int): Long =
        part.tagBase + rawMidiNote.toLong()

    fun harmonyTagFor(rawMidiNote: Int): Long =
        HARMONY_TAG_BASE + rawMidiNote.toLong()
}

enum class ArrangerSection(val shortLabel: String, val displayLabel: String) {
    INTRO_1("I", "INTRO 1"),
    INTRO_2("II", "INTRO 2"),
    INTRO_3("III", "INTRO 3"),
    MAIN_A("A", "MAIN A"),
    MAIN_B("B", "MAIN B"),
    MAIN_C("C", "MAIN C"),
    MAIN_D("D", "MAIN D"),
    FILL_A("F-A", "FILL A"),
    FILL_B("F-B", "FILL B"),
    FILL_C("F-C", "FILL C"),
    FILL_D("F-D", "FILL D"),
    BREAK("BRK", "BREAK"),
    ENDING_1("I", "ENDING 1"),
    ENDING_2("II", "ENDING 2"),
    ENDING_3("III", "ENDING 3");

    val isIntro: Boolean get() = this == INTRO_1 || this == INTRO_2 || this == INTRO_3
    val isMain: Boolean get() = this == MAIN_A || this == MAIN_B || this == MAIN_C || this == MAIN_D
    val isFill: Boolean get() = this == FILL_A || this == FILL_B || this == FILL_C || this == FILL_D
    val isEnding: Boolean get() = this == ENDING_1 || this == ENDING_2 || this == ENDING_3

    fun correspondingFill(): ArrangerSection = when (this) {
        MAIN_A -> FILL_A
        MAIN_B -> FILL_B
        MAIN_C -> FILL_C
        MAIN_D -> FILL_D
        else -> FILL_A
    }
}

enum class AccompPart(val label: String, val shortLabel: String) {
    DRUMS("Drum Kit", "DRUM"),
    PERCUSSION("Percussion", "PERC"),
    BASS("Bass", "BASS"),
    CHORD_1("Chord 1", "CHD1"),
    CHORD_2("Chord 2", "CHD2"),
    PAD("Pad / Strings", "PAD"),
    PHRASE_1("Phrase 1", "PHR1"),
    PHRASE_2("Phrase 2", "PHR2"),
    RIGHT_VOICE("Right 1/2", "RGT")
}

data class MixerChannelState(
    val part: AccompPart,
    val volume: Float = 0.82f,
    val pan: Float = 0f, // -1f Left .. +1f Right
    val isMuted: Boolean = false
)

enum class ChordQuality(val suffix: String, val intervals: IntArray) {
    MAJOR(" Maj", intArrayOf(0, 4, 7)),
    MINOR("m", intArrayOf(0, 3, 7)),
    DOMINANT_7("7", intArrayOf(0, 4, 7, 10)),
    MAJOR_7("maj7", intArrayOf(0, 4, 7, 11)),
    MINOR_7("m7", intArrayOf(0, 3, 7, 10)),
    SUS4("sus4", intArrayOf(0, 5, 7)),
    SUS2("sus2", intArrayOf(0, 2, 7)),
    DIM("dim", intArrayOf(0, 3, 6)),
    AUG("aug", intArrayOf(0, 4, 8)),
    POWER_5("5", intArrayOf(0, 7))
}

data class DetectedChord(
    val rootPitchClass: Int, // 0 = C .. 11 = B
    val quality: ChordQuality,
    val bassPitchClass: Int = rootPitchClass
) {
    val displayName: String
        get() {
            val rootName = NOTE_NAMES[rootPitchClass.floorMod(12)]
            val slash = if (bassPitchClass != rootPitchClass) {
                "/" + NOTE_NAMES[bassPitchClass.floorMod(12)]
            } else {
                ""
            }
            return "$rootName${quality.suffix}$slash"
        }

    val chordToneNames: List<String>
        get() = quality.intervals.map { NOTE_NAMES[(rootPitchClass + it).floorMod(12)] }

    companion object {
        val NOTE_NAMES = arrayOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
        val DEFAULT_C_MAJOR = DetectedChord(0, ChordQuality.MAJOR)

        fun midiNoteName(midiNote: Int): String {
            val note = NOTE_NAMES[midiNote.floorMod(12)]
            val octave = (midiNote / 12) - 1
            return "$note$octave"
        }

        private fun Int.floorMod(mod: Int): Int = ((this % mod) + mod) % mod
    }
}

enum class DrumSound {
    KICK,
    SNARE,
    CLOSED_HAT,
    OPEN_HAT,
    KEBERO_LOW,
    KEBERO_HIGH,
    CLAP,
    CRASH,
    SHAKER,
    RIM,
    METRO_HIGH,
    METRO_LOW
}

data class StyleStepEvent(
    val stepIndex: Int,
    val part: AccompPart,
    val drumSound: DrumSound? = null,
    val chordToneIndex: Int = 0,
    val semitoneOffset: Int = 0,
    val octaveBase: Int = 3,
    val velocity: Float = 0.8f,
    val durationSteps: Int = 1,
    val absoluteMidiNote: Int? = null
)

data class StyleSectionPattern(
    val section: ArrangerSection,
    val lengthBars: Int = 1,
    val events: List<StyleStepEvent> = emptyList()
)

enum class StyleSourceType {
    BUILTIN,
    IMPORTED_YAMAHA_STY,
    IMPORTED_ORG_STYLE,
    IMPORTED_SMF
}

data class StyleDefinition(
    val id: String,
    val name: String,
    val category: String,
    val tempoBpm: Int,
    val timeSigNumerator: Int = 4,
    val timeSigDenominator: Int = 4,
    val stepsPerBar: Int = 16,
    val sourceType: StyleSourceType = StyleSourceType.BUILTIN,
    val sourceUri: String? = null,
    val fileFormat: String = "BUILTIN",
    val parseDiagnostics: String = "Built-in procedural arranger pattern",
    val otsVoiceIds: List<String> = listOf(
        "piano_cfx",
        "ethio_masinko",
        "strings_cinema",
        "brass_addis"
    ),
    val sections: Map<ArrangerSection, StyleSectionPattern> = emptyMap()
) {
    val timeSignatureLabel: String get() = "$timeSigNumerator/$timeSigDenominator"

    fun patternFor(section: ArrangerSection): StyleSectionPattern {
        return sections[section]
            ?: sections[ArrangerSection.MAIN_A]
            ?: StyleSectionPattern(section, 1, emptyList())
    }
}

data class MultiPadPreset(
    val slot: Int, // 1..4
    val name: String,
    val shortLabel: String,
    val events: List<StyleStepEvent>,
    val lengthSteps: Int = 16
)

enum class SplitChordMode(val displayName: String, val description: String) {
    FINGERED("Fingered", "Play 2–4 note chords below split point"),
    SINGLE_FINGER("Single Finger", "1 key = Major, +black left = Minor, +white left = 7th"),
    FULL_KEYBOARD("Full Keyboard", "Detect chords anywhere on the keyboard")
}

enum class ArrangerScreen(val title: String) {
    PERFORMANCE("Home"),
    STYLE_LIBRARY("Style & SET Library"),
    VOICE_SELECT("Voice & Part Selection"),
    MIXER("Mixer & DSP Effects"),
    PLAYLIST("Church Worship & Regist"),
    MULTI_PAD_LOOPER("Multi Pads & Chord Looper"),
    SETTINGS("Utility, Keyboard & MIDI")
}

data class RegistrationSlotUi(
    val bankNumber: Int = 1, // 1..4
    val slotNumber: Int, // 1..8
    val isPopulated: Boolean = false,
    val label: String = "Empty",
    val styleId: String? = null,
    val tempoBpm: Int = 120,
    val voiceName: String = ""
)

data class WorshipSongUi(
    val id: Long,
    val title: String,
    val category: String, // e.g., Ethiopian Mezmur, Gospel Hymn, Praise
    val keySignature: String,
    val styleId: String,
    val voiceId: String,
    val tempoBpm: Int,
    val transposeSemitones: Int,
    val notes: String
)

data class ImportedSourceUi(
    val id: Long,
    val uri: String,
    val displayName: String,
    val setFamily: String, // "YAMAHA_SET", "ORG_SET", "SINGLE_FILE"
    val isFolder: Boolean,
    val totalFiles: Int,
    val supportedStyles: Int,
    val supportedSamples: Int,
    val unsupportedFiles: Int,
    val inspectionSummary: String,
    val importedAtMillis: Long
)

data class LibraryStyleItemUi(
    val id: String,
    val sourceId: Long?,
    val name: String,
    val category: String,
    val setFamily: String,
    val fileFormat: String,
    val tempoBpm: Int,
    val timeSignature: String,
    val sectionSummary: String,
    val isSupported: Boolean,
    val compatibilityStatus: String, // "PLAYABLE", "SAMPLE_LOADED", "UNSUPPORTED"
    val parseMessage: String,
    val missingResources: String,
    val isBuiltIn: Boolean,
    val uri: String?
)

data class ChordLoopStep(
    val barOffset: Int,
    val chord: DetectedChord
)

data class ArrangerUiState(
    val activeScreen: ArrangerScreen = ArrangerScreen.PERFORMANCE,
    val isPanelLocked: Boolean = false,
    val activeStyle: StyleDefinition,
    val activeSection: ArrangerSection = ArrangerSection.MAIN_A,
    val queuedSection: ArrangerSection? = null,
    val targetMainSection: ArrangerSection = ArrangerSection.MAIN_A,
    val isPlaying: Boolean = false,
    val isSyncStartArmed: Boolean = false,
    val isSyncStopArmed: Boolean = false,
    val isAutoFillEnabled: Boolean = true,
    val isFadeActive: Boolean = false,
    val fadeLabel: String = "",
    val isMetronomeEnabled: Boolean = false,
    val isSustainEnabled: Boolean = false,
    val isSplitEnabled: Boolean = true,
    val isAcmpEnabled: Boolean = true,
    val isTempoLocked: Boolean = false,
    val tempoBpm: Int = 120,
    val measureNumber: Int = 1,
    val currentBeat: Int = 1,
    val beatsPerBar: Int = 4,
    val detectedChord: DetectedChord = DetectedChord.DEFAULT_C_MAJOR,
    // Four-Part Keyboard Architecture (LEFT + RIGHT 1 + RIGHT 2 + RIGHT 3)
    val right1Part: KeyboardPartConfig = KeyboardPartConfig(
        part = KeyboardPartTarget.RIGHT_1,
        voice = BuiltInVoices.byId("piano_cfx"),
        isEnabled = true,
        volume = 0.90f,
        pan = 0f,
        octaveShift = 0,
        lowNoteMidi = 21,
        highNoteMidi = 108
    ),
    val right2Part: KeyboardPartConfig = KeyboardPartConfig(
        part = KeyboardPartTarget.RIGHT_2,
        voice = BuiltInVoices.byId("strings_cinema"),
        isEnabled = false,
        volume = 0.58f,
        pan = -0.25f,
        octaveShift = 0,
        lowNoteMidi = 21,
        highNoteMidi = 108
    ),
    val right3Part: KeyboardPartConfig = KeyboardPartConfig(
        part = KeyboardPartTarget.RIGHT_3,
        voice = BuiltInVoices.byId("ep_dx_modern"),
        isEnabled = false,
        volume = 0.55f,
        pan = 0.25f,
        octaveShift = 0,
        lowNoteMidi = 21,
        highNoteMidi = 108
    ),
    val leftPart: KeyboardPartConfig = KeyboardPartConfig(
        part = KeyboardPartTarget.LEFT,
        voice = BuiltInVoices.byId("pad_worship"),
        isEnabled = true,
        volume = 0.55f,
        pan = -0.15f,
        octaveShift = 0,
        lowNoteMidi = 21,
        highNoteMidi = 108
    ),
    val isHarmonyEnabled: Boolean = false,
    val userSampleVoices: List<VoicePreset> = emptyList(),
    // OTS & Registrations (4 Banks × 8 Slots + Freeze)
    val activeOtsSlot: Int = 1,
    val isOtsLinkEnabled: Boolean = false,
    val activeRegistrationBank: Int = 1, // 1..4
    val activeRegistrationSlot: Int? = 1,
    val isMemoryStoreArmed: Boolean = false,
    val isRegistrationFreezeEnabled: Boolean = false,
    val freezeTempo: Boolean = true,
    val freezeTranspose: Boolean = true,
    val registrationSlots: List<RegistrationSlotUi> = (1..8).map { RegistrationSlotUi(1, it) },
    // Multi-Pads & Chord Looper
    val activeMultiPadSlots: Set<Int> = emptySet(),
    val multiPads: List<MultiPadPreset> = emptyList(),
    val isChordLooperRecording: Boolean = false,
    val isChordLooperPlaying: Boolean = false,
    val chordLoopSteps: List<ChordLoopStep> = emptyList(),
    // Mixer & DSP Effects
    val mixerChannels: List<MixerChannelState> = AccompPart.entries.map { MixerChannelState(it) },
    val masterVolume: Float = 0.85f,
    val acmpBalance: Float = 0.55f,
    val styleVolume: Float = 0.82f,
    val reverbDepth: Float = 0.28f,
    val chorusDepth: Float = 0.20f,
    val isDspEnabled: Boolean = true,
    val eqBass: Float = 0.5f,   // 0..1 (0.5 neutral)
    val eqMid: Float = 0.5f,
    val eqTreble: Float = 0.55f,
    val masterTuneHz: Float = 440.0f, // 415..466 Hz
    val audioBufferFrames: Int = 512, // 256, 512, 1024
    // Realistic Playable Keyboard Configuration
    val keyboardLayout: KeyboardLayoutRange = KeyboardLayoutRange.KEYS_61,
    val keyboardStartMidi: Int = 48, // Current left edge visible C3 (scrollable across 49/61/76/88 range)
    val visibleWhiteKeyCount: Int = 15, // Adjustable key width (10..22)
    val keyboardHeightRatio: Float = 0.45f, // Adjustable keyboard height (0.36..0.54)
    val velocityCurve: TouchVelocityCurve = TouchVelocityCurve.NORMAL,
    val octaveShift: Int = 0,
    val transposeSemitones: Int = 0,
    val splitNoteMidi: Int = 59, // B3
    val showKeyLabels: Boolean = true,
    val showKeyboardScrollbar: Boolean = true,
    val chordMode: SplitChordMode = SplitChordMode.FINGERED,
    val pressedMidiNotes: Set<Int> = emptySet(),
    val pitchBendSemitones: Float = 0f,
    val modulationDepth: Float = 0f,
    // External MIDI, Library & Worship Songs
    val connectedMidiDevices: List<String> = emptyList(),
    val importedSources: List<ImportedSourceUi> = emptyList(),
    val libraryStyles: List<LibraryStyleItemUi> = emptyList(),
    val worshipSongs: List<WorshipSongUi> = emptyList(),
    val isImporting: Boolean = false,
    val statusBanner: String? = null,
    val audioEngineRunning: Boolean = false,
    val peakMeterLevel: Float = 0f
) {
    // Unified lookup for all 4 keyboard parts
    fun partConfig(target: KeyboardPartTarget): KeyboardPartConfig = when (target) {
        KeyboardPartTarget.LEFT -> leftPart
        KeyboardPartTarget.RIGHT_1 -> right1Part
        KeyboardPartTarget.RIGHT_2 -> right2Part
        KeyboardPartTarget.RIGHT_3 -> right3Part
    }

    val allKeyboardParts: Map<KeyboardPartTarget, KeyboardPartConfig>
        get() = mapOf(
            KeyboardPartTarget.LEFT to leftPart,
            KeyboardPartTarget.RIGHT_1 to right1Part,
            KeyboardPartTarget.RIGHT_2 to right2Part,
            KeyboardPartTarget.RIGHT_3 to right3Part
        )

    val activeRightPartsSummary: String
        get() {
            val active = buildList {
                if (right1Part.isEnabled) add("R1")
                if (right2Part.isEnabled) add("R2")
                if (right3Part.isEnabled) add("R3")
            }
            return if (active.isEmpty()) "OFF" else active.joinToString("+")
        }

    // Backward-compatible aliases for existing callers
    val right1Voice: VoicePreset get() = right1Part.voice
    val right2Voice: VoicePreset get() = right2Part.voice
    val right3Voice: VoicePreset get() = right3Part.voice
    val leftVoice: VoicePreset get() = leftPart.voice
    val isRight1Enabled: Boolean get() = right1Part.isEnabled
    val isRight2Enabled: Boolean get() = right2Part.isEnabled
    val isRight3Enabled: Boolean get() = right3Part.isEnabled
    val isLeftVoiceEnabled: Boolean get() = leftPart.isEnabled
    val right1Volume: Float get() = right1Part.volume
    val right2Volume: Float get() = right2Part.volume
    val right3Volume: Float get() = right3Part.volume
    val leftVoiceVolume: Float get() = leftPart.volume
    val rightVoice: VoicePreset get() = right1Part.voice
}
