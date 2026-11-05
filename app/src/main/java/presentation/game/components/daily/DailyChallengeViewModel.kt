package com.flagquiz.app.presentation.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagquiz.app.core.common.DispatcherProvider
import com.flagquiz.app.data.local.preferences.UserPreferencesDataStore
import com.flagquiz.app.domain.engine.QuestionGenerator
import com.flagquiz.app.domain.engine.ScoringEngine
import com.flagquiz.app.domain.model.AnsweredQuestionRecord
import com.flagquiz.app.domain.model.Difficulty
import com.flagquiz.app.domain.model.GameMode
import com.flagquiz.app.domain.model.QuizHistory
import com.flagquiz.app.domain.model.QuizSession
import com.flagquiz.app.domain.model.Region
import com.flagquiz.app.domain.repository.CountryRepository
import com.flagquiz.app.domain.repository.QuizHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
    private val historyRepository: QuizHistoryRepository,
    private val preferences: UserPreferencesDataStore,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyChallengeUiState>(DailyChallengeUiState.Loading)
    val uiState: StateFlow<DailyChallengeUiState> = _uiState.asStateFlow()

    private val scoringEngine = ScoringEngine()
    private var timerJob: Job? = null
    private var sessionStartTimeMillis: Long = System.currentTimeMillis()

    init {
        checkDailyStatusAndInitialize()
    }

    fun checkDailyStatusAndInitialize() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = DailyChallengeUiState.Loading

            val today = LocalDate.now()
            val todayStr = today.toString()
            val lastPlayedDate = preferences.lastDailyDate.first()
            val currentStreak = preferences.dailyStreak.first()

            // 1. If already completed today, block replay and show countdown!
            if (lastPlayedDate == todayStr) {
                val hoursRemaining = 24 - LocalTime.now().hour
                _uiState.value = DailyChallengeUiState.AlreadyCompleted(
                    streak = currentStreak,
                    completionDate = todayStr,
                    hoursUntilNextChallenge = hoursRemaining
                )
                return@launch
            }

            // 2. Load all 195 nations
            val allCountries = countryRepository.getCountries(Region.ALL)
            if (allCountries.size < 4) {
                _uiState.value = DailyChallengeUiState.Error("Unable to load countries for Daily Challenge.")
                return@launch
            }

            // 3. SYNCHRONIZED SEED: LocalDate.now().toEpochDay()
            // This guarantees every player on Earth gets the exact same 10 questions today!
            val dateSeed = today.toEpochDay()
            val generator = QuestionGenerator(seed = dateSeed)
            val questions = generator.generateQuestions(
                allCountries = allCountries,
                mode = GameMode.FLAG_TO_NAME,
                difficulty = Difficulty.MEDIUM,
                region = Region.ALL,
                count = 10
            )

            sessionStartTimeMillis = System.currentTimeMillis()
            val session = QuizSession(
                questions = questions,
                remainingTimeSeconds = 15
            )

            _uiState.value = DailyChallengeUiState.Active(
                session = session,
                currentQuestion = questions.first(),
                remainingSeconds = 15,
                timerProgress = 1.0f,
                streak = currentStreak
            )

            startTimer(15)
        }
    }

    private fun startTimer(durationSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(dispatchers.default) {
            val totalMillis = durationSeconds * 1000L
            var remainingMillis = totalMillis

            while (isActive && remainingMillis > 0) {
                delay(50L)
                remainingMillis -= 50L
                val progress = (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
                val secondsRemaining = (remainingMillis / 1000L).toInt() + 1

                _uiState.update { state ->
                    if (state is DailyChallengeUiState.Active && !state.isAnswerRevealed) {
                        state.copy(
                            remainingSeconds = secondsRemaining,
                            timerProgress = progress
                        )
                    } else state
                }
            }

            if (isActive) {
                handleOptionSelected(selectedOptionId = null, wasTimeout = true)
            }
        }
    }

    fun onOptionSelected(optionId: String) {
        val state = _uiState.value as? DailyChallengeUiState.Active ?: return
        if (state.isAnswerRevealed) return
        timerJob?.cancel()
        handleOptionSelected(optionId, wasTimeout = false)
    }

    private fun handleOptionSelected(selectedOptionId: String?, wasTimeout: Boolean) {
        viewModelScope.launch(dispatchers.default) {
            val state = _uiState.value as? DailyChallengeUiState.Active ?: return@launch
            val question = state.currentQuestion
            val isCorrect = selectedOptionId != null && selectedOptionId == question.correctAnswerId

            val scoreAwarded = scoringEngine.calculateQuestionScore(
                isCorrect = isCorrect,
                remainingSeconds = if (wasTimeout) 0 else state.remainingSeconds,
                totalAllocatedSeconds = 15,
                difficulty = Difficulty.MEDIUM,
                currentStreak = state.session.currentStreak
            )

            val record = AnsweredQuestionRecord(
                question = question,
                selectedOptionId = selectedOptionId,
                wasCorrect = isCorrect,
                responseTimeMillis = ((15 - state.remainingSeconds) * 1000L).coerceAtLeast(0L),
                scoreAwarded = scoreAwarded
            )

            _uiState.update { current ->
                if (current is DailyChallengeUiState.Active) {
                    current.copy(
                        selectedOptionId = selectedOptionId,
                        isAnswerRevealed = true
                    )
                } else current
            }

            delay(1500L)

            val updatedSession = state.session.copy(
                score = state.session.score + scoreAwarded,
                currentStreak = scoringEngine.computeNewStreak(isCorrect, state.session.currentStreak),
                answeredQuestions = state.session.answeredQuestions + record
            )

            val isLast = state.session.currentQuestionIndex >= state.session.questions.lastIndex
            if (isLast) {
                completeDailyChallenge(updatedSession)
            } else {
                val nextIndex = state.session.currentQuestionIndex + 1
                val nextQuestion = state.session.questions[nextIndex]
                _uiState.value = DailyChallengeUiState.Active(
                    session = updatedSession.copy(currentQuestionIndex = nextIndex),
                    currentQuestion = nextQuestion,
                    selectedOptionId = null,
                    isAnswerRevealed = false,
                    remainingSeconds = 15,
                    timerProgress = 1.0f,
                    streak = state.streak
                )
                startTimer(15)
            }
        }
    }

    private fun completeDailyChallenge(session: QuizSession) {
        viewModelScope.launch(dispatchers.io) {
            timerJob?.cancel()
            val today = LocalDate.now()
            val todayStr = today.toString()
            val yesterdayStr = today.minusDays(1).toString()
            val lastDate = preferences.lastDailyDate.first()
            val previousStreak = preferences.dailyStreak.first()

            val newStreak = if (lastDate == yesterdayStr) {
                previousStreak + 1
            } else {
                1
            }

            preferences.recordDailyChallengeCompleted(todayStr, newStreak)

            val history = QuizHistory(
                timestamp = System.currentTimeMillis(),
                gameMode = "DAILY_CHALLENGE",
                difficulty = "MEDIUM",
                score = session.score,
                totalQuestions = session.questions.size,
                highestStreak = session.bestStreak,
                timeSpentSeconds = ((System.currentTimeMillis() - sessionStartTimeMillis) / 1000L).toInt(),
                missedCountryCodes = session.answeredQuestions.filter { !it.wasCorrect }.map { it.question.targetCountry.code }
            )
            historyRepository.saveQuizResult(history)

            _uiState.value = DailyChallengeUiState.CompletedToday(
                finalScore = session.score,
                totalQuestions = session.questions.size,
                newStreak = newStreak
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}