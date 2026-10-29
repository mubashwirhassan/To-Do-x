package com.example.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.Localization

object NotificationHelper {
  const val CHANNEL_ID = "dinlipi_reminders_high_v1"
  private const val DEDUP_PREFS = "dinlipi_reminder_dedup"
  val VIBRATION_PATTERN = longArrayOf(0L, 350L, 180L, 450L)

  fun ensureChannelCreated(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val soundUri =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
          ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      val audioAttributes =
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()

      val channel =
        NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name_reminders),
            NotificationManager.IMPORTANCE_HIGH,
          )
          .apply {
            description = context.getString(R.string.channel_description_reminders)
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            setSound(soundUri, audioAttributes)
          }
      manager.createNotificationChannel(channel)
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  @Synchronized
  fun postTaskReminder(
    context: Context,
    notificationId: Int,
    dedupKey: String,
    epochMinute: Long,
    isRoutine: Boolean,
    taskTitle: String,
    scheduledHour: Int,
    scheduledMinute: Int,
    language: AppLanguage,
  ): Boolean {
    val prefs = context.getSharedPreferences(DEDUP_PREFS, Context.MODE_PRIVATE)
    val lastEpochMinute = prefs.getLong(dedupKey, -1L)
    if (lastEpochMinute == epochMinute) {
      return false
    }
    prefs.edit().putLong(dedupKey, epochMinute).apply()

    ensureChannelCreated(context)
    triggerDeviceVibration(context)

    if (!hasNotificationPermission(context)) {
      return false
    }

    val strings = Localization.forLanguage(language)
    val formattedTime = Localization.formatTime(scheduledHour, scheduledMinute, language)
    val header = if (isRoutine) strings.notifRoutineTitlePrefix else strings.notifTodoTitlePrefix
    val body = strings.notifBodyOneMinLeft(taskTitle, formattedTime)

    postNotificationInternal(context, notificationId, header, body)
    return true
  }

  fun postFocusCompleteNotification(
    context: Context,
    taskTitle: String,
    durationMinutes: Int,
    language: AppLanguage,
  ): Boolean {
    ensureChannelCreated(context)
    triggerDeviceVibration(context)
    if (!hasNotificationPermission(context)) {
      return false
    }
    val strings = Localization.forLanguage(language)
    postNotificationInternal(
      context = context,
      notificationId = 999_998,
      title = strings.notifFocusDoneTitle,
      body = strings.notifFocusDoneBody(taskTitle, durationMinutes),
    )
    return true
  }

  fun postTestNotification(context: Context, language: AppLanguage): Boolean {
    ensureChannelCreated(context)
    triggerDeviceVibration(context)
    if (!hasNotificationPermission(context)) {
      return false
    }
    val strings = Localization.forLanguage(language)
    postNotificationInternal(
      context = context,
      notificationId = 999_999,
      title = strings.notifTestTitle,
      body = strings.notifTestBody,
    )
    return true
  }

  private fun postNotificationInternal(
    context: Context,
    notificationId: Int,
    title: String,
    body: String,
  ) {
    val tapIntent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
    val pendingIntent =
      PendingIntent.getActivity(
        context,
        notificationId,
        tapIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
    val soundUri =
      RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    val notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setSound(soundUri)
        .setVibrate(VIBRATION_PATTERN)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    try {
      NotificationManagerCompat.from(context).notify(notificationId, notification)
    } catch (_: SecurityException) {
      // Handled safely if permission was revoked concurrently
    }
  }

  private fun triggerDeviceVibration(context: Context) {
    try {
      val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
          vm?.defaultVibrator
        } else {
          @Suppress("DEPRECATION")
          context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
      if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1))
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(VIBRATION_PATTERN, -1)
        }
      }
    } catch (_: Exception) {
      // Ignore hardware absence on headless environments
    }
  }
}
