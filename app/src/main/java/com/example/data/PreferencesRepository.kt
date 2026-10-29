package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dinLipiDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "dinlipi_prefs")

class PreferencesRepository(private val context: Context) {
  private val languageKey = stringPreferencesKey("app_language")
  private val themeModeKey = stringPreferencesKey("theme_mode")

  val languageFlow: Flow<AppLanguage> =
    context.dinLipiDataStore.data.map { prefs ->
      AppLanguage.fromCode(prefs[languageKey])
    }

  val themeModeFlow: Flow<ThemeMode> =
    context.dinLipiDataStore.data.map { prefs ->
      ThemeMode.fromCode(prefs[themeModeKey])
    }

  suspend fun getCurrentLanguage(): AppLanguage = languageFlow.first()

  suspend fun setLanguage(language: AppLanguage) {
    context.dinLipiDataStore.edit { prefs ->
      prefs[languageKey] = language.code
    }
  }

  suspend fun toggleLanguage() {
    val next = if (getCurrentLanguage() == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN
    setLanguage(next)
  }

  suspend fun toggleThemeMode(currentlyDark: Boolean) {
    val next = if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
    context.dinLipiDataStore.edit { prefs ->
      prefs[themeModeKey] = next.code
    }
  }
}
