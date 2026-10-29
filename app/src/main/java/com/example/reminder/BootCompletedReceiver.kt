package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.DinLipiDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: return
    if (
      action != Intent.ACTION_BOOT_COMPLETED &&
        action != Intent.ACTION_TIME_CHANGED &&
        action != Intent.ACTION_TIMEZONE_CHANGED &&
        action != "android.intent.action.QUICKBOOT_POWERON" &&
        action != "android.intent.action.TIME_SET"
    ) {
      return
    }

    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      try {
        val dao = DinLipiDatabase.getInstance(context).dao()
        val scheduler = ReminderScheduler(context)

        dao.getEnabledRoutines().forEach { routine ->
          scheduler.scheduleRoutine(routine)
        }
        dao.getEnabledTimedTodos().forEach { todo ->
          scheduler.scheduleTodo(todo)
        }
      } finally {
        pendingResult.finish()
      }
    }
  }
}
