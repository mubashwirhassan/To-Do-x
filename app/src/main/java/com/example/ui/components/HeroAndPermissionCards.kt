package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization
import com.example.reminder.NotificationHelper
import com.example.reminder.ReminderScheduler
import com.example.ui.UpcomingRoutineInfo

@Composable
fun PermissionGuidanceBanners(
  strings: AppStrings,
  onPermissionsUpdated: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val scheduler = remember(context) { ReminderScheduler(context) }

  var hasNotifPermission by remember {
    mutableStateOf(NotificationHelper.hasNotificationPermission(context))
  }
  var canScheduleExact by remember {
    mutableStateOf(scheduler.canScheduleExactAlarms())
  }

  val notifPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      hasNotifPermission = granted || NotificationHelper.hasNotificationPermission(context)
      if (hasNotifPermission) {
        onPermissionsUpdated()
      }
    }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        val updatedNotif = NotificationHelper.hasNotificationPermission(context)
        val updatedExact = scheduler.canScheduleExactAlarms()
        if (updatedNotif != hasNotifPermission || updatedExact != canScheduleExact) {
          hasNotifPermission = updatedNotif
          canScheduleExact = updatedExact
          onPermissionsUpdated()
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    AnimatedVisibility(visible = !hasNotifPermission) {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("notification_permission_banner"),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          ),
        shape = MaterialTheme.shapes.medium,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier =
              Modifier.size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.permissionNotificationTitle,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = strings.permissionNotificationBody,
              style = MaterialTheme.typography.bodySmall,
            )
          }
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(
              onClick = {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              },
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.tertiary,
                  contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
              modifier = Modifier.testTag("grant_notification_permission_button"),
            ) {
              Text(text = strings.permissionAllowButton)
            }
          }
        }
      }
    }

    AnimatedVisibility(visible = hasNotifPermission && !canScheduleExact) {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("exact_alarm_permission_banner"),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
          ),
        shape = MaterialTheme.shapes.medium,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.Alarm,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.permissionExactAlarmTitle,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = strings.permissionExactAlarmBody,
              style = MaterialTheme.typography.bodySmall,
            )
          }
          OutlinedButton(
            onClick = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent =
                  Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                  }
                try {
                  context.startActivity(intent)
                } catch (_: Exception) {
                  context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                  })
                }
              }
            },
            modifier = Modifier.testTag("open_exact_alarm_settings_button"),
          ) {
            Text(text = strings.permissionSettingsButton)
          }
        }
      }
    }
  }
}

@Composable
fun UpNextHeroCard(
  upcoming: UpcomingRoutineInfo?,
  totalRoutines: Int,
  completedToday: Int,
  language: AppLanguage,
  strings: AppStrings,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val primaryContainer = MaterialTheme.colorScheme.primaryContainer
  val onPrimary = MaterialTheme.colorScheme.onPrimary
  val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
  val onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer

  val progressTarget =
    if (totalRoutines > 0) completedToday.toFloat() / totalRoutines.toFloat() else 0f
  val animatedProgress by
    animateFloatAsState(
      targetValue = progressTarget,
      animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
      label = "routine_progress",
    )

  Card(
    modifier = modifier.fillMaxWidth().testTag("up_next_hero_card"),
    shape = MaterialTheme.shapes.large,
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            brush =
              Brush.linearGradient(
                colors =
                  listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.88f),
                  )
              )
          )
          .drawBehind {
            drawCircle(
              color = primaryContainer.copy(alpha = 0.14f),
              radius = size.minDimension * 0.65f,
              center = Offset(size.width * 0.92f, size.height * 0.15f),
            )
            drawCircle(
              color = primaryContainer.copy(alpha = 0.09f),
              radius = size.minDimension * 0.42f,
              center = Offset(size.width * 0.1f, size.height * 0.95f),
            )
          }
          .padding(20.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (upcoming != null) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              color = onPrimary.copy(alpha = 0.18f),
              contentColor = onPrimary,
              shape = RoundedCornerShape(50),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.AlarmOn,
                  contentDescription = null,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = strings.upNextLabel,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                )
              }
            }

            if (upcoming.routine.isReminderEnabled) {
              Surface(
                color = tertiaryContainer,
                contentColor = onTertiaryContainer,
                shape = RoundedCornerShape(50),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text =
                      "${strings.reminderAtPrefix} ${
                        Localization.formatTime(
                          upcoming.reminderHour,
                          upcoming.reminderMinute,
                          language,
                        )
                      }",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = upcoming.routine.title,
                style = MaterialTheme.typography.headlineSmall,
                color = onPrimary,
                fontWeight = FontWeight.Bold,
              )
              if (upcoming.routine.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = upcoming.routine.note,
                  style = MaterialTheme.typography.bodyMedium,
                  color = onPrimary.copy(alpha = 0.86f),
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text =
                  Localization.formatTime(
                    upcoming.routine.hour,
                    upcoming.routine.minute,
                    language,
                  ),
                style = MaterialTheme.typography.titleLarge,
                color = onPrimary,
                fontWeight = FontWeight.Bold,
              )
              val countdownText =
                if (upcoming.remainingSeconds in 1..59) {
                  strings.startingLessThanMin
                } else {
                  "${strings.startsInPrefix} ${
                    Localization.formatCountdown(upcoming.remainingSeconds, language)
                  }"
                }
              Text(
                text = countdownText,
                style = MaterialTheme.typography.labelMedium,
                color = onPrimary.copy(alpha = 0.92f),
              )
            }
          }
        } else if (totalRoutines > 0) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = onPrimary,
              modifier = Modifier.size(36.dp),
            )
            Column {
              Text(
                text = strings.allDoneForTodayTitle,
                style = MaterialTheme.typography.titleLarge,
                color = onPrimary,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = strings.allDoneForTodaySubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = onPrimary.copy(alpha = 0.88f),
              )
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
              color = tertiaryContainer,
              contentColor = onTertiaryContainer,
              shape = RoundedCornerShape(50),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.NotificationsActive,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                )
                Text(
                  text = strings.notifyOneMinBeforeBadge,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = strings.noRoutineYetTitle,
              style = MaterialTheme.typography.titleLarge,
              color = onPrimary,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = strings.noRoutineYetSubtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = onPrimary.copy(alpha = 0.9f),
            )
          }
        }

        if (totalRoutines > 0) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = strings.todayProgressLabel,
                style = MaterialTheme.typography.labelMedium,
                color = onPrimary.copy(alpha = 0.88f),
              )
              Text(
                text = strings.completedCountFormat(completedToday, totalRoutines),
                style = MaterialTheme.typography.labelMedium,
                color = onPrimary,
                fontWeight = FontWeight.Bold,
              )
            }
            LinearProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
              color = tertiaryContainer,
              trackColor = onPrimary.copy(alpha = 0.24f),
            )
          }
        }
      }
    }
  }
}
