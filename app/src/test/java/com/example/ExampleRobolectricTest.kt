package com.example

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.DayKeyUtil
import com.example.data.DinLipiDatabase
import com.example.data.PlannerRepository
import com.example.data.RoutineCategory
import com.example.data.RoutineCompletionEntity
import com.example.data.RoutineEntity
import com.example.data.TodoPriority
import com.example.data.WeekdayMask
import com.example.i18n.AppLanguage
import com.example.i18n.Localization
import com.example.reminder.ReminderScheduler
import com.example.ui.StatsCalculator
import java.util.Calendar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: DinLipiDatabase
  private lateinit var scheduler: ReminderScheduler
  private lateinit var repository: PlannerRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db =
      Room.inMemoryDatabaseBuilder(context, DinLipiDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    scheduler = ReminderScheduler(context)
    repository = PlannerRepository(db.dao(), scheduler)
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `app name matches DinLipi`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("DinLipi", appName)
  }

  @Test
  fun `one minute before calculation handles normal and midnight boundary`() {
    assertEquals(10 to 29, Localization.oneMinuteBefore(10, 30))
    assertEquals(9 to 59, Localization.oneMinuteBefore(10, 0))
    assertEquals(23 to 59, Localization.oneMinuteBefore(0, 0))
    assertEquals("সকাল ১০:৩০", Localization.formatTime(10, 30, AppLanguage.BN))
    assertEquals("10:30 AM", Localization.formatTime(10, 30, AppLanguage.EN))
  }

  @Test
  fun `weekday-aware scheduler skips unscheduled days and triggers 1 minute early on next scheduled day`() {
    // Thursday Oct 29, 2026 at 08:00 AM
    val thursdayMorning =
      Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 29, 8, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis

    // Routine scheduled ONLY on Saturday (Calendar.SATURDAY) at 10:30
    val saturdayOnlyMask = WeekdayMask.bitForCalendarDay(Calendar.SATURDAY)
    val trigger =
      ReminderScheduler.computeNextOneMinutePriorTriggerMillis(
        scheduledHour = 10,
        scheduledMinute = 30,
        nowMillis = thursdayMorning,
        repeatDaysMask = saturdayOnlyMask,
      )

    val triggerCal = Calendar.getInstance().apply { timeInMillis = trigger }
    assertEquals(Calendar.SATURDAY, triggerCal.get(Calendar.DAY_OF_WEEK))
    assertEquals(10, triggerCal.get(Calendar.HOUR_OF_DAY))
    assertEquals(29, triggerCal.get(Calendar.MINUTE))
    assertEquals(0, triggerCal.get(Calendar.SECOND))
  }

  @Test
  fun `streak calculator computes consecutive streaks and focus totals accurately`() =
    runBlocking {
      val now =
        Calendar.getInstance().apply {
          set(2026, Calendar.OCTOBER, 29, 14, 0, 0)
          set(Calendar.MILLISECOND, 0)
        }.timeInMillis

      val routine =
        RoutineEntity(
          id = 1L,
          title = "গভীর পড়াশোনা",
          hour = 9,
          minute = 0,
          category = RoutineCategory.STUDY.name,
          repeatDaysMask = WeekdayMask.ALL_DAYS,
        )

      val todayKey = DayKeyUtil.dayKeyFromCalendar(DayKeyUtil.calendarForDaysOffset(now, 0))
      val yesterdayKey = DayKeyUtil.dayKeyFromCalendar(DayKeyUtil.calendarForDaysOffset(now, -1))
      val twoDaysAgoKey = DayKeyUtil.dayKeyFromCalendar(DayKeyUtil.calendarForDaysOffset(now, -2))

      val completions =
        listOf(
          RoutineCompletionEntity(routineId = 1L, dayKey = todayKey),
          RoutineCompletionEntity(routineId = 1L, dayKey = yesterdayKey),
          RoutineCompletionEntity(routineId = 1L, dayKey = twoDaysAgoKey),
        )

      repository.recordFocusSession("গভীর পড়াশোনা", 25, todayKey)
      val focusSessions = repository.focusSessionsFlow.first()

      val summary =
        StatsCalculator.computeSummary(
          routines = listOf(routine),
          completions = completions,
          focusSessions = focusSessions,
          nowMillis = now,
        )

      assertEquals(3, summary.currentStreak)
      assertEquals(3, summary.bestStreak)
      assertEquals(25, summary.todayFocusMinutes)
      assertEquals(7, summary.weeklyPoints.size)
    }

  @Test
  fun `saving routine and timed todo schedules system alarms and deleting cancels them`() =
    runBlocking {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val shadowAlarmManager = Shadows.shadowOf(alarmManager)

      val routineId =
        repository.saveRoutine(
          title = "সকালের পড়াশোনা",
          note = "অধ্যায় ৩",
          hour = 23,
          minute = 45,
          category = RoutineCategory.STUDY,
          repeatDaysMask = WeekdayMask.ALL_DAYS,
          isReminderEnabled = true,
        )

      val routines = repository.routinesFlow.first()
      assertEquals(1, routines.size)
      assertTrue(shadowAlarmManager.scheduledAlarms.isNotEmpty())

      repository.deleteRoutine(routineId)
      assertTrue(repository.routinesFlow.first().isEmpty())
      assertTrue(shadowAlarmManager.scheduledAlarms.isEmpty())

      val todoId =
        repository.saveTodo(
          title = "গুরুত্বপূর্ণ ফোন কল",
          note = "",
          priority = TodoPriority.HIGH,
          scheduledHour = 23,
          scheduledMinute = 50,
          isReminderEnabled = true,
        )
      assertEquals(1, repository.todosFlow.first().size)
      assertTrue(shadowAlarmManager.scheduledAlarms.isNotEmpty())

      repository.toggleTodoCompleted(repository.todosFlow.first().first())
      assertTrue(shadowAlarmManager.scheduledAlarms.isEmpty())

      repository.deleteTodo(todoId)
      assertTrue(repository.todosFlow.first().isEmpty())
    }
}
