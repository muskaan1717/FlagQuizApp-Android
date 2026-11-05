package com.flagquiz.app.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagquiz.app.core.common.DispatcherProvider
import com.flagquiz.app.domain.model.Region
import com.flagquiz.app.domain.repository.CountryRepository
import com.flagquiz.app.domain.repository.QuizHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val historyRepository: QuizHistoryRepository,
    private val countryRepository: CountryRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = StatsUiState.Loading

            combine(
                historyRepository.getHistoryStream(),
                countryRepository.getCountriesStream(Region.ALL)
            ) { historyList, countries ->
                if (historyList.isEmpty() && countries.isEmpty()) {
                    StatsUiState.Success(
                        totalGamesPlayed = 0,
                        overallAccuracy = 0f,
                        highestAllTimeStreak = 0,
                        highScores = emptyList(),
                        accuracyTrend = emptyList(),
                        allCountries = emptyList()
                    )
                } else {
                    val totalGames = historyList.size
                    val totalScorePossible = historyList.sumOf { it.totalQuestions * 150 }
                    val totalScoreEarned = historyList.sumOf { it.score }
                    val overallAccuracy = if (totalScorePossible > 0) {
                        (totalScoreEarned.toFloat() / totalScorePossible.toFloat() * 100f).coerceIn(0f, 100f)
                    } else 0f

                    val highestStreak = historyList.maxOfOrNull { it.highestStreak } ?: 0
                    val topScores = historyList.sortedByDescending { it.score }.take(10)

                    val accuracyTrend = historyList.take(15)
                        .reversed()
                        .map { it.accuracyPercentage }

                    StatsUiState.Success(
                        totalGamesPlayed = totalGames,
                        overallAccuracy = overallAccuracy,
                        highestAllTimeStreak = highestStreak,
                        highScores = topScores,
                        accuracyTrend = accuracyTrend,
                        allCountries = countries
                    )
                }
            }.collect { calculatedState ->
                _uiState.value = calculatedState
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(dispatchers.io) {
            historyRepository.clearAllHistory()
        }
    }
}