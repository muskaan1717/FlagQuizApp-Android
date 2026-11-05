package com.flagquiz.app.presentation.settings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val themeMode: String,
        val isSoundEnabled: Boolean,
        val isHapticsEnabled: Boolean,
        val isReminderEnabled: Boolean,
        val appVersion: String = "1.0.0"
    ) : SettingsUiState
}