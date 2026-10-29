package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.RoutineCategory
import com.example.data.RoutineEntity
import com.example.data.TodoEntity
import com.example.data.TodoPriority
import com.example.data.WeekdayMask
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutineEditorSheet(
  existing: RoutineEntity?,
  language: AppLanguage,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onSave: (Long, String, String, Int, Int, RoutineCategory, Int, Boolean) -> Unit,
  onDelete: (Long) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val defaultCal = remember {
    Calendar.getInstance().apply { add(Calendar.MINUTE, 10) }
  }

  var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
  var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
  var hour by remember(existing) {
    mutableIntStateOf(existing?.hour ?: defaultCal.get(Calendar.HOUR_OF_DAY))
  }
  var minute by remember(existing) {
    mutableIntStateOf(existing?.minute ?: defaultCal.get(Calendar.MINUTE))
  }
  var category by remember(existing) {
    mutableStateOf(existing?.categoryEnum ?: RoutineCategory.MORNING)
  }
  var repeatDaysMask by remember(existing) {
    mutableIntStateOf(existing?.repeatDaysMask ?: WeekdayMask.ALL_DAYS)
  }
  var isReminderEnabled by remember(existing) {
    mutableStateOf(existing?.isReminderEnabled ?: true)
  }
  var showValidationError by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }

  val (remHour, remMinute) = Localization.oneMinuteBefore(hour, minute)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .navigationBarsPadding()
          .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (existing != null) strings.editRoutineTitle else strings.newRoutineTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        if (existing != null) {
          TextButton(
            onClick = { onDelete(existing.id) },
            colors =
              ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("delete_item_button"),
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = strings.deleteButton)
          }
        }
      }

      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          if (it.isNotBlank()) showValidationError = false
        },
        label = { Text(strings.fieldTitleLabel) },
        placeholder = { Text(strings.fieldTitlePlaceholderRoutine) },
        isError = showValidationError,
        supportingText =
          if (showValidationError) {
            { Text(strings.validationTitleRequired) }
          } else {
            null
          },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("title_input"),
      )

      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text(strings.fieldNoteLabel) },
        placeholder = { Text(strings.fieldNotePlaceholder) },
        minLines = 2,
        maxLines = 3,
        modifier = Modifier.fillMaxWidth().testTag("note_input"),
      )

      // Time Selector Card + Live 1-Min-Prior Preview
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = strings.fieldTimeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = Localization.formatTime(hour, minute, language),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            OutlinedButton(
              onClick = { showTimePicker = true },
              modifier = Modifier.testTag("time_select_button"),
            ) {
              Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.changeTimeButton)
            }
          }

          if (isReminderEnabled) {
            Surface(
              color = MaterialTheme.colorScheme.tertiaryContainer,
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
              shape = RoundedCornerShape(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.NotificationsActive,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text =
                    "${strings.reminderAtPrefix} ${
                      Localization.formatTime(remHour, remMinute, language)
                    } (${strings.notifyOneMinBeforeBadge})",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }
        }
      }

      // Repeat Days of Week Selector
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings.repeatDaysLabel,
            style = MaterialTheme.typography.labelLarge,
          )
          FilterChip(
            selected = repeatDaysMask == WeekdayMask.ALL_DAYS,
            onClick = { repeatDaysMask = WeekdayMask.ALL_DAYS },
            label = { Text(strings.everyDayLabel) },
            modifier = Modifier.testTag("repeat_every_day_chip"),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          WeekdayMask.orderedCalendarDays.forEach { day ->
            val selected = WeekdayMask.isScheduledOn(repeatDaysMask, day)
            Surface(
              modifier =
                Modifier.weight(1f)
                  .minimumInteractiveComponentSize()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { repeatDaysMask = WeekdayMask.toggleDay(repeatDaysMask, day) }
                  .testTag("editor_day_toggle_$day"),
              shape = RoundedCornerShape(12.dp),
              color =
                if (selected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
              contentColor =
                if (selected) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
            ) {
              Box(
                modifier = Modifier.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = Localization.shortWeekdayLabel(day, language),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
              }
            }
          }
        }
      }

      // Category Selector
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = strings.categoryLabel,
          style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          RoutineCategory.entries.forEach { item ->
            FilterChip(
              selected = category == item,
              onClick = { category = item },
              label = { Text(item.label(strings)) },
              leadingIcon = {
                Icon(
                  imageVector = item.icon(),
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              modifier = Modifier.testTag("category_chip_${item.name.lowercase()}"),
            )
          }
        }
      }

      // Reminder Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = strings.reminderToggleLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = strings.reminderToggleSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = isReminderEnabled,
          onCheckedChange = { isReminderEnabled = it },
          modifier = Modifier.testTag("reminder_enable_switch"),
        )
      }

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f).height(50.dp).testTag("cancel_item_button"),
        ) {
          Text(strings.cancelButton)
        }
        Button(
          onClick = {
            if (title.isBlank()) {
              showValidationError = true
            } else {
              onSave(
                existing?.id ?: 0L,
                title,
                note,
                hour,
                minute,
                category,
                repeatDaysMask,
                isReminderEnabled,
              )
            }
          },
          modifier = Modifier.weight(1f).height(50.dp).testTag("save_item_button"),
        ) {
          Text(strings.saveButton)
        }
      }
    }
  }

  if (showTimePicker) {
    ClockPickerDialog(
      initialHour = hour,
      initialMinute = minute,
      strings = strings,
      onDismiss = { showTimePicker = false },
      onConfirm = { pickedHour, pickedMinute ->
        hour = pickedHour
        minute = pickedMinute
        showTimePicker = false
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TodoEditorSheet(
  existing: TodoEntity?,
  language: AppLanguage,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onSave: (Long, String, String, TodoPriority, Int?, Int?, Boolean) -> Unit,
  onDelete: (Long) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
  var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
  var priority by remember(existing) {
    mutableStateOf(existing?.priorityEnum ?: TodoPriority.NORMAL)
  }
  var scheduledHour by remember(existing) { mutableStateOf(existing?.scheduledHour) }
  var scheduledMinute by remember(existing) { mutableStateOf(existing?.scheduledMinute) }
  var isReminderEnabled by remember(existing) {
    mutableStateOf(existing?.isReminderEnabled ?: true)
  }
  var showValidationError by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .navigationBarsPadding()
          .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (existing != null) strings.editTodoTitle else strings.newTodoTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        if (existing != null) {
          TextButton(
            onClick = { onDelete(existing.id) },
            colors =
              ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("delete_item_button"),
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = strings.deleteButton)
          }
        }
      }

      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          if (it.isNotBlank()) showValidationError = false
        },
        label = { Text(strings.fieldTitleLabel) },
        placeholder = { Text(strings.fieldTitlePlaceholderTodo) },
        isError = showValidationError,
        supportingText =
          if (showValidationError) {
            { Text(strings.validationTitleRequired) }
          } else {
            null
          },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("title_input"),
      )

      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text(strings.fieldNoteLabel) },
        placeholder = { Text(strings.fieldNotePlaceholder) },
        minLines = 2,
        maxLines = 3,
        modifier = Modifier.fillMaxWidth().testTag("note_input"),
      )

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = strings.priorityLabel,
          style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TodoPriority.entries.forEach { item ->
            FilterChip(
              selected = priority == item,
              onClick = { priority = item },
              label = { Text(item.label(strings)) },
              modifier = Modifier.testTag("priority_chip_${item.name.lowercase()}"),
            )
          }
        }
      }

      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          val h = scheduledHour
          val m = scheduledMinute
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.fieldTimeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              if (h != null && m != null) {
                Text(
                  text = Localization.formatTime(h, m, language),
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                )
              } else {
                Text(
                  text = strings.optionalTimeHint,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              if (h != null && m != null) {
                TextButton(
                  onClick = {
                    scheduledHour = null
                    scheduledMinute = null
                  },
                  modifier = Modifier.testTag("clear_time_button"),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(strings.clearTimeButton)
                }
              }
              OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.testTag("time_select_button"),
              ) {
                Icon(
                  imageVector = Icons.Outlined.AccessTime,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (h != null) strings.changeTimeButton else strings.selectTimeButton)
              }
            }
          }

          if (h != null && m != null) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              val (remH, remM) = Localization.oneMinuteBefore(h, m)
              Text(
                text =
                  if (isReminderEnabled) {
                    "${strings.reminderAtPrefix} ${Localization.formatTime(remH, remM, language)}"
                  } else {
                    strings.reminderMutedDesc
                  },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold,
              )
              Switch(
                checked = isReminderEnabled,
                onCheckedChange = { isReminderEnabled = it },
                modifier = Modifier.testTag("reminder_enable_switch"),
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f).height(50.dp).testTag("cancel_item_button"),
        ) {
          Text(strings.cancelButton)
        }
        Button(
          onClick = {
            if (title.isBlank()) {
              showValidationError = true
            } else {
              onSave(
                existing?.id ?: 0L,
                title,
                note,
                priority,
                scheduledHour,
                scheduledMinute,
                isReminderEnabled,
              )
            }
          },
          modifier = Modifier.weight(1f).height(50.dp).testTag("save_item_button"),
        ) {
          Text(strings.saveButton)
        }
      }
    }
  }

  if (showTimePicker) {
    val defaultCal = remember {
      Calendar.getInstance().apply { add(Calendar.MINUTE, 10) }
    }
    ClockPickerDialog(
      initialHour = scheduledHour ?: defaultCal.get(Calendar.HOUR_OF_DAY),
      initialMinute = scheduledMinute ?: defaultCal.get(Calendar.MINUTE),
      strings = strings,
      onDismiss = { showTimePicker = false },
      onConfirm = { pickedHour, pickedMinute ->
        scheduledHour = pickedHour
        scheduledMinute = pickedMinute
        showTimePicker = false
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockPickerDialog(
  initialHour: Int,
  initialMinute: Int,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (Int, Int) -> Unit,
) {
  val pickerState =
    rememberTimePickerState(
      initialHour = initialHour,
      initialMinute = initialMinute,
      is24Hour = false,
    )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.timePickerTitle,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        TimePicker(state = pickerState, modifier = Modifier.testTag("material_time_picker"))
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(pickerState.hour, pickerState.minute) },
        modifier = Modifier.testTag("time_picker_confirm_button"),
      ) {
        Text(strings.okButton)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("time_picker_cancel_button"),
      ) {
        Text(strings.cancelButton)
      }
    },
  )
}
