package com.example.i18n

import java.util.Calendar
import java.util.Locale

enum class AppLanguage(val code: String) {
  BN("bn"),
  EN("en");

  companion object {
    fun fromCode(code: String?): AppLanguage =
      entries.firstOrNull { it.code == code } ?: BN
  }
}

data class AppStrings(
  val appTitle: String,
  val appSubtitle: String,
  val tabRoutine: String,
  val tabTodo: String,
  val tabFocus: String,
  val tabProgress: String,
  val langBadgeLabel: String,
  val toggleThemeDesc: String,
  val upNextLabel: String,
  val allDoneForTodayTitle: String,
  val allDoneForTodaySubtitle: String,
  val noRoutineYetTitle: String,
  val noRoutineYetSubtitle: String,
  val noRoutineOnSelectedDayTitle: String,
  val noRoutineOnSelectedDaySubtitle: String,
  val notifyOneMinBeforeBadge: String,
  val reminderAtPrefix: String,
  val startsInPrefix: String,
  val startingLessThanMin: String,
  val todayProgressLabel: String,
  val completedCountFormat: (Int, Int) -> String,
  val addRoutineButton: String,
  val addTodoButton: String,
  val editRoutineTitle: String,
  val newRoutineTitle: String,
  val editTodoTitle: String,
  val newTodoTitle: String,
  val fieldTitleLabel: String,
  val fieldTitlePlaceholderRoutine: String,
  val fieldTitlePlaceholderTodo: String,
  val fieldNoteLabel: String,
  val fieldNotePlaceholder: String,
  val fieldTimeLabel: String,
  val selectTimeButton: String,
  val changeTimeButton: String,
  val clearTimeButton: String,
  val optionalTimeHint: String,
  val repeatDaysLabel: String,
  val everyDayLabel: String,
  val todayBadge: String,
  val reminderToggleLabel: String,
  val reminderToggleSubtitle: String,
  val categoryLabel: String,
  val priorityLabel: String,
  val saveButton: String,
  val cancelButton: String,
  val deleteButton: String,
  val filterAll: String,
  val filterPending: String,
  val filterCompleted: String,
  val emptyTodoTitle: String,
  val emptyTodoSubtitle: String,
  val permissionNotificationTitle: String,
  val permissionNotificationBody: String,
  val permissionAllowButton: String,
  val permissionExactAlarmTitle: String,
  val permissionExactAlarmBody: String,
  val permissionSettingsButton: String,
  val testNotificationButton: String,
  val testNotificationSentToast: String,
  val validationTitleRequired: String,
  val timePickerTitle: String,
  val okButton: String,
  val catMorning: String,
  val catStudy: String,
  val catWork: String,
  val catHealth: String,
  val catMindful: String,
  val catPersonal: String,
  val prioHigh: String,
  val prioMedium: String,
  val prioNormal: String,
  val clearCompletedLabel: String,
  val notifRoutineTitlePrefix: String,
  val notifTodoTitlePrefix: String,
  val notifBodyOneMinLeft: (String, String) -> String,
  val notifTestTitle: String,
  val notifTestBody: String,
  val notifFocusDoneTitle: String,
  val notifFocusDoneBody: (String, Int) -> String,
  val reminderActiveDesc: String,
  val reminderMutedDesc: String,
  val quickSampleRoutinesButton: String,
  // Focus Timer Strings
  val focusHeaderTitle: String,
  val focusHeaderSubtitle: String,
  val focusPreset25: String,
  val focusPreset50: String,
  val focusPresetBreak5: String,
  val focusPresetBreak15: String,
  val focusStartButton: String,
  val focusPauseButton: String,
  val focusResumeButton: String,
  val focusResetButton: String,
  val focusRunningStatus: String,
  val focusPausedStatus: String,
  val focusReadyStatus: String,
  val focusLinkedTaskPrefix: String,
  val focusNoLinkedTask: String,
  val focusClearTaskButton: String,
  val focusTodayStatsTitle: String,
  val focusSessionsCountFormat: (Int) -> String,
  val focusMinutesCountFormat: (Int) -> String,
  val startFocusForRoutineDesc: String,
  val focusCompletedToast: String,
  // Progress & Streak Strings
  val progressHeaderTitle: String,
  val progressHeaderSubtitle: String,
  val currentStreakLabel: String,
  val bestStreakLabel: String,
  val streakDaysFormat: (Int) -> String,
  val weeklyCompletionTitle: String,
  val weeklyCompletionSubtitle: String,
  val weeklyRateLabel: String,
  val totalFocusTimeLabel: String,
  val categoryBreakdownTitle: String,
  val noStatsYetHint: String,
)

