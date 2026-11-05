package com.flagquiz.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val DAILY_CHALLENGE_STREAK = intPreferencesKey("daily_streak")
        val LAST_DAILY_DATE = stringPreferencesKey("last_daily_date")
    }

    val isSoundEnabled: Flow<Boolean> = context.dataStore.data
        .catchPreferencesErrors()
        .map { it[PreferencesKeys.SOUND_ENABLED] ?: true }

    val isHapticsEnabled: Flow<Boolean> = context.dataStore.data
        .catchPreferencesErrors()
        .map { it[PreferencesKeys.HAPTICS_ENABLED] ?: true }

    val themeMode: Flow<String> = context.dataStore.data
        .catchPreferencesErrors()
        .map { it[PreferencesKeys.DARK_THEME_MODE] ?: "SYSTEM" }

    val lastDailyDate: Flow<String> = context.dataStore.data
        .catchPreferencesErrors()
        .map { it[PreferencesKeys.LAST_DAILY_DATE] ?: "" }

    val dailyStreak: Flow<Int> = context.dataStore.data
        .catchPreferencesErrors()
        .map { it[PreferencesKeys.DAILY_CHALLENGE_STREAK] ?: 0 }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.DARK_THEME_MODE] = mode }
    }

    suspend fun recordSyncSuccess(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { it[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp }
    }

    suspend fun recordDailyChallengeCompleted(dateString: String, newStreak: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.LAST_DAILY_DATE] = dateString
            it[PreferencesKeys.DAILY_CHALLENGE_STREAK] = newStreak
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }

    private fun Flow<Preferences>.catchPreferencesErrors(): Flow<Preferences> =
        this.catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Failed reading preferences from DataStore.")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
}