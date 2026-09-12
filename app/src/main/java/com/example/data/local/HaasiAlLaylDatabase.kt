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
abstract class HaasiAlLaylDatabase : RoomDatabase() {
  abstract fun haasiAlLaylDao(): HaasiAlLaylDao

  companion object {
    @Volatile
    private var INSTANCE: HaasiAlLaylDatabase? = null

    fun getDatabase(context: Context): HaasiAlLaylDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          HaasiAlLaylDatabase::class.java,
          "haasi_al_layl_database"
        // No fallbackToDestructiveMigration: it would silently wipe every lifetime tasbih
        // count, prayer log and bookmark the next time someone bumps the version. Any future
        // schema change needs a real Migration.
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
