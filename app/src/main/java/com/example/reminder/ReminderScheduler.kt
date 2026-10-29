package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.RoutineEntity
import com.example.data.TodoEntity
import com.example.data.WeekdayMask
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

  private val alarmManager: AlarmManager? =
    context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

  fun canScheduleExactAlarms(): Boolean {
    val am = alarmManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      am.canScheduleExactAlarms()
    } else {
      true
    }
  }

  fun scheduleRoutine(routine: RoutineEntity, nowMillis: Long = System.currentTimeMillis()) {
    if (!routine.isReminderEnabled) {
      cancelRoutine(routine.id)
      return
    }
    val triggerAtMillis =
      computeNextOneMinutePriorTriggerMillis(
        scheduledHour = routine.hour,
        scheduledMinute = routine.minute,
        nowMillis = nowMillis,
        repeatDaysMask = routine.repeatDaysMask,
        forceTomorrow = false,
      )
    val pi =
      buildPendingIntent(
        requestCode = routineRequestCode(routine.id),
        itemId = routine.id,
        isRoutine = true,
        title = routine.title,
        scheduledHour = routine.hour,
        scheduledMinute = routine.minute,
      )
    setAlarmSafely(triggerAtMillis, pi)
  }

  fun scheduleRoutineForNextCycle(
    routine: RoutineEntity,
    nowMillis: Long = System.currentTimeMillis(),
  ) {
    if (!routine.isReminderEnabled) {
      cancelRoutine(routine.id)
      return
    }
    val triggerAtMillis =
      computeNextOneMinutePriorTriggerMillis(
        scheduledHour = routine.hour,
        scheduledMinute = routine.minute,
        nowMillis = nowMillis,
        repeatDaysMask = routine.repeatDaysMask,
        forceTomorrow = true,
      )
    val pi =
      buildPendingIntent(
        requestCode = routineRequestCode(routine.id),
        itemId = routine.id,
        isRoutine = true,
        title = routine.title,
        scheduledHour = routine.hour,
        scheduledMinute = routine.minute,
      )
    setAlarmSafely(triggerAtMillis, pi)
  }

  fun cancelRoutine(routineId: Long) {
    cancelByRequestCode(routineRequestCode(routineId))
  }

  fun scheduleTodo(todo: TodoEntity, nowMillis: Long = System.currentTimeMillis()) {
    val hour = todo.scheduledHour
    val minute = todo.scheduledMinute
    if (todo.isCompleted || !todo.isReminderEnabled || hour == null || minute == null) {
      cancelTodo(todo.id)
      return
    }
    val triggerAtMillis =
      computeNextOneMinutePriorTriggerMillis(
        scheduledHour = hour,
        scheduledMinute = minute,
        nowMillis = nowMillis,
        repeatDaysMask = WeekdayMask.ALL_DAYS,
        forceTomorrow = false,
      )
    val pi =
      buildPendingIntent(
        requestCode = todoRequestCode(todo.id),
        itemId = todo.id,
        isRoutine = false,
        title = todo.title,
        scheduledHour = hour,
        scheduledMinute = minute,
      )
    setAlarmSafely(triggerAtMillis, pi)
  }

  fun cancelTodo(todoId: Long) {
    cancelByRequestCode(todoRequestCode(todoId))
  }

  private fun setAlarmSafely(triggerAtMillis: Long, pendingIntent: PendingIntent) {
    val am = alarmManager ?: return
    try {
      if (canScheduleExactAlarms()) {
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      } else {
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      }
    } catch (_: SecurityException) {
      am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
  }

  private fun cancelByRequestCode(requestCode: Int) {
    val am = alarmManager ?: return
    val intent =
      Intent(context, ReminderReceiver::class.java).apply {
        action = ACTION_TRIGGER_REMINDER
      }
    val pi =
      PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
      )
    if (pi != null) {
      am.cancel(pi)
      pi.cancel()
    }
  }

  private fun buildPendingIntent(
    requestCode: Int,
    itemId: Long,
    isRoutine: Boolean,
    title: String,
    scheduledHour: Int,
    scheduledMinute: Int,
  ): PendingIntent {
    val intent =
      Intent(context, ReminderReceiver::class.java).apply {
        action = ACTION_TRIGGER_REMINDER
        putExtra(EXTRA_ITEM_ID, itemId)
        putExtra(EXTRA_IS_ROUTINE, isRoutine)
        putExtra(EXTRA_TITLE, title)
        putExtra(EXTRA_SCHEDULED_HOUR, scheduledHour)
        putExtra(EXTRA_SCHEDULED_MINUTE, scheduledMinute)
        putExtra(EXTRA_NOTIFICATION_ID, requestCode)
      }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  companion object {
    const val ACTION_TRIGGER_REMINDER = "com.example.dinlipi.ACTION_TRIGGER_REMINDER"
    const val EXTRA_ITEM_ID = "extra_item_id"
    const val EXTRA_IS_ROUTINE = "extra_is_routine"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_SCHEDULED_HOUR = "extra_scheduled_hour"
    const val EXTRA_SCHEDULED_MINUTE = "extra_scheduled_minute"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    private const val ONE_MINUTE_MILLIS = 60_000L

    fun routineRequestCode(id: Long): Int = 100_000 + (id % 400_000L).toInt()

    fun todoRequestCode(id: Long): Int = 600_000 + (id % 400_000L).toInt()

    /**
     * Finds the next scheduled weekday in `repeatDaysMask` and returns the exact timestamp
     * 1 minute BEFORE `scheduledHour:scheduledMinute` on that day.
     */
    fun computeNextOneMinutePriorTriggerMillis(
      scheduledHour: Int,
      scheduledMinute: Int,
      nowMillis: Long,
      repeatDaysMask: Int = WeekdayMask.ALL_DAYS,
      forceTomorrow: Boolean = false,
    ): Long {
      for (dayOffset in 0..8) {
        if (forceTomorrow && dayOffset == 0) continue

        val taskCal =
          Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }

        if (!WeekdayMask.isScheduledOn(repeatDaysMask, taskCal.get(Calendar.DAY_OF_WEEK))) {
          continue
        }

        if (taskCal.timeInMillis <= nowMillis) {
          continue
        }

        val reminderMillis = taskCal.timeInMillis - ONE_MINUTE_MILLIS
        if (reminderMillis > nowMillis) {
          return reminderMillis
        } else if (!forceTomorrow && nowMillis < taskCal.timeInMillis) {
          // Task is scheduled today and starting in < 1 minute right now -> alert immediately!
          return nowMillis + 1_200L
        }
      }

      // Fallback 24h ahead if mask was somehow invalid
      return nowMillis + 86_400_000L
    }
  }
}
