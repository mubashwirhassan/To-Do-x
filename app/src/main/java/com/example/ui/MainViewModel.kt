package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DinLipiApp
import com.example.data.DayKeyUtil
import com.example.data.FocusPreset
import com.example.data.FocusSessionEntity
import com.example.data.PlannerRepository
import com.example.data.PreferencesRepository
import com.example.data.RoutineCategory
import com.example.data.RoutineCompletionEntity
import com.example.data.RoutineEntity
import com.example.data.ThemeMode
import com.example.data.TodoEntity
import com.example.data.TodoFilter
import com.example.data.TodoPriority
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization
import com.example.reminder.NotificationHelper
import com.example.reminder.ReminderScheduler
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ActiveTab {
  ROUTINE,
  TODO,
  FOCUS,
  PROGRESS
}

data class UpcomingRoutineInfo(
  val routine: RoutineEntity,
  val remainingSeconds: Long,
  val reminderHour: Int,
  val reminderMinute: Int,
)

data class FocusTimerState(
  val preset: FocusPreset = FocusPreset.FOCUS_25,
  val linkedTaskTitle: String? = null,
  val isRunning: Boolean = false,
  val totalSeconds: Int = FocusPreset.FOCUS_25.minutes * 60,
  val remainingSeconds: Int = FocusPreset.FOCUS_25.minutes * 60,
  val targetEndMillis: Long = 0L,
) {
  val progress: Float
    get() =
      if (totalSeconds <= 0) 0f
      else ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

  val hasStarted: Boolean
    get() = remainingSeconds < totalSeconds
}

sealed interface EditorSheetState {
  data object Hidden : EditorSheetState

  data class RoutineEditor(
    val existing: RoutineEntity? = null,
    val initialDayOfWeek: Int = Calendar.SUNDAY,
  ) : EditorSheetState

  data class TodoEditor(val existing: TodoEntity? = null) : EditorSheetState
}

data class MainUiState(
  val language: AppLanguage = AppLanguage.BN,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val strings: AppStrings = Localization.Bengali,
  val activeTab: ActiveTab = ActiveTab.ROUTINE,
  val selectedDayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
  val todayDayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
  val routines: List<RoutineEntity> = emptyList(),
  val completedRoutineIdsToday: Set<Long> = emptySet(),
  val todos: List<TodoEntity> = emptyList(),
  val todoFilter: TodoFilter = TodoFilter.ALL,
  val todayDayKey: Int = DayKeyUtil.todayDayKey(),
  val nowMillis: Long = System.currentTimeMillis(),
  val upcomingRoutine: UpcomingRoutineInfo? = null,
  val focusTimer: FocusTimerState = FocusTimerState(),
  val progressSummary: ProgressSummary = ProgressSummary(),
  val editorSheet: EditorSheetState = EditorSheetState.Hidden,
  val snackbarMessage: String? = null,
) {
  val routinesForSelectedDay: List<RoutineEntity>
    get() = routines.filter { it.isScheduledOnDayOfWeek(selectedDayOfWeek) }

  val routinesForToday: List<RoutineEntity>
    get() = routines.filter { it.isScheduledOnDayOfWeek(todayDayOfWeek) }

  val completedRoutinesToday: Int
    get() = routinesForToday.count { it.id in completedRoutineIdsToday }

  val filteredTodos: List<TodoEntity>
    get() =
      when (todoFilter) {
        TodoFilter.ALL -> todos
        TodoFilter.PENDING -> todos.filter { !it.isCompleted }
        TodoFilter.COMPLETED -> todos.filter { it.isCompleted }
      }

  val hasCompletedTodos: Boolean
    get() = todos.any { it.isCompleted }
}

