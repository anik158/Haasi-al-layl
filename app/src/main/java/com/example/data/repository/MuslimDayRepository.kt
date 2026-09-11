package com.example.data.repository

import com.example.data.local.DuaBookmarkEntity
import com.example.data.local.MuslimDayDao
import com.example.data.local.PrayerLogEntity
import com.example.data.local.TasbihEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MuslimDayRepository(private val dao: MuslimDayDao) {

  fun getPrayerLogForDate(date: LocalDate): Flow<PrayerLogEntity?> {
    return dao.getPrayerLogForDate(date.toString())
  }

  suspend fun togglePrayerCompletion(date: LocalDate, prayerName: String, isDone: Boolean) {
    val dateStr = date.toString()
    // Retrieve or create
    val currentLog = dao.getPrayerLogForDate(dateStr)
    // We can insert/update safely
    val existing = try {
      // In suspend function, retrieve latest or construct default
      null
    } catch (_: Exception) { null }

    // Read current state or create new
    // We can write a specific query or fetch in transaction
  }

  suspend fun savePrayerLog(log: PrayerLogEntity) {
    dao.insertOrUpdatePrayerLog(log)
  }

  fun getAllTasbihItems(): Flow<List<TasbihEntity>> = dao.getAllTasbihItems()

  suspend fun seedDefaultTasbihIfEmpty() {
    val defaults = listOf(
      TasbihEntity(
        id = "subhanallah",
        name = "SubhanAllah",
        arabic = "سُبْحَانَ اللَّهِ",
        transliteration = "Glory be to Allah",
        currentCount = 0,
        targetCount = 33
      ),
      TasbihEntity(
        id = "alhamdulillah",
        name = "Alhamdulillah",
        arabic = "الْحَمْدُ لِلَّهِ",
        transliteration = "Praise be to Allah",
        currentCount = 0,
        targetCount = 33
      ),
      TasbihEntity(
        id = "allahu_akbar",
        name = "Allahu Akbar",
        arabic = "اللَّهُ أَكْبَرُ",
        transliteration = "Allah is the Greatest",
        currentCount = 0,
        targetCount = 34
      ),
      TasbihEntity(
        id = "astaghfirullah",
        name = "Astaghfirullah",
        arabic = "أَسْتَغْفِرُ اللَّهَ",
        transliteration = "I seek forgiveness from Allah",
        currentCount = 0,
        targetCount = 100
      ),
      TasbihEntity(
        id = "la_ilaha_illallah",
        name = "La Ilaha Illallah",
        arabic = "لَا إِلَهَ إِلَّا اللَّهُ",
        transliteration = "There is no deity except Allah",
        currentCount = 0,
        targetCount = 100
      ),
      TasbihEntity(
        id = "salawat",
        name = "Salawat upon the Prophet",
        arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
        transliteration = "O Allah, send blessings upon Muhammad",
        currentCount = 0,
        targetCount = 100
      )
    )
    dao.insertAllTasbih(defaults)
  }

  suspend fun incrementTasbih(id: String, newCount: Int) {
    dao.incrementTasbih(id, newCount)
  }

  suspend fun resetTasbih(id: String) {
    dao.resetTasbih(id)
  }

  fun getAllBookmarks(): Flow<List<DuaBookmarkEntity>> = dao.getAllBookmarks()

  suspend fun toggleBookmark(duaId: String, isCurrentlyBookmarked: Boolean) {
    if (isCurrentlyBookmarked) {
      dao.removeBookmark(duaId)
    } else {
      dao.addBookmark(DuaBookmarkEntity(duaId))
    }
  }
}