object Localization {
  val Bengali = AppStrings(
    appTitle = "দিনলিপি",
    appSubtitle = "প্রতিদিনের রুটিন, ফোকাস ও অগ্রগতি",
    tabRoutine = "রুটিন",
    tabTodo = "টু-ডু",
    tabFocus = "ফোকাস",
    tabProgress = "অগ্রগতি",
    langBadgeLabel = "EN",
    toggleThemeDesc = "ডার্ক ও লাইট থিম পরিবর্তন করুন",
    upNextLabel = "পরবর্তী রুটিন",
    allDoneForTodayTitle = "আজকের সব রুটিন সম্পন্ন!",
    allDoneForTodaySubtitle = "দারুণ ধারাবাহিকতা! আগামীকাল আবার নতুনভাবে শুরু হবে",
    noRoutineYetTitle = "আপনার প্রতিদিনের রুটিন সাজান",
    noRoutineYetSubtitle = "সময় ও বার সেট করলে ঠিক ১ মিনিট আগে সাউন্ড ও ভাইব্রেশন সহ নোটিফিকেশন পাবেন",
    noRoutineOnSelectedDayTitle = "এই বারে কোনো রুটিন সেট করা নেই",
    noRoutineOnSelectedDaySubtitle = "নতুন রুটিন যোগ করে এই বারটি নির্বাচন করতে পারেন",
    notifyOneMinBeforeBadge = "১ মিনিট আগে নোটিফিকেশন",
    reminderAtPrefix = "রিমাইন্ডার বাজবে:",
    startsInPrefix = "শুরু হতে বাকি:",
    startingLessThanMin = "১ মিনিটের কম বাকি!",
    todayProgressLabel = "আজকের অগ্রগতি",
    completedCountFormat = { done, total ->
      "${toBengaliDigits(done)} / ${toBengaliDigits(total)} সম্পন্ন"
    },
    addRoutineButton = "রুটিন যোগ করুন",
    addTodoButton = "নতুন কাজ যোগ করুন",
    editRoutineTitle = "রুটিন সম্পাদনা করুন",
    newRoutineTitle = "নতুন ডেইলি রুটিন",
    editTodoTitle = "টু-ডু কাজ সম্পাদনা",
    newTodoTitle = "নতুন টু-ডু কাজ",
    fieldTitleLabel = "কাজের নাম *",
    fieldTitlePlaceholderRoutine = "যেমন: সকালের ব্যায়াম বা পড়াশোনা",
    fieldTitlePlaceholderTodo = "যেমন: বাজার করা বা বিল পরিশোধ",
    fieldNoteLabel = "ছোট নোট (ঐচ্ছিক)",
    fieldNotePlaceholder = "প্রয়োজনীয় তথ্য লিখে রাখুন...",
    fieldTimeLabel = "নির্ধারিত সময়",
    selectTimeButton = "সময় সেট করুন",
    changeTimeButton = "সময় পরিবর্তন",
    clearTimeButton = "সময় মুছুন",
    optionalTimeHint = "সময় সেট করলে ১ মিনিট আগে নোটিফিকেশন আসবে",
    repeatDaysLabel = "সপ্তাহের বার নির্বাচন করুন",
    everyDayLabel = "প্রতিদিন",
    todayBadge = "আজ",
    reminderToggleLabel = "১ মিনিট আগে নোটিফিকেশন",
    reminderToggleSubtitle = "নির্বাচিত বারে সাউন্ড ও ভাইব্রেশন সহ সতর্কবার্তা পাঠাবে",
    categoryLabel = "ক্যাটাগরি",
    priorityLabel = "অগ্রাধিকার",
    saveButton = "সংরক্ষণ করুন",
    cancelButton = "বাতিল",
    deleteButton = "মুছে ফেলুন",
    filterAll = "সবগুলো",
    filterPending = "বাকি আছে",
    filterCompleted = "সম্পন্ন",
    emptyTodoTitle = "কোনো টু-ডু কাজ নেই",
    emptyTodoSubtitle = "প্রতিদিনের খুচরা কাজ ও প্রয়োজনীয় সময় সেট করে রাখুন",
    permissionNotificationTitle = "নোটিফিকেশন অনুমতি প্রয়োজন",
    permissionNotificationBody = "কাজের ১ মিনিট আগে সাউন্ড ও ভাইব্রেশন রিমাইন্ডার পেতে নোটিফিকেশন চালু করুন।",
    permissionAllowButton = "চালু করুন",
    permissionExactAlarmTitle = "সঠিক সময়ে অ্যালার্মের অনুমতি",
    permissionExactAlarmBody = "ঠিক ১ মিনিট আগেই নিখুঁত রিমাইন্ডার পেতে অ্যালার্ম পারমিশন দিন।",
    permissionSettingsButton = "সেটিংস খুলুন",
    testNotificationButton = "টেস্ট নোটিফিকেশন",
    testNotificationSentToast = "টেস্ট রিমাইন্ডার পাঠানো হয়েছে!",
    validationTitleRequired = "অনুগ্রহ করে কাজের নাম লিখুন",
    timePickerTitle = "কাজের সময় নির্বাচন করুন",
    okButton = "ঠিক আছে",
    catMorning = "সকাল",
    catStudy = "পড়াশোনা",
    catWork = "কাজ",
    catHealth = "স্বাস্থ্য",
    catMindful = "প্রার্থনা/মনন",
    catPersonal = "ব্যক্তিগত",
    prioHigh = "জরুরি",
    prioMedium = "মাঝারি",
    prioNormal = "সাধারণ",
    clearCompletedLabel = "সম্পন্নগুলো মুছুন",
    notifRoutineTitlePrefix = "রুটিন শুরু হতে ১ মিনিট বাকি",
    notifTodoTitlePrefix = "কাজের সময় হতে ১ মিনিট বাকি",
    notifBodyOneMinLeft = { title, formattedTime ->
      "“$title” শুরু হবে $formattedTime-এ। প্রস্তুতি নিন!"
    },
    notifTestTitle = "দিনলিপি রিমাইন্ডার সক্রিয় আছে!",
    notifTestBody = "আপনার নির্ধারিত কাজের ঠিক ১ মিনিট আগে এভাবেই সাউন্ড ও ভাইব্রেশন নোটিফিকেশন আসবে।",
    notifFocusDoneTitle = "ফোকাস টাইমার সম্পন্ন হয়েছে!",
    notifFocusDoneBody = { taskName, mins ->
      "অভিনন্দন! “$taskName” সেশনের ${toBengaliDigits(mins)} মিনিট পূর্ণ হয়েছে।"
    },
    reminderActiveDesc = "১ মিনিট আগে বাজবে",
    reminderMutedDesc = "রিমাইন্ডার বন্ধ",
    quickSampleRoutinesButton = "নমুনা রুটিন দিয়ে শুরু করুন",
    focusHeaderTitle = "পোমোডোরো ফোকাস টাইমার",
    focusHeaderSubtitle = "মনোযোগ ধরে রেখে পড়াশোনা বা গুরুত্বপূর্ণ কাজ সম্পন্ন করুন",
    focusPreset25 = "২৫ মিঃ ফোকাস",
    focusPreset50 = "৫০ মিঃ ডিপ স্টাডি",
    focusPresetBreak5 = "৫ মিঃ বিরতি",
    focusPresetBreak15 = "১৫ মিঃ বিরতি",
    focusStartButton = "শুরু করুন",
    focusPauseButton = "বিরতি",
    focusResumeButton = "চালিয়ে যান",
    focusResetButton = "রিসেট",
    focusRunningStatus = "মনোযোগী সেশন চলছে...",
    focusPausedStatus = "টাইমার থামানো আছে",
    focusReadyStatus = "শুরু করার জন্য প্রস্তুত",
    focusLinkedTaskPrefix = "চলমান কাজ:",
    focusNoLinkedTask = "সাধারণ ফোকাস সেশন",
    focusClearTaskButton = "পরিবর্তন",
    focusTodayStatsTitle = "আজকের ফোকাস সামারি",
    focusSessionsCountFormat = { count -> "${toBengaliDigits(count)}টি সেশন" },
    focusMinutesCountFormat = { mins -> "${toBengaliDigits(mins)} মিনিট" },
    startFocusForRoutineDesc = "এই রুটিনের জন্য ফোকাস টাইমার চালু করুন",
    focusCompletedToast = "ফোকাস সেশন সফলভাবে সম্পন্ন হয়েছে!",
    progressHeaderTitle = "স্ট্রিক ও সাপ্তাহিক রিপোর্ট",
    progressHeaderSubtitle = "আপনার প্রতিদিনের ধারাবাহিকতা ও গত ৭ দিনের অগ্রগতি",
    currentStreakLabel = "বর্তমান স্ট্রিক",
    bestStreakLabel = "সেরা স্ট্রিক",
    streakDaysFormat = { days -> "${toBengaliDigits(days)} দিন" },
    weeklyCompletionTitle = "গত ৭ দিনের রুটিন অগ্রগতি",
    weeklyCompletionSubtitle = "প্রতিদিন সম্পন্ন হওয়া রুটিনের শতকরা হার",
    weeklyRateLabel = "সাপ্তাহিক গড়",
    totalFocusTimeLabel = "মোট ফোকাস সময়",
    categoryBreakdownTitle = "ক্যাটাগরি অনুযায়ী রুটিন",
    noStatsYetHint = "রুটিন সম্পন্ন করলে এখানে আপনার স্ট্রিক ও চার্ট তৈরি হবে",
  )

