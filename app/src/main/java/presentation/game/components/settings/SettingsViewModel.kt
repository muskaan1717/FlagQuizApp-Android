package com.flagquiz.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagquiz.app.core.common.DispatcherProvider
import com.flagquiz.app.core.worker.DailyReminderWorker
import com.flagquiz.app.data.local.FlagDatabase
import com.flagquiz.app.data.local.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataStore,
    private val database: FlagDatabase,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _isReminderEnabled = MutableStateFlow(true)

    init {
        viewModelScope.launch(dispatchers.io) {
            combine(
                preferences.themeMode,
                preferences.isSoundEnabled,
                preferences.isHapticsEnabled,
                _isReminderEnabled
            ) { theme, sound, haptics, reminder ->
                SettingsUiState.Success(
                    themeMode = theme,
                    isSoundEnabled = sound,
                    isHapticsEnabled = haptics,
                    isReminderEnabled = reminder
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch(dispatchers.io) { preferences.setThemeMode(mode) }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) { preferences.setSoundEnabled(enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) { preferences.setHapticsEnabled(enabled) }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        _isReminderEnabled.value = enabled
        if (enabled) {
            DailyReminderWorker.scheduleReminder(context)
        } else {
            DailyReminderWorker.cancelReminder(context)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch(dispatchers.io) {
            database.clearAllTables()
            preferences.clearPreferences()
        }
    }
}