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

  /** One-shot read, so a toggle can read-modify-write inside a transaction. */
  @Query("SELECT * FROM prayer_logs WHERE date = :date LIMIT 1")
  suspend fun getPrayerLogOnce(date: String): PrayerLogEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdatePrayerLog(log: PrayerLogEntity)

  // Tasbih items
  // No ORDER BY: the repository imposes a canonical order. Ordering by lastUpdated reshuffled
  // the chip row every time the user counted.
  @Query("SELECT * FROM tasbih_items")
  fun getAllTasbihItems(): Flow<List<TasbihEntity>>

  @Query("SELECT * FROM tasbih_items WHERE id = :id LIMIT 1")
  suspend fun getTasbihItemById(id: String): TasbihEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateTasbih(item: TasbihEntity)

  /**
   * Seeds rows that are absent and leaves existing ones untouched, so counts survive. IGNORE is
   * better than a "is the table empty" guard: it also lets a later release add new dhikr without
   * resetting the ones already there.
   */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertTasbihIfAbsent(items: List<TasbihEntity>)

  @Query("SELECT COUNT(*) FROM tasbih_items")
  suspend fun countTasbihItems(): Int

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