  val English = AppStrings(
    appTitle = "DinLipi",
    appSubtitle = "Daily Routine, Focus & Progress",
    tabRoutine = "Routine",
    tabTodo = "To-Do",
    tabFocus = "Focus",
    tabProgress = "Progress",
    langBadgeLabel = "বাংলা",
    toggleThemeDesc = "Toggle dark and light theme",
    upNextLabel = "UP NEXT",
    allDoneForTodayTitle = "All Routines Done Today!",
    allDoneForTodaySubtitle = "Great streak! Your routine resets tomorrow morning",
    noRoutineYetTitle = "Build Your Daily Routine",
    noRoutineYetSubtitle = "Pick days & times to receive sound and vibration alerts 1 minute early",
    noRoutineOnSelectedDayTitle = "No Routines Scheduled For This Day",
    noRoutineOnSelectedDaySubtitle = "Add a new routine or assign existing ones to this weekday",
    notifyOneMinBeforeBadge = "1-Min Early Alert",
    reminderAtPrefix = "Alert at:",
    startsInPrefix = "Starts in:",
    startingLessThanMin = "Starting in < 1 min!",
    todayProgressLabel = "Today's Progress",
    completedCountFormat = { done, total -> "$done / $total done" },
    addRoutineButton = "Add Routine",
    addTodoButton = "Add Task",
    editRoutineTitle = "Edit Routine",
    newRoutineTitle = "New Daily Routine",
    editTodoTitle = "Edit To-Do Task",
    newTodoTitle = "New To-Do Task",
    fieldTitleLabel = "Task Title *",
    fieldTitlePlaceholderRoutine = "e.g., Morning Workout or Study",
    fieldTitlePlaceholderTodo = "e.g., Buy groceries or Pay utility bill",
    fieldNoteLabel = "Short Note (Optional)",
    fieldNotePlaceholder = "Add helpful details...",
    fieldTimeLabel = "Scheduled Time",
    selectTimeButton = "Set Time",
    changeTimeButton = "Change Time",
    clearTimeButton = "Clear",
    optionalTimeHint = "Set a time to get an alert 1 minute before",
    repeatDaysLabel = "Repeat on Weekdays",
    everyDayLabel = "Every Day",
    todayBadge = "Today",
    reminderToggleLabel = "Notify 1 Minute Early",
    reminderToggleSubtitle = "Ring with sound and vibration on scheduled days",
    categoryLabel = "Category",
    priorityLabel = "Priority",
    saveButton = "Save",
    cancelButton = "Cancel",
    deleteButton = "Delete",
    filterAll = "All",
    filterPending = "Pending",
    filterCompleted = "Completed",
    emptyTodoTitle = "No To-Do Tasks Yet",
    emptyTodoSubtitle = "Jot down tasks with optional 1-minute-early reminders",
    permissionNotificationTitle = "Enable Reminder Notifications",
    permissionNotificationBody = "Allow notifications so DinLipi can alert you 1 minute before each scheduled task.",
    permissionAllowButton = "Allow",
    permissionExactAlarmTitle = "Allow Exact Reminder Timing",
    permissionExactAlarmBody = "Grant exact alarm access so reminders ring precisely 1 minute on time.",
    permissionSettingsButton = "Open Settings",
    testNotificationButton = "Test Alert",
    testNotificationSentToast = "Test reminder notification sent!",
    validationTitleRequired = "Please enter a task title",
    timePickerTitle = "Select Scheduled Time",
    okButton = "Confirm",
    catMorning = "Morning",
    catStudy = "Study",
    catWork = "Work",
    catHealth = "Health",
    catMindful = "Mindful",
    catPersonal = "Personal",
    prioHigh = "High",
    prioMedium = "Medium",
    prioNormal = "Normal",
    clearCompletedLabel = "Clear Done",
    notifRoutineTitlePrefix = "Routine starts in 1 minute",
    notifTodoTitlePrefix = "Task starts in 1 minute",
    notifBodyOneMinLeft = { title, formattedTime ->
      "“$title” is scheduled for $formattedTime. Get ready!"
    },
    notifTestTitle = "DinLipi Reminders Ready!",
    notifTestBody = "You will receive sound & vibration alerts 1 minute before every scheduled task.",
    notifFocusDoneTitle = "Focus Session Complete!",
    notifFocusDoneBody = { taskName, mins ->
      "Great job! You completed $mins minutes on “$taskName”."
    },
    reminderActiveDesc = "Alerts 1m early",
    reminderMutedDesc = "Muted",
    quickSampleRoutinesButton = "Load Starter Routine",
    focusHeaderTitle = "Pomodoro Focus Timer",
    focusHeaderSubtitle = "Stay deeply focused on your study or work block",
    focusPreset25 = "25m Focus",
    focusPreset50 = "50m Deep Study",
    focusPresetBreak5 = "5m Break",
    focusPresetBreak15 = "15m Long Break",
    focusStartButton = "Start Focus",
    focusPauseButton = "Pause",
    focusResumeButton = "Resume",
    focusResetButton = "Reset",
    focusRunningStatus = "Deep focus in progress...",
    focusPausedStatus = "Timer paused",
    focusReadyStatus = "Ready to focus",
    focusLinkedTaskPrefix = "Focusing on:",
    focusNoLinkedTask = "General Focus Session",
    focusClearTaskButton = "Clear",
    focusTodayStatsTitle = "Today's Focus Summary",
    focusSessionsCountFormat = { count -> "$count sessions" },
    focusMinutesCountFormat = { mins -> "$mins mins" },
    startFocusForRoutineDesc = "Start Pomodoro focus timer for this routine",
    focusCompletedToast = "Focus session completed!",
    progressHeaderTitle = "Streaks & Weekly Report",
    progressHeaderSubtitle = "Track your consistency and 7-day completion rate",
    currentStreakLabel = "Current Streak",
    bestStreakLabel = "Best Streak",
    streakDaysFormat = { days -> "$days days" },
    weeklyCompletionTitle = "Last 7 Days Completion",
    weeklyCompletionSubtitle = "Daily routine completion percentage",
    weeklyRateLabel = "Weekly Rate",
    totalFocusTimeLabel = "Focus Time",
    categoryBreakdownTitle = "Routines by Category",
    noStatsYetHint = "Complete daily routines to build your streak and weekly chart",
  )

