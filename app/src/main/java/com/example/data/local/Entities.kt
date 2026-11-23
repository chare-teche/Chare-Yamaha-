package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imported_sources")
data class ImportedSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val uri: String,
    val displayName: String,
    val setFamily: String, // "YAMAHA_SET", "ORG_SET", "SINGLE_FILE"
    val isFolder: Boolean,
    val totalFiles: Int,
    val supportedStyles: Int,
    val supportedSamples: Int,
    val unsupportedFiles: Int,
    val inspectionSummary: String,
    val importedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "style_entries")
data class StyleEntryEntity(
    @PrimaryKey val id: String,
    val sourceId: Long,
    val name: String,
    val category: String,
    val setFamily: String,
    val fileFormat: String,
    val uri: String,
    val tempoBpm: Int,
    val timeSigNum: Int,
    val timeSigDen: Int,
    val sectionSummary: String,
    val isSupported: Boolean,
    val compatibilityStatus: String,
    val parseMessage: String,
    val missingResources: String,
    val importedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "registration_slots")
data class RegistrationSlotEntity(
    @PrimaryKey val slotKey: Int, // bankNumber * 10 + slotIndex (e.g., 11..18, 21..28, 31..38, 41..48)
    val bankNumber: Int, // 1..4
    val slotIndex: Int, // 1..8
    val label: String,
    val styleId: String,
    val sectionName: String,
    val tempoBpm: Int,
    val right1VoiceId: String,
    val right2VoiceId: String,
    val isRight2Enabled: Boolean,
    val leftVoiceId: String,
    val splitNoteMidi: Int,
    val transposeSemitones: Int,
    val octaveShift: Int,
    val activeOtsSlot: Int,
    val acmpEnabled: Boolean,
    val splitEnabled: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    // Stage 1: Four-Part Keyboard Architecture fields (safe v2 -> v3 migration)
    val isRight1Enabled: Boolean = true,
    val isLeftEnabled: Boolean = true,
    val right3VoiceId: String = "ep_dx_modern",
    val isRight3Enabled: Boolean = false,
    val right1Volume: Float = 0.90f,
    val right2Volume: Float = 0.58f,
    val right3Volume: Float = 0.55f,
    val leftVolume: Float = 0.55f,
    val right1Pan: Float = 0f,
    val right2Pan: Float = -0.25f,
    val right3Pan: Float = 0.25f,
    val leftPan: Float = -0.15f,
    val right1Octave: Int = 0,
    val right2Octave: Int = 0,
    val right3Octave: Int = 0,
    val leftOctave: Int = 0,
    val right1LowMidi: Int = 21,
    val right1HighMidi: Int = 108,
    val right2LowMidi: Int = 21,
    val right2HighMidi: Int = 108,
    val right3LowMidi: Int = 21,
    val right3HighMidi: Int = 108,
    val leftLowMidi: Int = 21,
    val leftHighMidi: Int = 108
)

@Entity(tableName = "worship_songs")
data class WorshipSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String,
    val keySignature: String,
    val styleId: String,
    val voiceId: String,
    val tempoBpm: Int,
    val transposeSemitones: Int,
    val notes: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
