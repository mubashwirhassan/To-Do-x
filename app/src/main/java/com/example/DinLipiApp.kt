package com.example

import android.app.Application
import com.example.data.DinLipiDatabase
import com.example.data.PlannerRepository
import com.example.data.PreferencesRepository
import com.example.reminder.NotificationHelper
import com.example.reminder.ReminderScheduler

class DinLipiApp : Application() {
  lateinit var database: DinLipiDatabase
    private set

  lateinit var preferencesRepository: PreferencesRepository
    private set

  lateinit var reminderScheduler: ReminderScheduler
    private set

  lateinit var plannerRepository: PlannerRepository
    private set

  override fun onCreate() {
    super.onCreate()
    NotificationHelper.ensureChannelCreated(this)
    database = DinLipiDatabase.getInstance(this)
    preferencesRepository = PreferencesRepository(this)
    reminderScheduler = ReminderScheduler(this)
    plannerRepository = PlannerRepository(database.dao(), reminderScheduler)
  }
}
