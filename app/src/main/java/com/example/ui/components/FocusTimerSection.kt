package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FocusPreset
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization
import com.example.ui.FocusTimerState
import com.example.ui.ProgressSummary
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusTimerSection(
  focusState: FocusTimerState,
  progressSummary: ProgressSummary,
  language: AppLanguage,
  strings: AppStrings,
  onSelectPreset: (FocusPreset) -> Unit,
  onToggleStartPause: () -> Unit,
  onReset: () -> Unit,
  onClearLinkedTask: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val animatedSweep by
    animateFloatAsState(
      targetValue = 360f * (1f - focusState.progress),
      animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
      label = "focus_arc_sweep",
    )

  val arcPrimaryColor =
    if (focusState.preset.isBreak) {
      MaterialTheme.colorScheme.tertiary
    } else {
      MaterialTheme.colorScheme.primary
    }
  val arcTrackColor = MaterialTheme.colorScheme.surfaceVariant
  val dotAccentColor = MaterialTheme.colorScheme.tertiary

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("focus_timer_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 36.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    item(key = "focus_header") {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = strings.focusHeaderTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = strings.focusHeaderSubtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Linked Routine / Task Banner
    item(key = "focus_linked_task") {
      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.TaskAlt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              if (focusState.linkedTaskTitle != null) {
                Text(
                  text = strings.focusLinkedTaskPrefix,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = focusState.linkedTaskTitle ?: strings.focusNoLinkedTask,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
          if (focusState.linkedTaskTitle != null) {
            TextButton(
              onClick = onClearLinkedTask,
              modifier = Modifier.testTag("focus_clear_task_button"),
            ) {
              Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(strings.focusClearTaskButton)
            }
          }
        }
      }
    }

    // Preset Chips
    item(key = "focus_presets") {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        FocusPreset.entries.forEach { preset ->
          FilterChip(
            selected = focusState.preset == preset,
            onClick = { onSelectPreset(preset) },
            label = { Text(preset.label(strings)) },
            modifier = Modifier.testTag("focus_preset_${preset.name.lowercase()}"),
          )
        }
      }
    }

    // Main Circular Arc Countdown Card
    item(key = "focus_dial") {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          Box(
            modifier = Modifier.size(236.dp),
            contentAlignment = Alignment.Center,
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val strokeWidth = 14.dp.toPx()
              val diameter = size.minDimension - strokeWidth * 2f
              val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

              // Background Track Ring
              drawArc(
                color = arcTrackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
              )

              // Remaining Time Arc
              if (animatedSweep > 0.5f) {
                drawArc(
                  color = arcPrimaryColor,
                  startAngle = -90f,
                  sweepAngle = animatedSweep,
                  useCenter = false,
                  topLeft = topLeft,
                  size = Size(diameter, diameter),
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                val angleRad = Math.toRadians((-90f + animatedSweep).toDouble())
                val radius = diameter / 2f
                val cx = size.width / 2f + (radius * cos(angleRad)).toFloat()
                val cy = size.height / 2f + (radius * sin(angleRad)).toFloat()
                drawCircle(
                  color = dotAccentColor,
                  radius = strokeWidth * 0.55f,
                  center = Offset(cx, cy),
                )
              }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = Localization.formatTimerClock(focusState.remainingSeconds, language),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("focus_clock_display"),
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text =
                  when {
                    focusState.isRunning -> strings.focusRunningStatus
                    focusState.hasStarted -> strings.focusPausedStatus
                    else -> strings.focusReadyStatus
                  },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Button(
              onClick = onToggleStartPause,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = arcPrimaryColor,
                  contentColor =
                    if (focusState.preset.isBreak) {
                      MaterialTheme.colorScheme.onTertiary
                    } else {
                      MaterialTheme.colorScheme.onPrimary
                    }
                ),
              shape = RoundedCornerShape(50),
              contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
              modifier = Modifier.testTag("focus_start_pause_button"),
            ) {
              Icon(
                imageVector =
                  if (focusState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text =
                  when {
                    focusState.isRunning -> strings.focusPauseButton
                    focusState.hasStarted -> strings.focusResumeButton
                    else -> strings.focusStartButton
                  },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
            }

            OutlinedButton(
              onClick = onReset,
              shape = RoundedCornerShape(50),
              contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
              modifier = Modifier.testTag("focus_reset_button"),
            ) {
              Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = strings.focusResetButton)
            }
          }
        }
      }
    }

    // Today's Focus Summary Card
    item(key = "focus_today_stats") {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.Timer,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column {
              Text(
                text = strings.focusTodayStatsTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = strings.focusSessionsCountFormat(progressSummary.todayFocusSessions),
                style = MaterialTheme.typography.bodySmall,
              )
            }
          }

          Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = strings.focusMinutesCountFormat(progressSummary.todayFocusMinutes),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
          }
        }
      }
    }
  }
}
