package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HaasiAlLaylDao {

  // Prayer logs
  @Query("SELECT * FROM prayer_logs WHERE date = :date LIMIT 1")
  fun getPrayerLogForDate(date: String): Flow<PrayerLogEntity?>

  @Query("SELECT * FROM prayer_logs ORDER BY date DESC LIMIT 7")
  fun getRecentPrayerLogs(): Flow<List<PrayerLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdatePrayerLog(log: PrayerLogEntity)

  // Tasbih items
  @Query("SELECT * FROM tasbih_items ORDER BY lastUpdated DESC")
  fun getAllTasbihItems(): Flow<List<TasbihEntity>>

  @Query("SELECT * FROM tasbih_items WHERE id = :id LIMIT 1")
  suspend fun getTasbihItemById(id: String): TasbihEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateTasbih(item: TasbihEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllTasbih(items: List<TasbihEntity>)

  @Query("UPDATE tasbih_items SET currentCount = :newCount, totalLifetimeCount = totalLifetimeCount + 1, lastUpdated = :timestamp WHERE id = :id")
  suspend fun incrementTasbih(id: String, newCount: Int, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE tasbih_items SET currentCount = 0, lastUpdated = :timestamp WHERE id = :id")
  suspend fun resetTasbih(id: String, timestamp: Long = System.currentTimeMillis())

  // Du'a bookmarks
  @Query("SELECT * FROM dua_bookmarks")
  fun getAllBookmarks(): Flow<List<DuaBookmarkEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM dua_bookmarks WHERE duaId = :id)")
  fun isBookmarked(id: String): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addBookmark(bookmark: DuaBookmarkEntity)

  @Query("DELETE FROM dua_bookmarks WHERE duaId = :id")
  suspend fun removeBookmark(id: String)
}
