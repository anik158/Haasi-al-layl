package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    PrayerLogEntity::class,
    TasbihEntity::class,
    DuaBookmarkEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class MuslimDayDatabase : RoomDatabase() {
  abstract fun muslimDayDao(): MuslimDayDao

  companion object {
    @Volatile
    private var INSTANCE: MuslimDayDatabase? = null

    fun getDatabase(context: Context): MuslimDayDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MuslimDayDatabase::class.java,
          "muslim_day_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
