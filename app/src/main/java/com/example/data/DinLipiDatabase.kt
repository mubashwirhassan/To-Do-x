package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities =
    [
      RoutineEntity::class,
      TodoEntity::class,
      RoutineCompletionEntity::class,
      FocusSessionEntity::class,
    ],
  version = 2,
  exportSchema = false,
)
abstract class DinLipiDatabase : RoomDatabase() {
  abstract fun dao(): DinLipiDao

  companion object {
    @Volatile private var instance: DinLipiDatabase? = null

    val MIGRATION_1_2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
            "ALTER TABLE routines ADD COLUMN repeatDaysMask INTEGER NOT NULL DEFAULT 127"
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS routine_completions (
              routineId INTEGER NOT NULL,
              dayKey INTEGER NOT NULL,
              completedAt INTEGER NOT NULL,
              PRIMARY KEY(routineId, dayKey)
            )
            """.trimIndent()
          )
          db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_routine_completions_dayKey ON routine_completions(dayKey)"
          )
          db.execSQL(
            """
            INSERT OR IGNORE INTO routine_completions (routineId, dayKey, completedAt)
            SELECT id, lastCompletedDayKey, createdAt FROM routines WHERE lastCompletedDayKey > 0
            """.trimIndent()
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS focus_sessions (
              id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              taskTitle TEXT NOT NULL,
              durationMinutes INTEGER NOT NULL,
              dayKey INTEGER NOT NULL,
              completedAt INTEGER NOT NULL
            )
            """.trimIndent()
          )
        }
      }

    fun getInstance(context: Context): DinLipiDatabase {
      return instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                DinLipiDatabase::class.java,
                "dinlipi_offline.db",
              )
              .addMigrations(MIGRATION_1_2)
              .fallbackToDestructiveMigration(true)
              .build()
              .also { instance = it }
        }
    }
  }
}
