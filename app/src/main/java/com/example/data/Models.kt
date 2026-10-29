package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization
import java.util.Calendar

enum class ThemeMode(val code: String) {
  SYSTEM("system"),
  LIGHT("light"),
  DARK("dark");

  companion object {
    fun fromCode(code: String?): ThemeMode =
      entries.firstOrNull { it.code == code } ?: SYSTEM
  }
}

enum class RoutineCategory {
  MORNING,
  STUDY,
  WORK,
  HEALTH,
  MINDFUL,
  PERSONAL;

  fun label(strings: AppStrings): String =
    when (this) {
      MORNING -> strings.catMorning
      STUDY -> strings.catStudy
      WORK -> strings.catWork
      HEALTH -> strings.catHealth
      MINDFUL -> strings.catMindful
      PERSONAL -> strings.catPersonal
    }
}

enum class TodoPriority {
  HIGH,
  MEDIUM,
  NORMAL;

  fun label(strings: AppStrings): String =
    when (this) {
      HIGH -> strings.prioHigh
      MEDIUM -> strings.prioMedium
      NORMAL -> strings.prioNormal
    }
}

enum class TodoFilter {
  ALL,
  PENDING,
  COMPLETED
}

enum class FocusPreset(val minutes: Int, val isBreak: Boolean) {
  FOCUS_25(25, false),
  DEEP_50(50, false),
  SHORT_BREAK_5(5, true),
  LONG_BREAK_15(15, true);

  fun label(strings: AppStrings): String =
    when (this) {
      FOCUS_25 -> strings.focusPreset25
      DEEP_50 -> strings.focusPreset50
      SHORT_BREAK_5 -> strings.focusPresetBreak5
      LONG_BREAK_15 -> strings.focusPresetBreak15
    }
}

object WeekdayMask {
  const val ALL_DAYS: Int = 0b1111111 // 127

  val orderedCalendarDays: List<Int> =
    listOf(
      Calendar.SUNDAY,
      Calendar.MONDAY,
      Calendar.TUESDAY,
      Calendar.WEDNESDAY,
      Calendar.THURSDAY,
      Calendar.FRIDAY,
      Calendar.SATURDAY,
    )

  fun bitForCalendarDay(calendarDayOfWeek: Int): Int {
    val index = (calendarDayOfWeek - Calendar.SUNDAY).coerceIn(0, 6)
    return 1 shl index
  }

  fun isScheduledOn(mask: Int, calendarDayOfWeek: Int): Boolean {
    val effectiveMask = if (mask <= 0) ALL_DAYS else (mask and ALL_DAYS)
    return (effectiveMask and bitForCalendarDay(calendarDayOfWeek)) != 0
  }

  fun toggleDay(mask: Int, calendarDayOfWeek: Int): Int {
    val bit = bitForCalendarDay(calendarDayOfWeek)
    val toggled = mask xor bit
    // Prevent 0 selected days so routine always belongs to at least one day
    return if ((toggled and ALL_DAYS) == 0) mask else (toggled and ALL_DAYS)
  }

  fun summaryLabel(mask: Int, language: AppLanguage, strings: AppStrings): String {
    val effective = if (mask <= 0) ALL_DAYS else (mask and ALL_DAYS)
    if (effective == ALL_DAYS) return strings.everyDayLabel
    return orderedCalendarDays
      .filter { isScheduledOn(effective, it) }
      .joinToString(", ") { Localization.shortWeekdayLabel(it, language) }
  }
}

@Entity(tableName = "routines")
data class RoutineEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val note: String = "",
  val hour: Int,
  val minute: Int,
  val category: String = RoutineCategory.PERSONAL.name,
  val isReminderEnabled: Boolean = true,
  val lastCompletedDayKey: Int = 0, // yyyyMMdd format
  val createdAt: Long = System.currentTimeMillis(),
  val repeatDaysMask: Int = WeekdayMask.ALL_DAYS,
) {
  val categoryEnum: RoutineCategory
    get() = RoutineCategory.entries.firstOrNull { it.name == category } ?: RoutineCategory.PERSONAL

  fun isScheduledOnDayOfWeek(calendarDayOfWeek: Int): Boolean =
    WeekdayMask.isScheduledOn(repeatDaysMask, calendarDayOfWeek)
}

@Entity(
  tableName = "routine_completions",
  primaryKeys = ["routineId", "dayKey"],
  indices = [Index(value = ["dayKey"])],
)
data class RoutineCompletionEntity(
  val routineId: Long,
  val dayKey: Int, // yyyyMMdd
  val completedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val taskTitle: String,
  val durationMinutes: Int,
  val dayKey: Int,
  val completedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "todos")
data class TodoEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val note: String = "",
  val priority: String = TodoPriority.NORMAL.name,
  val scheduledHour: Int? = null,
  val scheduledMinute: Int? = null,
  val isReminderEnabled: Boolean = true,
  val isCompleted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
) {
  val priorityEnum: TodoPriority
    get() = TodoPriority.entries.firstOrNull { it.name == priority } ?: TodoPriority.NORMAL

  val hasScheduledTime: Boolean
    get() = scheduledHour != null && scheduledMinute != null
}

object DayKeyUtil {
  fun dayKeyForMillis(millis: Long = System.currentTimeMillis()): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return dayKeyFromCalendar(cal)
  }

  fun todayDayKey(nowMillis: Long = System.currentTimeMillis()): Int = dayKeyForMillis(nowMillis)

  fun dayKeyFromCalendar(cal: Calendar): Int {
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    return year * 10000 + month * 100 + day
  }

  fun calendarForDaysOffset(nowMillis: Long, daysOffset: Int): Calendar {
    return Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, 12)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      add(Calendar.DAY_OF_YEAR, daysOffset)
    }
  }
}
