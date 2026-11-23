package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ImportedSourceEntity::class,
        StyleEntryEntity::class,
        RegistrationSlotEntity::class,
        WorshipSongEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Safe non-destructive migration from v2 -> v3 adding Stage 1 Four-Part Keyboard fields
         * (RIGHT 3 + independent enable, volume, pan, octave, and note ranges) to registration_slots
         * while preserving all existing imported sources, styles, worship songs, and registrations.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN isRight1Enabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN isLeftEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right3VoiceId TEXT NOT NULL DEFAULT 'ep_dx_modern'")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN isRight3Enabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right1Volume REAL NOT NULL DEFAULT 0.90")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right2Volume REAL NOT NULL DEFAULT 0.58")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right3Volume REAL NOT NULL DEFAULT 0.55")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN leftVolume REAL NOT NULL DEFAULT 0.55")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right1Pan REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right2Pan REAL NOT NULL DEFAULT -0.25")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right3Pan REAL NOT NULL DEFAULT 0.25")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN leftPan REAL NOT NULL DEFAULT -0.15")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right1Octave INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right2Octave INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right3Octave INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN leftOctave INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right1LowMidi INTEGER NOT NULL DEFAULT 21")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right1HighMidi INTEGER NOT NULL DEFAULT 108")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right2LowMidi INTEGER NOT NULL DEFAULT 21")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right2HighMidi INTEGER NOT NULL DEFAULT 108")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right3LowMidi INTEGER NOT NULL DEFAULT 21")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN right3HighMidi INTEGER NOT NULL DEFAULT 108")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN leftLowMidi INTEGER NOT NULL DEFAULT 21")
                db.execSQL("ALTER TABLE registration_slots ADD COLUMN leftHighMidi INTEGER NOT NULL DEFAULT 108")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ethio_yamaha_library.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
