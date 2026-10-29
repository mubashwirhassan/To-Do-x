package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.RoutineCategory
import com.example.data.RoutineEntity
import com.example.data.WeekdayMask
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization

fun RoutineCategory.icon(): ImageVector =
  when (this) {
    RoutineCategory.MORNING -> Icons.Outlined.WbSunny
    RoutineCategory.STUDY -> Icons.Outlined.MenuBook
    RoutineCategory.WORK -> Icons.Outlined.WorkOutline
    RoutineCategory.HEALTH -> Icons.Outlined.FavoriteBorder
    RoutineCategory.MINDFUL -> Icons.Outlined.SelfImprovement
    RoutineCategory.PERSONAL -> Icons.Outlined.PersonOutline
  }

@Composable
fun WeekdaySelectorBar(
  selectedDayOfWeek: Int,
  todayDayOfWeek: Int,
  language: AppLanguage,
  strings: AppStrings,
  onSelectDay: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().testTag("weekday_selector_bar"),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    WeekdayMask.orderedCalendarDays.forEach { dayOfWeek ->
      val isSelected = dayOfWeek == selectedDayOfWeek
      val isToday = dayOfWeek == todayDayOfWeek

      Surface(
        modifier =
          Modifier.weight(1f)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelectDay(dayOfWeek) }
            .testTag("weekday_filter_$dayOfWeek"),
        shape = RoundedCornerShape(14.dp),
        color =
          when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
          },
        contentColor =
          when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
          },
      ) {
        Column(
          modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = Localization.shortWeekdayLabel(dayOfWeek, language),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
          )
          Box(
            modifier =
              Modifier.size(5.dp)
                .clip(CircleShape)
                .background(
                  when {
                    isToday && isSelected -> MaterialTheme.colorScheme.tertiaryContainer
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> androidx.compose.ui.graphics.Color.Transparent
                  }
                )
          )
        }
      }
    }
  }
}

