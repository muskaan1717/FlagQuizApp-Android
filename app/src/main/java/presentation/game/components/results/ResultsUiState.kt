package com.flagquiz.app.presentation.results

import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.QuizHistory

sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data class Success(
        val history: QuizHistory,
        val missedCountries: List<Country>,
        val accuracyPercentage: Float
    ) : ResultsUiState
    data class Error(val message: String) : ResultsUiState
}