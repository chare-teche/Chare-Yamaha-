package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM imported_sources ORDER BY importedAtMillis DESC")
    fun observeSources(): Flow<List<ImportedSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: ImportedSourceEntity): Long

    @Query("DELETE FROM imported_sources WHERE id = :sourceId")
    suspend fun deleteSourceById(sourceId: Long)

    @Query("SELECT * FROM style_entries ORDER BY isSupported DESC, name ASC")
    fun observeStyleEntries(): Flow<List<StyleEntryEntity>>

    @Query("SELECT * FROM style_entries WHERE id = :id LIMIT 1")
    suspend fun getStyleEntryById(id: String): StyleEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyleEntries(entries: List<StyleEntryEntity>)

    @Query("DELETE FROM style_entries WHERE sourceId = :sourceId")
    suspend fun deleteStyleEntriesForSource(sourceId: Long)

    @Query("DELETE FROM style_entries WHERE id = :styleId")
    suspend fun deleteStyleEntryById(styleId: String)

    @Query("SELECT * FROM registration_slots ORDER BY slotKey ASC")
    fun observeAllRegistrationSlots(): Flow<List<RegistrationSlotEntity>>

    @Query("SELECT * FROM registration_slots WHERE slotKey = :slotKey LIMIT 1")
    suspend fun getRegistrationSlot(slotKey: Int): RegistrationSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRegistrationSlot(slot: RegistrationSlotEntity)

    @Query("DELETE FROM registration_slots WHERE slotKey = :slotKey")
    suspend fun clearRegistrationSlot(slotKey: Int)

    @Query("SELECT * FROM worship_songs ORDER BY createdAtMillis DESC")
    fun observeWorshipSongs(): Flow<List<WorshipSongEntity>>

    @Query("SELECT COUNT(*) FROM worship_songs")
    suspend fun countWorshipSongs(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorshipSong(song: WorshipSongEntity): Long

    @Query("DELETE FROM worship_songs WHERE id = :songId")
    suspend fun deleteWorshipSong(songId: Long)
}
