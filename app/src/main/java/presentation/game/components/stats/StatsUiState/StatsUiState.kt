package com.flagquiz.app.presentation.stats

import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.QuizHistory

sealed interface StatsUiState {
    data object Loading : StatsUiState

    data class Success(
        val totalGamesPlayed: Int,
        val overallAccuracy: Float,
        val highestAllTimeStreak: Int,
        val highScores: List<QuizHistory>,
        val accuracyTrend: List<Float>,
        val allCountries: List<Country>
    ) : StatsUiState

    data class Error(val message: String) : StatsUiState
}