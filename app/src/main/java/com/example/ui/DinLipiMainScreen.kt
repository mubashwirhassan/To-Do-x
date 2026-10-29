package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FocusPreset
import com.example.data.RoutineCategory
import com.example.data.RoutineEntity
import com.example.data.TodoEntity
import com.example.data.TodoFilter
import com.example.data.TodoPriority
import com.example.i18n.AppLanguage
import com.example.i18n.Localization
import com.example.ui.components.FocusTimerSection
import com.example.ui.components.PermissionGuidanceBanners
import com.example.ui.components.ProgressReportSection
import com.example.ui.components.RoutineEditorSheet
import com.example.ui.components.RoutineEmptyStateCard
import com.example.ui.components.RoutineTimelineItem
import com.example.ui.components.TodoEditorSheet
import com.example.ui.components.TodoEmptyStateCard
import com.example.ui.components.TodoFilterBar
import com.example.ui.components.TodoItemCard
import com.example.ui.components.UpNextHeroCard
import com.example.ui.components.WeekdaySelectorBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinLipiMainScreen(
  state: MainUiState,
  isDarkTheme: Boolean,
  onSelectTab: (ActiveTab) -> Unit,
  onSelectDayOfWeek: (Int) -> Unit,
  onToggleTheme: () -> Unit,
  onToggleLanguage: () -> Unit,
  onSendTestNotification: () -> Unit,
  onOpenNewItemSheet: () -> Unit,
  onOpenEditRoutine: (RoutineEntity) -> Unit,
  onOpenEditTodo: (TodoEntity) -> Unit,
  onCloseEditorSheet: () -> Unit,
  onSaveRoutine: (Long, String, String, Int, Int, RoutineCategory, Int, Boolean) -> Unit,
  onToggleRoutineCompleted: (RoutineEntity) -> Unit,
  onToggleRoutineReminder: (RoutineEntity) -> Unit,
  onDeleteRoutine: (Long) -> Unit,
  onStartFocusForRoutine: (RoutineEntity) -> Unit,
  onLoadStarterRoutines: () -> Unit,
  onSaveTodo: (Long, String, String, TodoPriority, Int?, Int?, Boolean) -> Unit,
  onToggleTodoCompleted: (TodoEntity) -> Unit,
  onToggleTodoReminder: (TodoEntity) -> Unit,
  onDeleteTodo: (Long) -> Unit,
  onSelectTodoFilter: (TodoFilter) -> Unit,
  onClearCompletedTodos: () -> Unit,
  onSelectFocusPreset: (FocusPreset) -> Unit,
  onToggleFocusStartPause: () -> Unit,
  onResetFocusTimer: () -> Unit,
  onClearFocusLinkedTask: () -> Unit,
  onConsumeSnackbar: () -> Unit,
  onPermissionsUpdated: () -> Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val strings = state.strings

  // Secondary tabs return to home Routine tab on system Back press
  if (state.activeTab != ActiveTab.ROUTINE) {
    BackHandler { onSelectTab(ActiveTab.ROUTINE) }
  }

  LaunchedEffect(state.snackbarMessage) {
    val msg = state.snackbarMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      onConsumeSnackbar()
    }
  }

  val pendingTodoCount = remember(state.todos) { state.todos.count { !it.isCompleted } }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isExpandedLayout = maxWidth >= 840.dp
    val showCreationFab =
      state.activeTab == ActiveTab.ROUTINE || state.activeTab == ActiveTab.TODO

    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        TopAppBar(
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.background,
              titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
          title = {
            Column {
              Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = strings.appSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          actions = {
            IconButton(
              onClick = onToggleTheme,
              modifier = Modifier.testTag("theme_toggle_button"),
            ) {
              Icon(
                imageVector =
                  if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = strings.toggleThemeDesc,
                tint = MaterialTheme.colorScheme.onSurface,
              )
            }

            IconButton(
              onClick = onSendTestNotification,
              modifier = Modifier.testTag("test_notification_button"),
            ) {
              Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = stringResource(id = R.string.cd_test_notification),
                tint = MaterialTheme.colorScheme.tertiary,
              )
            }

            Spacer(modifier = Modifier.width(2.dp))

            FilledTonalButton(
              onClick = onToggleLanguage,
              shape = RoundedCornerShape(50),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier.padding(end = 10.dp).testTag("language_toggle_button"),
            ) {
              Icon(
                imageVector = Icons.Outlined.Translate,
                contentDescription = stringResource(id = R.string.cd_toggle_language),
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = strings.langBadgeLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
            }
          },
        )
      },
      bottomBar = {
        if (!isExpandedLayout) {
          NavigationBar(modifier = Modifier.testTag("bottom_navigation_bar")) {
            NavigationBarItem(
              selected = state.activeTab == ActiveTab.ROUTINE,
              onClick = { onSelectTab(ActiveTab.ROUTINE) },
              icon = {
                Icon(
                  imageVector =
                    if (state.activeTab == ActiveTab.ROUTINE) {
                      Icons.Filled.Schedule
                    } else {
                      Icons.Outlined.Schedule
                    },
                  contentDescription = strings.tabRoutine,
                )
              },
              label = { Text(strings.tabRoutine) },
              modifier = Modifier.testTag("tab_routine"),
            )

            NavigationBarItem(
              selected = state.activeTab == ActiveTab.TODO,
              onClick = { onSelectTab(ActiveTab.TODO) },
              icon = {
                BadgedBox(
                  badge = {
                    if (pendingTodoCount > 0) {
                      Badge {
                        Text(
                          text =
                            if (state.language == AppLanguage.BN) {
                              Localization.toBengaliDigits(pendingTodoCount)
                            } else {
                              pendingTodoCount.toString()
                            }
                        )
                      }
                    }
                  }
                ) {
                  Icon(
                    imageVector =
                      if (state.activeTab == ActiveTab.TODO) {
                        Icons.Filled.CheckCircle
                      } else {
                        Icons.Outlined.CheckCircle
                      },
                    contentDescription = strings.tabTodo,
                  )
                }
              },
              label = { Text(strings.tabTodo) },
              modifier = Modifier.testTag("tab_todo"),
            )

            NavigationBarItem(
              selected = state.activeTab == ActiveTab.FOCUS,
              onClick = { onSelectTab(ActiveTab.FOCUS) },
              icon = {
                BadgedBox(
                  badge = {
                    if (state.focusTimer.isRunning) {
                      Badge(containerColor = MaterialTheme.colorScheme.tertiary)
                    }
                  }
                ) {
                  Icon(
                    imageVector =
                      if (state.activeTab == ActiveTab.FOCUS) {
                        Icons.Filled.Timer
                      } else {
                        Icons.Outlined.Timer
                      },
                    contentDescription = strings.tabFocus,
                  )
                }
              },
              label = { Text(strings.tabFocus) },
              modifier = Modifier.testTag("tab_focus"),
            )

            NavigationBarItem(
              selected = state.activeTab == ActiveTab.PROGRESS,
              onClick = { onSelectTab(ActiveTab.PROGRESS) },
              icon = {
                Icon(
                  imageVector =
                    if (state.activeTab == ActiveTab.PROGRESS) {
                      Icons.Filled.Insights
                    } else {
                      Icons.Outlined.Insights
                    },
                  contentDescription = strings.tabProgress,
                )
              },
              label = { Text(strings.tabProgress) },
              modifier = Modifier.testTag("tab_progress"),
            )
          }
        }
      },
      floatingActionButton = {
        if (showCreationFab) {
          ExtendedFloatingActionButton(
            onClick = onOpenNewItemSheet,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.cd_add_item),
              )
            },
            text = {
              Text(
                text =
                  if (state.activeTab == ActiveTab.ROUTINE) {
                    strings.addRoutineButton
                  } else {
                    strings.addTodoButton
                  },
                fontWeight = FontWeight.Bold,
              )
            },
            modifier = Modifier.testTag("add_fab_button"),
          )
        }
      },
    ) { innerPadding ->
      if (isExpandedLayout) {
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          NavigationRail(modifier = Modifier.fillMaxHeight().testTag("side_navigation_rail")) {
            Spacer(modifier = Modifier.height(16.dp))
            NavigationRailItem(
              selected = state.activeTab == ActiveTab.ROUTINE,
              onClick = { onSelectTab(ActiveTab.ROUTINE) },
              icon = {
                Icon(
                  imageVector =
                    if (state.activeTab == ActiveTab.ROUTINE) {
                      Icons.Filled.Schedule
                    } else {
                      Icons.Outlined.Schedule
                    },
                  contentDescription = strings.tabRoutine,
                )
              },
              label = { Text(strings.tabRoutine) },
              modifier = Modifier.testTag("rail_tab_routine"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            NavigationRailItem(
              selected = state.activeTab == ActiveTab.TODO,
              onClick = { onSelectTab(ActiveTab.TODO) },
              icon = {
                Icon(
                  imageVector =
                    if (state.activeTab == ActiveTab.TODO) {
                      Icons.Filled.CheckCircle
                    } else {
                      Icons.Outlined.CheckCircle
                    },
                  contentDescription = strings.tabTodo,
                )
              },
              label = { Text(strings.tabTodo) },
              modifier = Modifier.testTag("rail_tab_todo"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            NavigationRailItem(
              selected = state.activeTab == ActiveTab.FOCUS,
              onClick = { onSelectTab(ActiveTab.FOCUS) },
              icon = {
                Icon(
                  imageVector =
                    if (state.activeTab == ActiveTab.FOCUS) {
                      Icons.Filled.Timer
                    } else {
                      Icons.Outlined.Timer
                    },
                  contentDescription = strings.tabFocus,
                )
              },
              label = { Text(strings.tabFocus) },
              modifier = Modifier.testTag("rail_tab_focus"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            NavigationRailItem(
              selected = state.activeTab == ActiveTab.PROGRESS,
              onClick = { onSelectTab(ActiveTab.PROGRESS) },
              icon = {
                Icon(
                  imageVector =
                    if (state.activeTab == ActiveTab.PROGRESS) {
                      Icons.Filled.Insights
                    } else {
                      Icons.Outlined.Insights
                    },
                  contentDescription = strings.tabProgress,
                )
              },
              label = { Text(strings.tabProgress) },
              modifier = Modifier.testTag("rail_tab_progress"),
            )
          }

          Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
          ) {
            ActiveTabContent(
              state = state,
              onSelectDayOfWeek = onSelectDayOfWeek,
              onOpenNewItemSheet = onOpenNewItemSheet,
              onOpenEditRoutine = onOpenEditRoutine,
              onOpenEditTodo = onOpenEditTodo,
              onToggleRoutineCompleted = onToggleRoutineCompleted,
              onToggleRoutineReminder = onToggleRoutineReminder,
              onStartFocusForRoutine = onStartFocusForRoutine,
              onDeleteRoutine = onDeleteRoutine,
              onLoadStarterRoutines = onLoadStarterRoutines,
              onToggleTodoCompleted = onToggleTodoCompleted,
              onToggleTodoReminder = onToggleTodoReminder,
              onDeleteTodo = onDeleteTodo,
              onSelectTodoFilter = onSelectTodoFilter,
              onClearCompletedTodos = onClearCompletedTodos,
              onSelectFocusPreset = onSelectFocusPreset,
              onToggleFocusStartPause = onToggleFocusStartPause,
              onResetFocusTimer = onResetFocusTimer,
              onClearFocusLinkedTask = onClearFocusLinkedTask,
              onPermissionsUpdated = onPermissionsUpdated,
              modifier = Modifier.widthIn(max = 720.dp),
            )
          }
        }
      } else {
        Box(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentAlignment = Alignment.TopCenter,
        ) {
          ActiveTabContent(
            state = state,
            onSelectDayOfWeek = onSelectDayOfWeek,
            onOpenNewItemSheet = onOpenNewItemSheet,
            onOpenEditRoutine = onOpenEditRoutine,
            onOpenEditTodo = onOpenEditTodo,
            onToggleRoutineCompleted = onToggleRoutineCompleted,
            onToggleRoutineReminder = onToggleRoutineReminder,
            onStartFocusForRoutine = onStartFocusForRoutine,
            onDeleteRoutine = onDeleteRoutine,
            onLoadStarterRoutines = onLoadStarterRoutines,
            onToggleTodoCompleted = onToggleTodoCompleted,
            onToggleTodoReminder = onToggleTodoReminder,
            onDeleteTodo = onDeleteTodo,
            onSelectTodoFilter = onSelectTodoFilter,
            onClearCompletedTodos = onClearCompletedTodos,
            onSelectFocusPreset = onSelectFocusPreset,
            onToggleFocusStartPause = onToggleFocusStartPause,
            onResetFocusTimer = onResetFocusTimer,
            onClearFocusLinkedTask = onClearFocusLinkedTask,
            onPermissionsUpdated = onPermissionsUpdated,
            modifier = Modifier.widthIn(max = 600.dp),
          )
        }
      }
    }
  }

  when (val editor = state.editorSheet) {
    EditorSheetState.Hidden -> Unit
    is EditorSheetState.RoutineEditor -> {
      RoutineEditorSheet(
        existing = editor.existing,
        language = state.language,
        strings = strings,
        onDismiss = onCloseEditorSheet,
        onSave = onSaveRoutine,
        onDelete = onDeleteRoutine,
      )
    }
    is EditorSheetState.TodoEditor -> {
      TodoEditorSheet(
        existing = editor.existing,
        language = state.language,
        strings = strings,
        onDismiss = onCloseEditorSheet,
        onSave = onSaveTodo,
        onDelete = onDeleteTodo,
      )
    }
  }
}

@Composable
private fun ActiveTabContent(
  state: MainUiState,
  onSelectDayOfWeek: (Int) -> Unit,
  onOpenNewItemSheet: () -> Unit,
  onOpenEditRoutine: (RoutineEntity) -> Unit,
  onOpenEditTodo: (TodoEntity) -> Unit,
  onToggleRoutineCompleted: (RoutineEntity) -> Unit,
  onToggleRoutineReminder: (RoutineEntity) -> Unit,
  onStartFocusForRoutine: (RoutineEntity) -> Unit,
  onDeleteRoutine: (Long) -> Unit,
  onLoadStarterRoutines: () -> Unit,
  onToggleTodoCompleted: (TodoEntity) -> Unit,
  onToggleTodoReminder: (TodoEntity) -> Unit,
  onDeleteTodo: (Long) -> Unit,
  onSelectTodoFilter: (TodoFilter) -> Unit,
  onClearCompletedTodos: () -> Unit,
  onSelectFocusPreset: (FocusPreset) -> Unit,
  onToggleFocusStartPause: () -> Unit,
  onResetFocusTimer: () -> Unit,
  onClearFocusLinkedTask: () -> Unit,
  onPermissionsUpdated: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = state.strings
  val isViewingToday = state.selectedDayOfWeek == state.todayDayOfWeek

  when (state.activeTab) {
    ActiveTab.ROUTINE -> {
      val dayRoutines = state.routinesForSelectedDay
      LazyColumn(
        modifier = modifier.fillMaxSize().testTag("routine_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item(key = "permissions") {
          PermissionGuidanceBanners(
            strings = strings,
            onPermissionsUpdated = onPermissionsUpdated,
          )
        }

        item(key = "hero") {
          UpNextHeroCard(
            upcoming = state.upcomingRoutine,
            totalRoutines = state.routinesForToday.size,
            completedToday = state.completedRoutinesToday,
            language = state.language,
            strings = strings,
          )
        }

        item(key = "weekday_selector") {
          WeekdaySelectorBar(
            selectedDayOfWeek = state.selectedDayOfWeek,
            todayDayOfWeek = state.todayDayOfWeek,
            language = state.language,
            strings = strings,
            onSelectDay = onSelectDayOfWeek,
          )
        }

        if (dayRoutines.isEmpty()) {
          item(key = "empty_routine") {
            RoutineEmptyStateCard(
              hasAnyRoutinesInApp = state.routines.isNotEmpty(),
              strings = strings,
              onAddClick = onOpenNewItemSheet,
              onLoadStarterClick = onLoadStarterRoutines,
            )
          }
        } else {
          itemsIndexed(
            items = dayRoutines,
            key = { _, item -> "routine_${item.id}" },
          ) { index, routine ->
            RoutineTimelineItem(
              routine = routine,
              isViewingToday = isViewingToday,
              isCompletedToday = routine.id in state.completedRoutineIdsToday,
              isNextUpcoming = state.upcomingRoutine?.routine?.id == routine.id,
              isLastItem = index == dayRoutines.lastIndex,
              language = state.language,
              strings = strings,
              onToggleCompleted = { onToggleRoutineCompleted(routine) },
              onToggleReminder = { onToggleRoutineReminder(routine) },
              onStartFocus = { onStartFocusForRoutine(routine) },
              onEdit = { onOpenEditRoutine(routine) },
              onDelete = { onDeleteRoutine(routine.id) },
            )
          }
        }
      }
    }

    ActiveTab.TODO -> {
      LazyColumn(
        modifier = modifier.fillMaxSize().testTag("todo_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item(key = "permissions_todo") {
          PermissionGuidanceBanners(
            strings = strings,
            onPermissionsUpdated = onPermissionsUpdated,
          )
        }

        item(key = "todo_filters") {
          TodoFilterBar(
            selectedFilter = state.todoFilter,
            hasCompletedTodos = state.hasCompletedTodos,
            strings = strings,
            onSelectFilter = onSelectTodoFilter,
            onClearCompleted = onClearCompletedTodos,
          )
        }

        if (state.filteredTodos.isEmpty()) {
          item(key = "empty_todo") {
            TodoEmptyStateCard(
              strings = strings,
              onAddClick = onOpenNewItemSheet,
            )
          }
        } else {
          items(
            items = state.filteredTodos,
            key = { item -> "todo_${item.id}" },
          ) { todo ->
            TodoItemCard(
              todo = todo,
              language = state.language,
              strings = strings,
              onToggleCompleted = { onToggleTodoCompleted(todo) },
              onToggleReminder = { onToggleTodoReminder(todo) },
              onEdit = { onOpenEditTodo(todo) },
              onDelete = { onDeleteTodo(todo.id) },
            )
          }
        }
      }
    }

    ActiveTab.FOCUS -> {
      FocusTimerSection(
        focusState = state.focusTimer,
        progressSummary = state.progressSummary,
        language = state.language,
        strings = strings,
        onSelectPreset = onSelectFocusPreset,
        onToggleStartPause = onToggleFocusStartPause,
        onReset = onResetFocusTimer,
        onClearLinkedTask = onClearFocusLinkedTask,
        modifier = modifier,
      )
    }

    ActiveTab.PROGRESS -> {
      ProgressReportSection(
        summary = state.progressSummary,
        totalRoutines = state.routines.size,
        language = state.language,
        strings = strings,
        modifier = modifier,
      )
    }
  }
}
