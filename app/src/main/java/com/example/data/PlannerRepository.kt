package com.example.data

import com.example.i18n.AppLanguage
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class PlannerRepository(
  private val dao: DinLipiDao,
  private val scheduler: ReminderScheduler,
) {
  val routinesFlow: Flow<List<RoutineEntity>> = dao.observeRoutines()
  val completionsFlow: Flow<List<RoutineCompletionEntity>> = dao.observeCompletions()
  val focusSessionsFlow: Flow<List<FocusSessionEntity>> = dao.observeFocusSessions()
  val todosFlow: Flow<List<TodoEntity>> = dao.observeTodos()

  suspend fun saveRoutine(
    id: Long = 0L,
    title: String,
    note: String,
    hour: Int,
    minute: Int,
    category: RoutineCategory,
    repeatDaysMask: Int,
    isReminderEnabled: Boolean,
  ): Long {
    val cleanTitle = title.trim()
    val cleanNote = note.trim()
    val safeMask = if (repeatDaysMask <= 0) WeekdayMask.ALL_DAYS else repeatDaysMask

    return if (id > 0L) {
      val existing = dao.getRoutineById(id)
      val updated =
        RoutineEntity(
          id = id,
          title = cleanTitle,
          note = cleanNote,
          hour = hour,
          minute = minute,
          category = category.name,
          isReminderEnabled = isReminderEnabled,
          lastCompletedDayKey = existing?.lastCompletedDayKey ?: 0,
          createdAt = existing?.createdAt ?: System.currentTimeMillis(),
          repeatDaysMask = safeMask,
        )
      dao.updateRoutine(updated)
      scheduler.scheduleRoutine(updated)
      id
    } else {
      val entity =
        RoutineEntity(
          title = cleanTitle,
          note = cleanNote,
          hour = hour,
          minute = minute,
          category = category.name,
          isReminderEnabled = isReminderEnabled,
          repeatDaysMask = safeMask,
        )
      val newId = dao.insertRoutine(entity)
      val saved = entity.copy(id = newId)
      scheduler.scheduleRoutine(saved)
      newId
    }
  }

  suspend fun toggleRoutineCompletedOnDay(routine: RoutineEntity, dayKey: Int) {
    val alreadyDone = dao.isRoutineCompletedOnDay(routine.id, dayKey) > 0
    if (alreadyDone) {
      dao.deleteCompletion(routine.id, dayKey)
      if (routine.lastCompletedDayKey == dayKey) {
        dao.updateRoutine(routine.copy(lastCompletedDayKey = 0))
      }
    } else {
      dao.insertCompletion(RoutineCompletionEntity(routineId = routine.id, dayKey = dayKey))
      dao.updateRoutine(routine.copy(lastCompletedDayKey = dayKey))
    }
  }

  suspend fun toggleRoutineReminder(routine: RoutineEntity) {
    val updated = routine.copy(isReminderEnabled = !routine.isReminderEnabled)
    dao.updateRoutine(updated)
    scheduler.scheduleRoutine(updated)
  }

  suspend fun deleteRoutine(routineId: Long) {
    scheduler.cancelRoutine(routineId)
    dao.deleteCompletionsForRoutine(routineId)
    dao.deleteRoutineById(routineId)
  }

  suspend fun recordFocusSession(taskTitle: String, durationMinutes: Int, dayKey: Int) {
    if (durationMinutes <= 0) return
    dao.insertFocusSession(
      FocusSessionEntity(
        taskTitle = taskTitle.ifBlank { "Focus" },
        durationMinutes = durationMinutes,
        dayKey = dayKey,
      )
    )
  }

  suspend fun saveTodo(
    id: Long = 0L,
    title: String,
    note: String,
    priority: TodoPriority,
    scheduledHour: Int?,
    scheduledMinute: Int?,
    isReminderEnabled: Boolean,
  ): Long {
    val cleanTitle = title.trim()
    val cleanNote = note.trim()
    val hasTime = scheduledHour != null && scheduledMinute != null
    val effectiveReminder = hasTime && isReminderEnabled

    return if (id > 0L) {
      val existing = dao.getTodoById(id)
      val updated =
        TodoEntity(
          id = id,
          title = cleanTitle,
          note = cleanNote,
          priority = priority.name,
          scheduledHour = if (hasTime) scheduledHour else null,
          scheduledMinute = if (hasTime) scheduledMinute else null,
          isReminderEnabled = effectiveReminder,
          isCompleted = existing?.isCompleted ?: false,
          createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        )
      dao.updateTodo(updated)
      scheduler.scheduleTodo(updated)
      id
    } else {
      val entity =
        TodoEntity(
          title = cleanTitle,
          note = cleanNote,
          priority = priority.name,
          scheduledHour = if (hasTime) scheduledHour else null,
          scheduledMinute = if (hasTime) scheduledMinute else null,
          isReminderEnabled = effectiveReminder,
        )
      val newId = dao.insertTodo(entity)
      val saved = entity.copy(id = newId)
      scheduler.scheduleTodo(saved)
      newId
    }
  }

  suspend fun toggleTodoCompleted(todo: TodoEntity) {
    val updated = todo.copy(isCompleted = !todo.isCompleted)
    dao.updateTodo(updated)
    scheduler.scheduleTodo(updated)
  }

  suspend fun toggleTodoReminder(todo: TodoEntity) {
    if (!todo.hasScheduledTime) return
    val updated = todo.copy(isReminderEnabled = !todo.isReminderEnabled)
    dao.updateTodo(updated)
    scheduler.scheduleTodo(updated)
  }

  suspend fun deleteTodo(todoId: Long) {
    scheduler.cancelTodo(todoId)
    dao.deleteTodoById(todoId)
  }

  suspend fun clearCompletedTodos() {
    val completed = dao.getCompletedTodos()
    completed.forEach { scheduler.cancelTodo(it.id) }
    dao.deleteCompletedTodos()
  }

  suspend fun rescheduleAllActiveReminders() {
    dao.getEnabledRoutines().forEach { scheduler.scheduleRoutine(it) }
    dao.getEnabledTimedTodos().forEach { scheduler.scheduleTodo(it) }
  }

  suspend fun loadStarterRoutines(language: AppLanguage) {
    val samples =
      if (language == AppLanguage.BN) {
        listOf(
          RoutineEntity(
            title = "সকালের হাঁটা ও ব্যায়াম",
            note = "২০ মিনিট হালকা স্ট্রেচিং ও পানি পান",
            hour = 7,
            minute = 0,
            category = RoutineCategory.MORNING.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "পড়াশোনা / মনোযোগী কাজ",
            note = "দিনের সবচেয়ে গুরুত্বপূর্ণ অধ্যায় বা কাজ",
            hour = 9,
            minute = 30,
            category = RoutineCategory.STUDY.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "দুপুরের খাবার ও বিশ্রাম",
            note = "ফোন থেকে দূরে থেকে কিছুক্ষণ বিশ্রাম",
            hour = 13,
            minute = 30,
            category = RoutineCategory.HEALTH.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "বিকালের রিভিশন ও পরিকল্পনা",
            note = "বাকি থাকা টু-ডু তালিকা মিলিয়ে নেওয়া",
            hour = 17,
            minute = 30,
            category = RoutineCategory.WORK.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "ঘুমানোর প্রস্তুতি",
            note = "আগামীকালের প্রস্তুতি ও পর্যাপ্ত ঘুম",
            hour = 22,
            minute = 30,
            category = RoutineCategory.MINDFUL.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
        )
      } else {
        listOf(
          RoutineEntity(
            title = "Morning Walk & Hydration",
            note = "20 mins light stretch and water",
            hour = 7,
            minute = 0,
            category = RoutineCategory.MORNING.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "Deep Focus Session",
            note = "Most important study or work block",
            hour = 9,
            minute = 30,
            category = RoutineCategory.STUDY.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "Mindful Lunch & Break",
            note = "Step away from screens and recharge",
            hour = 13,
            minute = 30,
            category = RoutineCategory.HEALTH.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "Evening Review",
            note = "Check pending to-dos and wrap up",
            hour = 17,
            minute = 30,
            category = RoutineCategory.WORK.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
          RoutineEntity(
            title = "Wind Down & Sleep",
            note = "Prepare for tomorrow and rest early",
            hour = 22,
            minute = 30,
            category = RoutineCategory.MINDFUL.name,
            isReminderEnabled = true,
            repeatDaysMask = WeekdayMask.ALL_DAYS,
          ),
        )
      }

    samples.forEach { sample ->
      val newId = dao.insertRoutine(sample)
      scheduler.scheduleRoutine(sample.copy(id = newId))
    }
  }
}
