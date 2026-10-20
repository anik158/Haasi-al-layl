package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.DuaBookmarkEntity
import com.example.data.local.HaasiAlLaylDao
import com.example.data.local.HaasiAlLaylDatabase
import com.example.data.local.PrayerLogEntity
import com.example.data.local.TasbihEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class HaasiAlLaylRepository(private val database: HaasiAlLaylDatabase) {

  private val dao: HaasiAlLaylDao = database.haasiAlLaylDao()

  fun getPrayerLogForDate(date: LocalDate): Flow<PrayerLogEntity?> {
    return dao.getPrayerLogForDate(date.toString())
  }

  /**
   * Flips one prayer for one date atomically. The row is read inside the transaction, so a stale
   * or not-yet-loaded cache can never blank the day's other prayers.
   */
  suspend fun togglePrayerCompletion(date: LocalDate, prayerKey: String) {
    database.withTransaction {
      val dateStr = date.toString()
      val current = dao.getPrayerLogOnce(dateStr) ?: PrayerLogEntity(date = dateStr)
      val flipped = when (prayerKey.uppercase()) {
        "FAJR" -> current.copy(fajrDone = !current.fajrDone)
        "DHUHR" -> current.copy(dhuhrDone = !current.dhuhrDone)
        "ASR" -> current.copy(asrDone = !current.asrDone)
        "MAGHRIB" -> current.copy(maghribDone = !current.maghribDone)
        "ISHA" -> current.copy(ishaDone = !current.ishaDone)
        else -> return@withTransaction
      }
      dao.insertOrUpdatePrayerLog(flipped.copy(lastUpdated = System.currentTimeMillis()))
    }
  }

  suspend fun savePrayerLog(log: PrayerLogEntity) {
    dao.insertOrUpdatePrayerLog(log)
  }

  fun getAllTasbihItems(): Flow<List<TasbihEntity>> = dao.getAllTasbihItems().map { items ->
    items.sortedBy { item ->
      CANONICAL_TASBIH_ORDER.indexOf(item.id).let { if (it < 0) Int.MAX_VALUE else it }
    }
  }

  suspend fun seedDefaultTasbih() {
    dao.insertTasbihIfAbsent(DEFAULT_TASBIH)
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

/**
 * The dhikr every install starts with. Seeded with INSERT OR IGNORE, so these values only ever
 * apply to rows that do not exist yet - an existing row keeps its counts.
 *
 * The English text here is only a fallback; TasbihScreen renders names and meanings through
 * LocalizationManager, keyed by [TasbihEntity.id].
 */
private val DEFAULT_TASBIH = listOf(
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
      name = "Salawat upon the Prophet ﷺ",
      arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
      transliteration = "O Allah, send blessings upon Muhammad ﷺ",
      currentCount = 0,
      targetCount = 100
    ),
    // Qur'an 21:87; its virtue is Tirmidhi 3505 (sahih). Standalone it begins "لَا" - in the mushaf
    // the shadda on the lam comes from the أَن before it. The sunnah gives no number; 100 is only
    // the size of a counting round. Appended last, so existing installs gain it on the next seed
    // without disturbing the others' rows.
    TasbihEntity(
      id = "dua_yunus",
      name = "Dua of Yunus",
      arabic = "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
      transliteration = "There is no god but You; glory be to You; indeed, I have been of the wrongdoers.",
      currentCount = 0,
      targetCount = 100
    ),
    // Bukhari 6406 / Muslim 2694: "two words light on the tongue, heavy in the Balance, beloved to
    // the Most Merciful". That hadith gives no number; 100 is the counting round asked for (the
    // 100 of Bukhari 6405 is for its first half alone). Appended last, like Yunus above.
    TasbihEntity(
      id = "subhanallah_wabihamdihi",
      name = "SubhanAllahi wa bihamdihi",
      arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
      transliteration = "Glory be to Allah and praise be to Him; glory be to Allah, the Most Great.",
      currentCount = 0,
      targetCount = 100
    )
)

/** Display order for the preset chips, independent of when a row was last touched. */
private val CANONICAL_TASBIH_ORDER = DEFAULT_TASBIH.map { it.id }