class MainViewModel(
  private val appContext: Context,
  private val plannerRepository: PlannerRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

  private val activeTabFlow = MutableStateFlow(ActiveTab.ROUTINE)
  private val selectedDayOfWeekFlow =
    MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
  private val todoFilterFlow = MutableStateFlow(TodoFilter.ALL)
  private val focusTimerFlow = MutableStateFlow(FocusTimerState())
  private val editorSheetFlow = MutableStateFlow<EditorSheetState>(EditorSheetState.Hidden)
  private val snackbarMessageFlow = MutableStateFlow<String?>(null)
  private val nowMillisFlow = MutableStateFlow(System.currentTimeMillis())

  private val persistenceFlow =
    combine(
      preferencesRepository.languageFlow,
      preferencesRepository.themeModeFlow,
      plannerRepository.routinesFlow,
      plannerRepository.completionsFlow,
      plannerRepository.focusSessionsFlow,
    ) { language, themeMode, routines, completions, focusSessions ->
      PersistenceSnapshot(language, themeMode, routines, completions, focusSessions)
    }

  private val uiControlFlow =
    combine(
      plannerRepository.todosFlow,
      activeTabFlow,
      selectedDayOfWeekFlow,
      todoFilterFlow,
      focusTimerFlow,
    ) { todos, activeTab, selectedDay, todoFilter, focusTimer ->
      UiControlSnapshot(todos, activeTab, selectedDay, todoFilter, focusTimer)
    }

  val uiState: StateFlow<MainUiState> =
    combine(
        persistenceFlow,
        uiControlFlow,
        nowMillisFlow,
        editorSheetFlow,
        snackbarMessageFlow,
      ) { persisted, controls, nowMillis, editorSheet, snackbar ->
        val todayCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val todayKey = DayKeyUtil.dayKeyFromCalendar(todayCal)
        val todayDow = todayCal.get(Calendar.DAY_OF_WEEK)
        val strings = Localization.forLanguage(persisted.language)

        val completedTodayIds =
          persisted.completions
            .filter { it.dayKey == todayKey }
            .map { it.routineId }
            .toSet()

        val upcoming =
          computeNextUpcomingRoutine(
            routines = persisted.routines,
            completedTodayIds = completedTodayIds,
            nowMillis = nowMillis,
          )

        val summary =
          StatsCalculator.computeSummary(
            routines = persisted.routines,
            completions = persisted.completions,
            focusSessions = persisted.focusSessions,
            nowMillis = nowMillis,
          )

        MainUiState(
          language = persisted.language,
          themeMode = persisted.themeMode,
          strings = strings,
          activeTab = controls.activeTab,
          selectedDayOfWeek = controls.selectedDayOfWeek,
          todayDayOfWeek = todayDow,
          routines = persisted.routines,
          completedRoutineIdsToday = completedTodayIds,
          todos = controls.todos,
          todoFilter = controls.todoFilter,
          todayDayKey = todayKey,
          nowMillis = nowMillis,
          upcomingRoutine = upcoming,
          focusTimer = controls.focusTimer,
          progressSummary = summary,
          editorSheet = editorSheet,
          snackbarMessage = snackbar,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
      )

  init {
    viewModelScope.launch { plannerRepository.rescheduleAllActiveReminders() }

    viewModelScope.launch {
      while (isActive) {
        val now = System.currentTimeMillis()
        nowMillisFlow.value = now
        tickFocusTimer(now)
        checkForegroundOneMinutePriorAlerts(now)
        delay(1_000L)
      }
    }
  }

  private suspend fun tickFocusTimer(nowMillis: Long) {
    val current = focusTimerFlow.value
    if (!current.isRunning) return

    val remaining = ((current.targetEndMillis - nowMillis + 999L) / 1000L).toInt()
    if (remaining <= 0) {
      val state = uiState.value
      val strings = state.strings
      val taskName = current.linkedTaskTitle ?: strings.focusNoLinkedTask
      val completedMinutes = current.preset.minutes

      focusTimerFlow.value =
        current.copy(
          isRunning = false,
          remainingSeconds = current.totalSeconds,
          targetEndMillis = 0L,
        )

      if (!current.preset.isBreak) {
        plannerRepository.recordFocusSession(
          taskTitle = taskName,
          durationMinutes = completedMinutes,
          dayKey = DayKeyUtil.todayDayKey(nowMillis),
        )
      }

      NotificationHelper.postFocusCompleteNotification(
        context = appContext,
        taskTitle = taskName,
        durationMinutes = completedMinutes,
        language = state.language,
      )
      snackbarMessageFlow.value = strings.focusCompletedToast
    } else if (remaining != current.remainingSeconds) {
      focusTimerFlow.value = current.copy(remainingSeconds = remaining)
    }
  }

  private fun checkForegroundOneMinutePriorAlerts(nowMillis: Long) {
    val state = uiState.value
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val sec = cal.get(Calendar.SECOND)
    if (sec > 5) return

    val currentHour = cal.get(Calendar.HOUR_OF_DAY)
    val currentMinute = cal.get(Calendar.MINUTE)
    val epochMinute = nowMillis / 60_000L

    // The task starts 1 minute AFTER nowMillis, so evaluate weekday of (nowMillis + 60s)
    val taskMomentCal = Calendar.getInstance().apply { timeInMillis = nowMillis + 60_000L }
    val taskDow = taskMomentCal.get(Calendar.DAY_OF_WEEK)

    state.routines.forEach { routine ->
      val isDoneToday =
        taskDow == state.todayDayOfWeek && routine.id in state.completedRoutineIdsToday
      if (
        routine.isReminderEnabled &&
          routine.isScheduledOnDayOfWeek(taskDow) &&
          !isDoneToday
      ) {
        val (remHour, remMinute) = Localization.oneMinuteBefore(routine.hour, routine.minute)
        if (currentHour == remHour && currentMinute == remMinute) {
          NotificationHelper.postTaskReminder(
            context = appContext,
            notificationId = ReminderScheduler.routineRequestCode(routine.id),
            dedupKey = "routine_${routine.id}",
            epochMinute = epochMinute,
            isRoutine = true,
            taskTitle = routine.title,
            scheduledHour = routine.hour,
            scheduledMinute = routine.minute,
            language = state.language,
          )
        }
      }
    }

    state.todos.forEach { todo ->
      val h = todo.scheduledHour
      val m = todo.scheduledMinute
      if (!todo.isCompleted && todo.isReminderEnabled && h != null && m != null) {
        val (remHour, remMinute) = Localization.oneMinuteBefore(h, m)
        if (currentHour == remHour && currentMinute == remMinute) {
          NotificationHelper.postTaskReminder(
            context = appContext,
            notificationId = ReminderScheduler.todoRequestCode(todo.id),
            dedupKey = "todo_${todo.id}",
            epochMinute = epochMinute,
            isRoutine = false,
            taskTitle = todo.title,
            scheduledHour = h,
            scheduledMinute = m,
            language = state.language,
          )
        }
      }
    }
  }

  private fun computeNextUpcomingRoutine(
    routines: List<RoutineEntity>,
    completedTodayIds: Set<Long>,
    nowMillis: Long,
  ): UpcomingRoutineInfo? {
    if (routines.isEmpty()) return null

    var bestRoutine: RoutineEntity? = null
    var bestDeltaMillis = Long.MAX_VALUE

    for (routine in routines) {
      for (dayOffset in 0..7) {
        if (dayOffset == 0 && routine.id in completedTodayIds) continue
        val targetCal =
          Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, routine.hour)
            set(Calendar.MINUTE, routine.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
        if (!routine.isScheduledOnDayOfWeek(targetCal.get(Calendar.DAY_OF_WEEK))) continue
        if (targetCal.timeInMillis <= nowMillis) continue

        val delta = targetCal.timeInMillis - nowMillis
        if (delta < bestDeltaMillis) {
          bestDeltaMillis = delta
          bestRoutine = routine
        }
        break
      }
    }

    val chosen = bestRoutine ?: return null
    val (remH, remM) = Localization.oneMinuteBefore(chosen.hour, chosen.minute)
    return UpcomingRoutineInfo(
      routine = chosen,
      remainingSeconds = (bestDeltaMillis / 1000L).coerceAtLeast(0L),
      reminderHour = remH,
      reminderMinute = remM,
    )
  }

  fun selectTab(tab: ActiveTab) {
    activeTabFlow.value = tab
  }

  fun selectDayOfWeek(dayOfWeek: Int) {
    selectedDayOfWeekFlow.value = dayOfWeek
  }

  fun selectTodoFilter(filter: TodoFilter) {
    todoFilterFlow.value = filter
  }

  fun toggleLanguage() {
    viewModelScope.launch { preferencesRepository.toggleLanguage() }
  }

  fun toggleThemeMode(currentlyDark: Boolean) {
    viewModelScope.launch { preferencesRepository.toggleThemeMode(currentlyDark) }
  }

  // Focus Timer Controls
  fun selectFocusPreset(preset: FocusPreset) {
    val totalSecs = preset.minutes * 60
    focusTimerFlow.update { current ->
      current.copy(
        preset = preset,
        isRunning = false,
        totalSeconds = totalSecs,
        remainingSeconds = totalSecs,
        targetEndMillis = 0L,
      )
    }
  }

  fun toggleFocusStartPause() {
    val now = System.currentTimeMillis()
    focusTimerFlow.update { current ->
      if (current.isRunning) {
        val left =
          ((current.targetEndMillis - now + 999L) / 1000L)
            .toInt()
            .coerceIn(1, current.totalSeconds)
        current.copy(isRunning = false, remainingSeconds = left, targetEndMillis = 0L)
      } else {
        val secs = if (current.remainingSeconds <= 0) current.totalSeconds else current.remainingSeconds
        current.copy(
          isRunning = true,
          remainingSeconds = secs,
          targetEndMillis = now + secs * 1000L,
        )
      }
    }
  }

  fun resetFocusTimer() {
    focusTimerFlow.update { current ->
      current.copy(
        isRunning = false,
        remainingSeconds = current.totalSeconds,
        targetEndMillis = 0L,
      )
    }
  }

  fun startFocusForRoutine(routine: RoutineEntity) {
    val preset = FocusPreset.FOCUS_25
    val secs = preset.minutes * 60
    val now = System.currentTimeMillis()
    focusTimerFlow.value =
      FocusTimerState(
        preset = preset,
        linkedTaskTitle = routine.title,
        isRunning = true,
        totalSeconds = secs,
        remainingSeconds = secs,
        targetEndMillis = now + secs * 1000L,
      )
    activeTabFlow.value = ActiveTab.FOCUS
  }

  fun clearFocusLinkedTask() {
    focusTimerFlow.update { it.copy(linkedTaskTitle = null) }
  }

  // Sheet & CRUD Controls
  fun openNewItemSheet() {
    editorSheetFlow.value =
      when (activeTabFlow.value) {
        ActiveTab.TODO -> EditorSheetState.TodoEditor(null)
        else ->
          EditorSheetState.RoutineEditor(
            existing = null,
            initialDayOfWeek = selectedDayOfWeekFlow.value,
          )
      }
  }

  fun openEditRoutine(routine: RoutineEntity) {
    editorSheetFlow.value =
      EditorSheetState.RoutineEditor(
        existing = routine,
        initialDayOfWeek = selectedDayOfWeekFlow.value,
      )
  }

  fun openEditTodo(todo: TodoEntity) {
    editorSheetFlow.value = EditorSheetState.TodoEditor(todo)
  }

  fun closeEditorSheet() {
    editorSheetFlow.value = EditorSheetState.Hidden
  }

  fun saveRoutine(
    id: Long,
    title: String,
    note: String,
    hour: Int,
    minute: Int,
    category: RoutineCategory,
    repeatDaysMask: Int,
    isReminderEnabled: Boolean,
  ) {
    viewModelScope.launch {
      plannerRepository.saveRoutine(
        id = id,
        title = title,
        note = note,
        hour = hour,
        minute = minute,
        category = category,
        repeatDaysMask = repeatDaysMask,
        isReminderEnabled = isReminderEnabled,
      )
      editorSheetFlow.value = EditorSheetState.Hidden
    }
  }

  fun toggleRoutineCompleted(routine: RoutineEntity) {
    viewModelScope.launch {
      plannerRepository.toggleRoutineCompletedOnDay(routine, DayKeyUtil.todayDayKey())
    }
  }

  fun toggleRoutineReminder(routine: RoutineEntity) {
    viewModelScope.launch { plannerRepository.toggleRoutineReminder(routine) }
  }

  fun deleteRoutine(routineId: Long) {
    viewModelScope.launch {
      plannerRepository.deleteRoutine(routineId)
      if (editorSheetFlow.value is EditorSheetState.RoutineEditor) {
        editorSheetFlow.value = EditorSheetState.Hidden
      }
    }
  }

  fun saveTodo(
    id: Long,
    title: String,
    note: String,
    priority: TodoPriority,
    scheduledHour: Int?,
    scheduledMinute: Int?,
    isReminderEnabled: Boolean,
  ) {
    viewModelScope.launch {
      plannerRepository.saveTodo(
        id = id,
        title = title,
        note = note,
        priority = priority,
        scheduledHour = scheduledHour,
        scheduledMinute = scheduledMinute,
        isReminderEnabled = isReminderEnabled,
      )
      editorSheetFlow.value = EditorSheetState.Hidden
    }
  }

  fun toggleTodoCompleted(todo: TodoEntity) {
    viewModelScope.launch { plannerRepository.toggleTodoCompleted(todo) }
  }

  fun toggleTodoReminder(todo: TodoEntity) {
    viewModelScope.launch { plannerRepository.toggleTodoReminder(todo) }
  }

  fun deleteTodo(todoId: Long) {
    viewModelScope.launch {
      plannerRepository.deleteTodo(todoId)
      if (editorSheetFlow.value is EditorSheetState.TodoEditor) {
        editorSheetFlow.value = EditorSheetState.Hidden
      }
    }
  }

  fun clearCompletedTodos() {
    viewModelScope.launch { plannerRepository.clearCompletedTodos() }
  }

  fun loadStarterRoutines() {
    viewModelScope.launch {
      plannerRepository.loadStarterRoutines(uiState.value.language)
    }
  }

  fun sendTestNotification() {
    val language = uiState.value.language
    val strings = uiState.value.strings
    NotificationHelper.postTestNotification(appContext, language)
    snackbarMessageFlow.update { strings.testNotificationSentToast }
  }

  fun consumeSnackbar() {
    snackbarMessageFlow.value = null
  }

  fun refreshAlarms() {
    viewModelScope.launch { plannerRepository.rescheduleAllActiveReminders() }
  }

  private data class PersistenceSnapshot(
    val language: AppLanguage,
    val themeMode: ThemeMode,
    val routines: List<RoutineEntity>,
    val completions: List<RoutineCompletionEntity>,
    val focusSessions: List<FocusSessionEntity>,
  )

  private data class UiControlSnapshot(
    val todos: List<TodoEntity>,
    val activeTab: ActiveTab,
    val selectedDayOfWeek: Int,
    val todoFilter: TodoFilter,
    val focusTimer: FocusTimerState,
  )

  companion object {
    fun factory(app: DinLipiApp): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return MainViewModel(
            appContext = app.applicationContext,
            plannerRepository = app.plannerRepository,
            preferencesRepository = app.preferencesRepository,
          ) as T
        }
      }
  }
}
