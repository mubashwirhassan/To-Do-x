package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.Localization
import com.example.ui.DailyProgressPoint
import com.example.ui.ProgressSummary

@Composable
fun ProgressReportSection(
  summary: ProgressSummary,
  totalRoutines: Int,
  language: AppLanguage,
  strings: AppStrings,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("progress_report_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 36.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item(key = "progress_header") {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = strings.progressHeaderTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = strings.progressHeaderSubtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Streak Duo Cards
    item(key = "streak_cards") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        StatHighlightCard(
          title = strings.currentStreakLabel,
          value = strings.streakDaysFormat(summary.currentStreak),
          icon = Icons.Filled.LocalFireDepartment,
          containerColor = MaterialTheme.colorScheme.tertiaryContainer,
          contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          iconTint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.weight(1f).testTag("current_streak_card"),
        )

        StatHighlightCard(
          title = strings.bestStreakLabel,
          value = strings.streakDaysFormat(summary.bestStreak),
          icon = Icons.Filled.EmojiEvents,
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          iconTint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f).testTag("best_streak_card"),
        )
      }
    }

    // Secondary Metrics Row (Weekly Average % + Total Focus Mins)
    item(key = "secondary_metrics") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        val avgText = "${Localization.formatNumber(summary.weeklyAveragePercent, language)}%"
        StatHighlightCard(
          title = strings.weeklyRateLabel,
          value = avgText,
          icon = Icons.Outlined.Insights,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          iconTint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f).testTag("weekly_rate_card"),
        )

        StatHighlightCard(
          title = strings.totalFocusTimeLabel,
          value = strings.focusMinutesCountFormat(summary.totalFocusMinutes),
          icon = Icons.Outlined.Timer,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          iconTint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.weight(1f).testTag("total_focus_card"),
        )
      }
    }

    // 7-Day Weekly Bar Chart
    item(key = "weekly_bar_chart") {
      WeeklyProgressChartCard(
        points = summary.weeklyPoints,
        hasRoutines = totalRoutines > 0,
        language = language,
        strings = strings,
      )
    }

    // Category Breakdown
    if (summary.categoryBreakdown.isNotEmpty()) {
      item(key = "category_breakdown") {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = strings.categoryBreakdownTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )

            summary.categoryBreakdown.forEach { (cat, count) ->
              val fraction =
                if (totalRoutines > 0) count.toFloat() / totalRoutines.toFloat() else 0f
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = cat.icon(),
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = cat.label(strings),
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Medium,
                    )
                  }
                  Text(
                    text = Localization.formatNumber(count, language),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
                LinearProgressIndicator(
                  progress = { fraction },
                  modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
                  color = MaterialTheme.colorScheme.primary,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StatHighlightCard(
  title: String,
  value: String,
  icon: ImageVector,
  containerColor: androidx.compose.ui.graphics.Color,
  contentColor: androidx.compose.ui.graphics.Color,
  iconTint: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.large,
    colors =
      CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor,
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier =
          Modifier.size(38.dp)
            .clip(CircleShape)
            .background(iconTint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(20.dp),
        )
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = value,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = contentColor.copy(alpha = 0.82f),
        )
      }
    }
  }
}

@Composable
private fun WeeklyProgressChartCard(
  points: List<DailyProgressPoint>,
  hasRoutines: Boolean,
  language: AppLanguage,
  strings: AppStrings,
) {
  Card(
    modifier = Modifier.fillMaxWidth().testTag("weekly_chart_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = strings.weeklyCompletionTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = if (hasRoutines) strings.weeklyCompletionSubtitle else strings.noStatsYetHint,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth().height(168.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
      ) {
        points.forEach { point ->
          val animatedRatio by
            animateFloatAsState(
              targetValue = point.ratio.coerceAtLeast(if (point.scheduledCount > 0) 0.08f else 0.04f),
              animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
              label = "bar_${point.dayKey}",
            )

          Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
          ) {
            Text(
              text =
                if (point.scheduledCount > 0) {
                  "${Localization.formatNumber(point.percent, language)}%"
                } else {
                  "–"
                },
              style = MaterialTheme.typography.labelSmall,
              color =
                if (point.isToday) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              fontWeight = if (point.isToday) FontWeight.Bold else FontWeight.Normal,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
              modifier =
                Modifier.fillMaxWidth(0.68f)
                  .weight(1f)
                  .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
              contentAlignment = Alignment.BottomCenter,
            ) {
              Box(
                modifier =
                  Modifier.fillMaxWidth()
                    .fillMaxHeight(animatedRatio)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                    .background(
                      when {
                        point.completedCount > 0 && point.isToday ->
                          MaterialTheme.colorScheme.tertiary
                        point.completedCount > 0 ->
                          MaterialTheme.colorScheme.primary
                        else ->
                          MaterialTheme.colorScheme.outlineVariant
                      }
                    )
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = Localization.shortWeekdayLabel(point.calendarDayOfWeek, language),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (point.isToday) FontWeight.Bold else FontWeight.Medium,
              color =
                if (point.isToday) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
          }
        }
      }
    }
  }
}
