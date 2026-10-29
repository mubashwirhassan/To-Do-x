package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.DinLipiDatabase
import com.example.data.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != ReminderScheduler.ACTION_TRIGGER_REMINDER) return

    val itemId = intent.getLongExtra(ReminderScheduler.EXTRA_ITEM_ID, -1L)
    if (itemId <= 0L) return

    val isRoutine = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_ROUTINE, true)
    val fallbackTitle = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: ""
    val fallbackHour = intent.getIntExtra(ReminderScheduler.EXTRA_SCHEDULED_HOUR, 0)
    val fallbackMinute = intent.getIntExtra(ReminderScheduler.EXTRA_SCHEDULED_MINUTE, 0)
    val notificationId =
      intent.getIntExtra(
        ReminderScheduler.EXTRA_NOTIFICATION_ID,
        ReminderScheduler.routineRequestCode(itemId),
      )

    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      try {
        val db = DinLipiDatabase.getInstance(context)
        val dao = db.dao()
        val prefs = PreferencesRepository(context)
        val language = prefs.getCurrentLanguage()
        val scheduler = ReminderScheduler(context)
        val nowEpochMinute = System.currentTimeMillis() / 60_000L

        if (isRoutine) {
          val routine = dao.getRoutineById(itemId)
          if (routine != null && routine.isReminderEnabled) {
            val upcomingDayKey =
              com.example.data.DayKeyUtil.todayDayKey(System.currentTimeMillis() + 60_000L)
            val alreadyCompleted = dao.isRoutineCompletedOnDay(routine.id, upcomingDayKey) > 0
            if (!alreadyCompleted) {
              NotificationHelper.postTaskReminder(
                context = context,
                notificationId = notificationId,
                dedupKey = "routine_${routine.id}",
                epochMinute = nowEpochMinute,
                isRoutine = true,
                taskTitle = routine.title,
                scheduledHour = routine.hour,
                scheduledMinute = routine.minute,
                language = language,
              )
            }
            // Schedule next active weekday cycle automatically
            scheduler.scheduleRoutineForNextCycle(routine)
          } else if (routine == null && fallbackTitle.isNotBlank()) {
            // Cancelled/deleted items should not ring
            scheduler.cancelRoutine(itemId)
          }
        } else {
          val todo = dao.getTodoById(itemId)
          if (
            todo != null &&
              !todo.isCompleted &&
              todo.isReminderEnabled &&
              todo.scheduledHour != null &&
              todo.scheduledMinute != null
          ) {
            NotificationHelper.postTaskReminder(
              context = context,
              notificationId = notificationId,
              dedupKey = "todo_${todo.id}",
              epochMinute = nowEpochMinute,
              isRoutine = false,
              taskTitle = todo.title,
              scheduledHour = todo.scheduledHour,
              scheduledMinute = todo.scheduledMinute,
              language = language,
            )
          } else if (todo == null && fallbackTitle.isNotBlank()) {
            scheduler.cancelTodo(itemId)
          }
        }
      } finally {
        pendingResult.finish()
      }
    }
  }
}
