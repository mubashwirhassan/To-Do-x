package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TodoEntity
import com.example.data.TodoFilter
import com.example.data.TodoPriority
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodoFilterBar(
  selectedFilter: TodoFilter,
  hasCompletedTodos: Boolean,
  strings: AppStrings,
  onSelectFilter: (TodoFilter) -> Unit,
  onClearCompleted: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f),
    ) {
      FilterChip(
        selected = selectedFilter == TodoFilter.ALL,
        onClick = { onSelectFilter(TodoFilter.ALL) },
        label = { Text(strings.filterAll) },
        leadingIcon =
          if (selectedFilter == TodoFilter.ALL) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else {
            null
          },
        modifier = Modifier.testTag("todo_filter_all"),
      )
      FilterChip(
        selected = selectedFilter == TodoFilter.PENDING,
        onClick = { onSelectFilter(TodoFilter.PENDING) },
        label = { Text(strings.filterPending) },
        leadingIcon =
          if (selectedFilter == TodoFilter.PENDING) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else {
            null
          },
        modifier = Modifier.testTag("todo_filter_pending"),
      )
      FilterChip(
        selected = selectedFilter == TodoFilter.COMPLETED,
        onClick = { onSelectFilter(TodoFilter.COMPLETED) },
        label = { Text(strings.filterCompleted) },
        leadingIcon =
          if (selectedFilter == TodoFilter.COMPLETED) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else {
            null
          },
        modifier = Modifier.testTag("todo_filter_completed"),
      )
    }

    if (hasCompletedTodos) {
      TextButton(
        onClick = onClearCompleted,
        modifier = Modifier.testTag("clear_completed_todos_button"),
      ) {
        Icon(
          imageVector = Icons.Outlined.DeleteSweep,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = strings.clearCompletedLabel)
      }
    }
  }
}

@Composable
fun TodoItemCard(
  todo: TodoEntity,
  language: AppLanguage,
  strings: AppStrings,
  onToggleCompleted: () -> Unit,
  onToggleReminder: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val priority = todo.priorityEnum
  val priorityColor: Color =
    when (priority) {
      TodoPriority.HIGH -> MaterialTheme.colorScheme.error
      TodoPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
      TodoPriority.NORMAL -> MaterialTheme.colorScheme.primary
    }

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onEdit)
        .testTag("todo_item_card_${todo.id}"),
    shape = MaterialTheme.shapes.medium,
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (todo.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          } else {
            MaterialTheme.colorScheme.surface
          }
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (todo.isCompleted) 0.dp else 1.5.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = todo.isCompleted,
        onCheckedChange = { onToggleCompleted() },
        modifier = Modifier.testTag("todo_checkbox_${todo.id}"),
      )

      Spacer(modifier = Modifier.width(6.dp))

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
          text = todo.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color =
            if (todo.isCompleted) {
              MaterialTheme.colorScheme.onSurfaceVariant
            } else {
              MaterialTheme.colorScheme.onSurface
            },
          textDecoration =
            if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
        )

        if (todo.note.isNotBlank()) {
          Text(
            text = todo.note,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Priority pill
          Surface(
            color = priorityColor.copy(alpha = 0.14f),
            contentColor = priorityColor,
            shape = RoundedCornerShape(8.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
              )
              Text(
                text = priority.label(strings),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }

          // Optional Scheduled Time + 1-Min Prior Reminder Pill
          val h = todo.scheduledHour
          val m = todo.scheduledMinute
          if (h != null && m != null) {
            val (remH, remM) = Localization.oneMinuteBefore(h, m)
            Surface(
              color =
                if (todo.isReminderEnabled && !todo.isCompleted) {
                  MaterialTheme.colorScheme.tertiaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
              contentColor =
                if (todo.isReminderEnabled && !todo.isCompleted) {
                  MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              shape = RoundedCornerShape(50),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.Schedule,
                  contentDescription = null,
                  modifier = Modifier.size(12.dp),
                )
                Text(
                  text = Localization.formatTime(h, m, language),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                )
                if (todo.isReminderEnabled && !todo.isCompleted) {
                  Box(
                    modifier =
                      Modifier.size(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onTertiaryContainer)
                  )
                  Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                  )
                  Text(
                    text = Localization.formatTime(remH, remM, language),
                    style = MaterialTheme.typography.labelSmall,
                  )
                }
              }
            }
          }
        }
      }

      if (todo.hasScheduledTime && !todo.isCompleted) {
        IconButton(
          onClick = onToggleReminder,
          modifier = Modifier.testTag("todo_bell_toggle_${todo.id}"),
        ) {
          Icon(
            imageVector =
              if (todo.isReminderEnabled) {
                Icons.Filled.NotificationsActive
              } else {
                Icons.Outlined.NotificationsOff
              },
            contentDescription =
              if (todo.isReminderEnabled) {
                strings.reminderActiveDesc
              } else {
                strings.reminderMutedDesc
              },
            tint =
              if (todo.isReminderEnabled) {
                MaterialTheme.colorScheme.tertiary
              } else {
                MaterialTheme.colorScheme.outline
              },
          )
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.testTag("todo_delete_${todo.id}"),
      ) {
        Icon(
          imageVector = Icons.Outlined.DeleteOutline,
          contentDescription = strings.deleteButton,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun TodoEmptyStateCard(
  strings: AppStrings,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth().testTag("todo_empty_state_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_routine_empty_1793260992308),
        contentDescription = stringResource(id = R.string.cd_empty_illustration),
        modifier = Modifier.fillMaxWidth().height(156.dp),
        contentScale = ContentScale.Crop,
      )
      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = strings.emptyTodoTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = strings.emptyTodoSubtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = onAddClick,
          modifier = Modifier.testTag("empty_add_todo_button"),
        ) {
          Icon(imageVector = Icons.Filled.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = strings.addTodoButton)
        }
      }
    }
  }
}
