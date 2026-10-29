package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ThemeMode
import com.example.i18n.AppLanguage
import com.example.ui.DinLipiMainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.DinLipiTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as DinLipiApp

    setContent {
      val viewModel: MainViewModel = viewModel(factory = MainViewModel.factory(app))
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val systemDark = isSystemInDarkTheme()
      val effectiveDark =
        when (uiState.themeMode) {
          ThemeMode.DARK -> true
          ThemeMode.LIGHT -> false
          ThemeMode.SYSTEM -> systemDark
        }

      DinLipiTheme(
        darkTheme = effectiveDark,
        isBengali = uiState.language == AppLanguage.BN,
      ) {
        DinLipiMainScreen(
          state = uiState,
          isDarkTheme = effectiveDark,
          onSelectTab = viewModel::selectTab,
          onSelectDayOfWeek = viewModel::selectDayOfWeek,
          onToggleTheme = { viewModel.toggleThemeMode(effectiveDark) },
          onToggleLanguage = viewModel::toggleLanguage,
          onSendTestNotification = viewModel::sendTestNotification,
          onOpenNewItemSheet = viewModel::openNewItemSheet,
          onOpenEditRoutine = viewModel::openEditRoutine,
          onOpenEditTodo = viewModel::openEditTodo,
          onCloseEditorSheet = viewModel::closeEditorSheet,
          onSaveRoutine = viewModel::saveRoutine,
          onToggleRoutineCompleted = viewModel::toggleRoutineCompleted,
          onToggleRoutineReminder = viewModel::toggleRoutineReminder,
          onDeleteRoutine = viewModel::deleteRoutine,
          onStartFocusForRoutine = viewModel::startFocusForRoutine,
          onLoadStarterRoutines = viewModel::loadStarterRoutines,
          onSaveTodo = viewModel::saveTodo,
          onToggleTodoCompleted = viewModel::toggleTodoCompleted,
          onToggleTodoReminder = viewModel::toggleTodoReminder,
          onDeleteTodo = viewModel::deleteTodo,
          onSelectTodoFilter = viewModel::selectTodoFilter,
          onClearCompletedTodos = viewModel::clearCompletedTodos,
          onSelectFocusPreset = viewModel::selectFocusPreset,
          onToggleFocusStartPause = viewModel::toggleFocusStartPause,
          onResetFocusTimer = viewModel::resetFocusTimer,
          onClearFocusLinkedTask = viewModel::clearFocusLinkedTask,
          onConsumeSnackbar = viewModel::consumeSnackbar,
          onPermissionsUpdated = viewModel::refreshAlarms,
        )
      }
    }
  }
}
