package com.example.ui

import com.example.data.DayKeyUtil
import com.example.data.FocusSessionEntity
import com.example.data.RoutineCategory
import com.example.data.RoutineCompletionEntity
import com.example.data.RoutineEntity
import java.util.Calendar

data class DailyProgressPoint(
  val dayKey: Int,
  val calendarDayOfWeek: Int,
  val scheduledCount: Int,
  val completedCount: Int,
  val isToday: Boolean,
) {
  val ratio: Float
    get() =
      if (scheduledCount <= 0) 0f
      else (completedCount.toFloat() / scheduledCount.toFloat()).coerceIn(0f, 1f)

  val percent: Int
    get() = (ratio * 100f).toInt()
}

data class ProgressSummary(
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val weeklyAveragePercent: Int = 0,
  val weeklyPoints: List<DailyProgressPoint> = emptyList(),
  val todayFocusSessions: Int = 0,
  val todayFocusMinutes: Int = 0,
  val totalFocusMinutes: Int = 0,
  val categoryBreakdown: List<Pair<RoutineCategory, Int>> = emptyList(),
)

object StatsCalculator {

  fun computeSummary(
    routines: List<RoutineEntity>,
    completions: List<RoutineCompletionEntity>,
    focusSessions: List<FocusSessionEntity>,
    nowMillis: Long,
  ): ProgressSummary {
    val todayKey = DayKeyUtil.todayDayKey(nowMillis)
    val validRoutineIds = routines.map { it.id }.toSet()
    val completionsByDay: Map<Int, Set<Long>> =
      completions
        .filter { it.routineId in validRoutineIds }
        .groupBy({ it.dayKey }, { it.routineId })
        .mapValues { it.value.toSet() }

    // Last 7 days bar points (oldest -> today)
    val weeklyPoints =
      (-6..0).map { offset ->
        val cal = DayKeyUtil.calendarForDaysOffset(nowMillis, offset)
        val dayKey = DayKeyUtil.dayKeyFromCalendar(cal)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val scheduledRoutines = routines.filter { it.isScheduledOnDayOfWeek(dow) }
        val doneIds = completionsByDay[dayKey].orEmpty()
        val doneCount = scheduledRoutines.count { it.id in doneIds }

        DailyProgressPoint(
          dayKey = dayKey,
          calendarDayOfWeek = dow,
          scheduledCount = scheduledRoutines.size,
          completedCount = doneCount,
          isToday = offset == 0,
        )
      }

    val activePoints = weeklyPoints.filter { it.scheduledCount > 0 }
    val weeklyAvg =
      if (activePoints.isEmpty()) 0
      else activePoints.map { it.percent }.average().toInt()

    // Calculate current streak & best streak across the past 365 days
    var currentStreak = 0
    var countingCurrent = true
    var runningStreak = 0
    var bestStreak = 0

    for (daysBack in 0..365) {
      val cal = DayKeyUtil.calendarForDaysOffset(nowMillis, -daysBack)
      val dayKey = DayKeyUtil.dayKeyFromCalendar(cal)
      val dow = cal.get(Calendar.DAY_OF_WEEK)
      val scheduledOnDay = routines.filter { it.isScheduledOnDayOfWeek(dow) }

      if (scheduledOnDay.isEmpty()) {
        // Off-days with 0 scheduled routines don't break a streak
        continue
      }

      val doneIds = completionsByDay[dayKey].orEmpty()
      val completedAny = scheduledOnDay.any { it.id in doneIds }

      if (completedAny) {
        runningStreak += 1
        if (runningStreak > bestStreak) bestStreak = runningStreak
        if (countingCurrent) {
          currentStreak += 1
        }
      } else {
        if (daysBack == 0) {
          // Today is still in progress; don't reset currentStreak yet if yesterday was completed!
          runningStreak = 0
        } else {
          countingCurrent = false
          runningStreak = 0
        }
      }
    }
    if (currentStreak > bestStreak) {
      bestStreak = currentStreak
    }

    val todayFocus = focusSessions.filter { it.dayKey == todayKey }
    val todayFocusMins = todayFocus.sumOf { it.durationMinutes }
    val totalFocusMins = focusSessions.sumOf { it.durationMinutes }

    val categoryCounts =
      RoutineCategory.entries
        .map { cat -> cat to routines.count { it.categoryEnum == cat } }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }

    return ProgressSummary(
      currentStreak = currentStreak,
      bestStreak = bestStreak,
      weeklyAveragePercent = weeklyAvg,
      weeklyPoints = weeklyPoints,
      todayFocusSessions = todayFocus.size,
      todayFocusMinutes = todayFocusMins,
      totalFocusMinutes = totalFocusMins,
      categoryBreakdown = categoryCounts,
    )
  }
}
