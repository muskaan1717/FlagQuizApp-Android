package com.flagquiz.app.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagquiz.app.core.common.DispatcherProvider
import com.flagquiz.app.domain.model.QuizHistory
import com.flagquiz.app.domain.repository.CountryRepository
import com.flagquiz.app.domain.repository.QuizHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val historyRepository: QuizHistoryRepository,
    private val countryRepository: CountryRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun loadResults(score: Int, total: Int, streak: Int, historyId: Long) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = ResultsUiState.Loading

            val history = if (historyId > 0L) {
                historyRepository.getHistoryById(historyId)
            } else null

            val fallbackHistory = history ?: QuizHistory(
                id = 0L,
                timestamp = System.currentTimeMillis(),
                gameMode = "FLAG_TO_NAME",
                difficulty = "MEDIUM",
                score = score,
                totalQuestions = total,
                highestStreak = streak,
                timeSpentSeconds = 0,
                missedCountryCodes = emptyList()
            )

            // Resolve full country entities for all missed codes so the player can study them!
            val missedCountries = fallbackHistory.missedCountryCodes.mapNotNull { code ->
                countryRepository.getCountryByCode(code)
            }

            val accuracy = if (fallbackHistory.totalQuestions > 0) {
                (fallbackHistory.score.toFloat() / (fallbackHistory.totalQuestions * 150).toFloat() * 100f).coerceIn(0f, 100f)
            } else 0f

            _uiState.value = ResultsUiState.Success(
                history = fallbackHistory,
                missedCountries = missedCountries,
                accuracyPercentage = accuracy
            )
        }
    }
}