@Composable
fun RoutineTimelineItem(
  routine: RoutineEntity,
  isViewingToday: Boolean,
  isCompletedToday: Boolean,
  isNextUpcoming: Boolean,
  isLastItem: Boolean,
  language: AppLanguage,
  strings: AppStrings,
  onToggleCompleted: () -> Unit,
  onToggleReminder: () -> Unit,
  onStartFocus: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val (remHour, remMin) = Localization.oneMinuteBefore(routine.hour, routine.minute)
  val cardContainerColor by
    animateColorAsState(
      targetValue =
        when {
          isViewingToday && isCompletedToday ->
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
          isViewingToday && isNextUpcoming ->
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
          else -> MaterialTheme.colorScheme.surface
        },
      label = "routine_card_bg",
    )

  Row(
    modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Left Timeline Rail & Interactive Check Node
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.width(48.dp).fillMaxHeight(),
    ) {
      Box(
        modifier =
          Modifier.minimumInteractiveComponentSize()
            .size(36.dp)
            .clip(CircleShape)
            .background(
              when {
                isViewingToday && isCompletedToday -> MaterialTheme.colorScheme.primary
                isViewingToday && isNextUpcoming -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
              }
            )
            .then(
              if (isViewingToday) Modifier.clickable(onClick = onToggleCompleted) else Modifier
            )
            .testTag("routine_check_${routine.id}"),
        contentAlignment = Alignment.Center,
      ) {
        if (isViewingToday && isCompletedToday) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = strings.filterCompleted,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp),
          )
        } else {
          Box(
            modifier =
              Modifier.size(12.dp)
                .clip(CircleShape)
                .background(
                  if (isViewingToday && isNextUpcoming) {
                    MaterialTheme.colorScheme.tertiary
                  } else {
                    MaterialTheme.colorScheme.outline
                  }
                )
          )
        }
      }

      if (!isLastItem) {
        Box(
          modifier =
            Modifier.width(2.dp)
              .weight(1f)
              .background(
                if (isViewingToday && isCompletedToday) {
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                  MaterialTheme.colorScheme.outlineVariant
                }
              )
        )
      }
    }

    // Main Routine Card
    Card(
      modifier =
        Modifier.weight(1f)
          .padding(bottom = 14.dp)
          .clip(MaterialTheme.shapes.medium)
          .clickable(onClick = onEdit)
          .testTag("routine_item_card_${routine.id}"),
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = cardContainerColor),
      elevation =
        CardDefaults.cardElevation(
          defaultElevation = if (isViewingToday && isCompletedToday) 0.dp else 1.5.dp
        ),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = Localization.formatTime(routine.hour, routine.minute, language),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color =
                if (isViewingToday && isCompletedToday) {
                  MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                  MaterialTheme.colorScheme.primary
                },
            )

            Surface(
              color =
                if (routine.isReminderEnabled) {
                  MaterialTheme.colorScheme.tertiaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
              contentColor =
                if (routine.isReminderEnabled) {
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
                  imageVector =
                    if (routine.isReminderEnabled) {
                      Icons.Filled.NotificationsActive
                    } else {
                      Icons.Outlined.NotificationsOff
                    },
                  contentDescription = null,
                  modifier = Modifier.size(12.dp),
                )
                Text(
                  text =
                    if (routine.isReminderEnabled) {
                      Localization.formatTime(remHour, remMin, language)
                    } else {
                      strings.reminderMutedDesc
                    },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }

          val cat = routine.categoryEnum
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(8.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = cat.icon(),
                contentDescription = null,
                modifier = Modifier.size(13.dp),
              )
              Text(
                text = cat.label(strings),
                style = MaterialTheme.typography.labelSmall,
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = routine.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color =
                if (isViewingToday && isCompletedToday) {
                  MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
              textDecoration =
                if (isViewingToday && isCompletedToday) {
                  TextDecoration.LineThrough
                } else {
                  TextDecoration.None
                },
            )
            if (routine.note.isNotBlank()) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = routine.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
            if ((routine.repeatDaysMask and WeekdayMask.ALL_DAYS) != WeekdayMask.ALL_DAYS) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.EventRepeat,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(13.dp),
                )
                Text(
                  text = WeekdayMask.summaryLabel(routine.repeatDaysMask, language, strings),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
            }
          }

          IconButton(
            onClick = onStartFocus,
            modifier = Modifier.testTag("routine_focus_start_${routine.id}"),
          ) {
            Icon(
              imageVector = Icons.Outlined.PlayCircleOutline,
              contentDescription = strings.startFocusForRoutineDesc,
              tint = MaterialTheme.colorScheme.primary,
            )
          }

          IconButton(
            onClick = onToggleReminder,
            modifier = Modifier.testTag("routine_bell_toggle_${routine.id}"),
          ) {
            Icon(
              imageVector =
                if (routine.isReminderEnabled) {
                  Icons.Filled.NotificationsActive
                } else {
                  Icons.Outlined.NotificationsOff
                },
              contentDescription =
                if (routine.isReminderEnabled) {
                  strings.reminderActiveDesc
                } else {
                  strings.reminderMutedDesc
                },
              tint =
                if (routine.isReminderEnabled) {
                  MaterialTheme.colorScheme.tertiary
                } else {
                  MaterialTheme.colorScheme.outline
                },
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("routine_delete_${routine.id}"),
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
  }
}

@Composable
fun RoutineEmptyStateCard(
  hasAnyRoutinesInApp: Boolean,
  strings: AppStrings,
  onAddClick: () -> Unit,
  onLoadStarterClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth().testTag("routine_empty_state_card"),
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
        modifier = Modifier.fillMaxWidth().height(168.dp),
        contentScale = ContentScale.Crop,
      )
      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text =
            if (hasAnyRoutinesInApp) {
              strings.noRoutineOnSelectedDayTitle
            } else {
              strings.noRoutineYetTitle
            },
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text =
            if (hasAnyRoutinesInApp) {
              strings.noRoutineOnSelectedDaySubtitle
            } else {
              strings.noRoutineYetSubtitle
            },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = onAddClick,
            modifier = Modifier.testTag("empty_add_routine_button"),
          ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = strings.addRoutineButton)
          }

          if (!hasAnyRoutinesInApp) {
            OutlinedButton(
              onClick = onLoadStarterClick,
              modifier = Modifier.testTag("load_starter_routines_button"),
            ) {
              Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = strings.quickSampleRoutinesButton)
            }
          }
        }
      }
    }
  }
}
