package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
  @PrimaryKey val date: String, // Format: YYYY-MM-DD
  val fajrDone: Boolean = false,
  val dhuhrDone: Boolean = false,
  val asrDone: Boolean = false,
  val maghribDone: Boolean = false,
  val ishaDone: Boolean = false,
  val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbih_items")
data class TasbihEntity(
  @PrimaryKey val id: String,
  val name: String,
  val arabic: String,
  val transliteration: String,
  val currentCount: Int = 0,
  val targetCount: Int = 33,
  val totalLifetimeCount: Int = 0,
  val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "dua_bookmarks")
data class DuaBookmarkEntity(
  @PrimaryKey val duaId: String,
  val bookmarkedAt: Long = System.currentTimeMillis()
)
