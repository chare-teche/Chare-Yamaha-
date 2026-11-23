package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.RegistrationSlotEntity
import com.example.domain.model.BuiltInVoices
import com.example.domain.model.KeyboardPartTarget
import com.example.viewmodel.ArrangerViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FourPartRegistrationRobolectricTest {

    @Test
    fun migration2To3_preservesExistingUserDataAndPopulatesRight3Defaults() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_ethio_migration_2_3.db"
        context.deleteDatabase(dbName)

        // 1. Seed a real version 2 SQLite database with pre-existing worship song & registration slot
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        val rawDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        rawDb.version = 2
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `imported_sources` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uri` TEXT NOT NULL,
                `displayName` TEXT NOT NULL,
                `setFamily` TEXT NOT NULL,
                `isFolder` INTEGER NOT NULL,
                `totalFiles` INTEGER NOT NULL,
                `supportedStyles` INTEGER NOT NULL,
                `supportedSamples` INTEGER NOT NULL,
                `unsupportedFiles` INTEGER NOT NULL,
                `inspectionSummary` TEXT NOT NULL,
                `importedAtMillis` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `style_entries` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `sourceId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `setFamily` TEXT NOT NULL,
                `fileFormat` TEXT NOT NULL,
                `uri` TEXT NOT NULL,
                `tempoBpm` INTEGER NOT NULL,
                `timeSigNum` INTEGER NOT NULL,
                `timeSigDen` INTEGER NOT NULL,
                `sectionSummary` TEXT NOT NULL,
                `isSupported` INTEGER NOT NULL,
                `compatibilityStatus` TEXT NOT NULL,
                `parseMessage` TEXT NOT NULL,
                `missingResources` TEXT NOT NULL,
                `importedAtMillis` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `registration_slots` (
                `slotKey` INTEGER NOT NULL PRIMARY KEY,
                `bankNumber` INTEGER NOT NULL,
                `slotIndex` INTEGER NOT NULL,
                `label` TEXT NOT NULL,
                `styleId` TEXT NOT NULL,
                `sectionName` TEXT NOT NULL,
                `tempoBpm` INTEGER NOT NULL,
                `right1VoiceId` TEXT NOT NULL,
                `right2VoiceId` TEXT NOT NULL,
                `isRight2Enabled` INTEGER NOT NULL,
                `leftVoiceId` TEXT NOT NULL,
                `splitNoteMidi` INTEGER NOT NULL,
                `transposeSemitones` INTEGER NOT NULL,
                `octaveShift` INTEGER NOT NULL,
                `activeOtsSlot` INTEGER NOT NULL,
                `acmpEnabled` INTEGER NOT NULL,
                `splitEnabled` INTEGER NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `worship_songs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `keySignature` TEXT NOT NULL,
                `styleId` TEXT NOT NULL,
                `voiceId` TEXT NOT NULL,
                `tempoBpm` INTEGER NOT NULL,
                `transposeSemitones` INTEGER NOT NULL,
                `notes` TEXT NOT NULL,
                `createdAtMillis` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            INSERT INTO registration_slots (
                slotKey, bankNumber, slotIndex, label, styleId, sectionName, tempoBpm,
                right1VoiceId, right2VoiceId, isRight2Enabled, leftVoiceId, splitNoteMidi,
                transposeSemitones, octaveShift, activeOtsSlot, acmpEnabled, splitEnabled, updatedAtMillis
            ) VALUES (
                11, 1, 1, 'Legacy Worship', 'builtin_ethio_worship', 'MAIN_B', 84,
                'piano_worship', 'strings_cinema', 1, 'pad_worship', 59,
                2, 0, 1, 1, 1, 1700000000000
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            INSERT INTO worship_songs (
                title, category, keySignature, styleId, voiceId, tempoBpm, transposeSemitones, notes, createdAtMillis
            ) VALUES (
                'My Saved Mezmur', 'Mezmur', 'Dm', 'builtin_ethio_worship', 'piano_worship', 82, 0, 'Keep safe', 1700000000000
            )
            """.trimIndent()
        )
        rawDb.close()

        // 2. Open with Room v3 + MIGRATION_2_3 (no destructive migration!)
        val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

        try {
            val dao = migratedDb.appDao()
            val preservedSlot = dao.getRegistrationSlot(11)
            assertNotNull("Legacy v2 registration slot must survive migration", preservedSlot)
            assertEquals("Legacy Worship", preservedSlot!!.label)
            assertEquals("piano_worship", preservedSlot.right1VoiceId)
            assertTrue(preservedSlot.isRight2Enabled)
            // Verify Stage 1 defaults populated safely
            assertTrue(preservedSlot.isRight1Enabled)
            assertTrue(preservedSlot.isLeftEnabled)
            assertEquals("ep_dx_modern", preservedSlot.right3VoiceId)
            assertFalse(preservedSlot.isRight3Enabled)
            assertEquals(21, preservedSlot.right3LowMidi)
            assertEquals(108, preservedSlot.right3HighMidi)

            assertEquals(1, dao.countWorshipSongs())
        } finally {
            migratedDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun registrationMemory_savesAndRecallsAllFourPartsIncludingRight3() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val dao = db.appDao()
            val slotToSave = RegistrationSlotEntity(
                slotKey = 13,
                bankNumber = 1,
                slotIndex = 3,
                label = "4-Part Ethio Praise",
                styleId = "builtin_ethio_jazz",
                sectionName = "MAIN_C",
                tempoBpm = 114,
                right1VoiceId = "piano_cfx",
                right2VoiceId = "strings_cinema",
                isRight2Enabled = true,
                leftVoiceId = "bass_fingered",
                splitNoteMidi = 60,
                transposeSemitones = -2,
                octaveShift = 0,
                activeOtsSlot = 2,
                acmpEnabled = true,
                splitEnabled = true,
                isRight1Enabled = true,
                isLeftEnabled = true,
                right3VoiceId = "brass_addis",
                isRight3Enabled = true,
                right1Volume = 0.88f,
                right2Volume = 0.62f,
                right3Volume = 0.74f,
                leftVolume = 0.70f,
                right1Pan = 0.0f,
                right2Pan = -0.35f,
                right3Pan = 0.40f,
                leftPan = -0.20f,
                right1Octave = 0,
                right2Octave = 1,
                right3Octave = -1,
                leftOctave = -1,
                right1LowMidi = 61,
                right1HighMidi = 96,
                right2LowMidi = 61,
                right2HighMidi = 84,
                right3LowMidi = 72,
                right3HighMidi = 96,
                leftLowMidi = 36,
                leftHighMidi = 60
            )
            dao.upsertRegistrationSlot(slotToSave)

            val recalled = dao.getRegistrationSlot(13)
            assertNotNull(recalled)
            assertEquals("brass_addis", recalled!!.right3VoiceId)
            assertTrue(recalled.isRight3Enabled)
            assertEquals(0.74f, recalled.right3Volume, 0.001f)
            assertEquals(0.40f, recalled.right3Pan, 0.001f)
            assertEquals(-1, recalled.right3Octave)
            assertEquals(72, recalled.right3LowMidi)
            assertEquals(96, recalled.right3HighMidi)
            assertEquals(-1, recalled.leftOctave)
            assertEquals(1, recalled.right2Octave)
        } finally {
            db.close()
        }
    }

    @Test
    fun arrangerViewModel_togglesAndConfiguresAllFourPartsIndependently() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = ArrangerViewModel(app)

        vm.selectPartVoice(KeyboardPartTarget.RIGHT_3, BuiltInVoices.byId("brass_addis"))
        vm.setPartVolume(KeyboardPartTarget.RIGHT_3, 0.77f)
        vm.setPartPan(KeyboardPartTarget.RIGHT_3, 0.50f)
        vm.setPartOctave(KeyboardPartTarget.RIGHT_3, 1)
        vm.setPartNoteRange(KeyboardPartTarget.RIGHT_3, 66, 96)

        val afterConfig = vm.uiState.value.right3Part
        assertTrue(afterConfig.isEnabled)
        assertEquals("brass_addis", afterConfig.voice.id)
        assertEquals(0.77f, afterConfig.volume, 0.001f)
        assertEquals(0.50f, afterConfig.pan, 0.001f)
        assertEquals(1, afterConfig.octaveShift)
        assertEquals(66, afterConfig.lowNoteMidi)
        assertEquals(96, afterConfig.highNoteMidi)

        // Toggle RIGHT_3 OFF without affecting RIGHT_1 or LEFT
        vm.togglePartEnabled(KeyboardPartTarget.RIGHT_3)
        val afterToggle = vm.uiState.value
        assertFalse(afterToggle.isRight3Enabled)
        assertTrue(afterToggle.isRight1Enabled)
        assertTrue(afterToggle.isLeftVoiceEnabled)
    }
}