  fun forLanguage(language: AppLanguage): AppStrings =
    when (language) {
      AppLanguage.BN -> Bengali
      AppLanguage.EN -> English
    }

  private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

  fun toBengaliDigits(value: Int): String = toBengaliDigits(value.toString())

  fun toBengaliDigits(text: String): String {
    val sb = StringBuilder(text.length)
    for (ch in text) {
      if (ch in '0'..'9') {
        sb.append(bengaliDigits[ch - '0'])
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }

  fun formatNumber(value: Int, language: AppLanguage): String =
    if (language == AppLanguage.BN) toBengaliDigits(value) else value.toString()

  fun shortWeekdayLabel(calendarDayOfWeek: Int, language: AppLanguage): String {
    return if (language == AppLanguage.BN) {
      when (calendarDayOfWeek) {
        Calendar.SUNDAY -> "রবি"
        Calendar.MONDAY -> "সোম"
        Calendar.TUESDAY -> "মঙ্গল"
        Calendar.WEDNESDAY -> "বুধ"
        Calendar.THURSDAY -> "বৃহঃ"
        Calendar.FRIDAY -> "শুক্র"
        Calendar.SATURDAY -> "শনি"
        else -> "রবি"
      }
    } else {
      when (calendarDayOfWeek) {
        Calendar.SUNDAY -> "Sun"
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        else -> "Sun"
      }
    }
  }

  fun formatTimerClock(totalSeconds: Int, language: AppLanguage): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    val raw = String.format(Locale.US, "%02d:%02d", mins, secs)
    return if (language == AppLanguage.BN) toBengaliDigits(raw) else raw
  }

  fun formatTime(hour: Int, minute: Int, language: AppLanguage): String {
    val h24 = ((hour % 24) + 24) % 24
    val m = ((minute % 60) + 60) % 60
    val hour12 = when {
      h24 == 0 -> 12
      h24 > 12 -> h24 - 12
      else -> h24
    }
    val rawTime = String.format(Locale.US, "%d:%02d", hour12, m)
    return if (language == AppLanguage.BN) {
      val period = when (h24) {
        in 4..11 -> "সকাল"
        in 12..15 -> "দুপুর"
        in 16..18 -> "বিকাল"
        else -> "রাত"
      }
      "$period ${toBengaliDigits(rawTime)}"
    } else {
      val amPm = if (h24 < 12) "AM" else "PM"
      "$rawTime $amPm"
    }
  }

  fun oneMinuteBefore(hour: Int, minute: Int): Pair<Int, Int> {
    val totalMinutes = ((hour * 60 + minute - 1) + 1440) % 1440
    return (totalMinutes / 60) to (totalMinutes % 60)
  }

  fun formatCountdown(remainingSeconds: Long, language: AppLanguage): String {
    if (remainingSeconds <= 0L) {
      return if (language == AppLanguage.BN) "এখনই শুরু" else "Starting now"
    }
    val days = (remainingSeconds / 86_400L).toInt()
    val hours = ((remainingSeconds % 86_400L) / 3600).toInt()
    val minutes = ((remainingSeconds % 3600) / 60).toInt()
    val secs = (remainingSeconds % 60).toInt()

    return if (language == AppLanguage.BN) {
      buildString {
        if (days > 0) append("${toBengaliDigits(days)} দিন ")
        if (hours > 0) append("${toBengaliDigits(hours)} ঘঃ ")
        if ((minutes > 0 || hours > 0) && days == 0) append("${toBengaliDigits(minutes)} মিঃ ")
        if (days == 0) append("${toBengaliDigits(secs)} সেঃ")
      }.trim()
    } else {
      buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if ((minutes > 0 || hours > 0) && days == 0) append("${minutes}m ")
        if (days == 0) append("${secs}s")
      }.trim()
    }
  }
}
