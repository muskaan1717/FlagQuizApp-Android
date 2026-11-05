package com.flagquiz.app.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagquiz.app.core.common.DispatcherProvider
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface GameUiEffect {
    data object TriggerHapticSuccess : GameUiEffect
    data object TriggerHapticFailure : GameUiEffect
    data object PlaySoundCorrect : GameUiEffect
    data object PlaySoundWrong : GameUiEffect
    data class NavigateToResults(
        val score: Int,
        val total: Int,
        val streak: Int,
        val historyId: Long
    ) : GameUiEffect
}

@HiltViewModel
class GameViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
    private val historyRepository: QuizHistoryRepository,
    private val dispatchers: DispatcherProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<GameUiEffect>()
    val uiEffect: SharedFlow<GameUiEffect> = _uiEffect.asSharedFlow()

    private val scoringEngine = ScoringEngine()
    private val questionGenerator = QuestionGenerator()

    private val modeStr: String = savedStateHandle["mode"] ?: GameMode.FLAG_TO_NAME.name
    private val difficultyStr: String = savedStateHandle["difficulty"] ?: Difficulty.MEDIUM.name
    private val regionStr: String = savedStateHandle["region"] ?: Region.ALL.name

    private var activeGameMode: GameMode = GameMode.fromName(modeStr)
    private var activeDifficulty: Difficulty = Difficulty.fromName(difficultyStr)
    private var activeRegion: Region = Region.fromString(regionStr)

    private var timerJob: Job? = null
    private var sessionStartTimeMillis: Long = System.currentTimeMillis()

    init {
        initializeGame(modeStr, difficultyStr, regionStr)
    }

    // THIS IS WHAT CHANGED: It now accepts the mode, difficulty, and region chosen in the bottom sheet!
    fun initializeGame(
        selectedMode: String = modeStr,
        selectedDifficulty: String = difficultyStr,
        selectedRegion: String = regionStr
    ) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = GameUiState.Loading
            sessionStartTimeMillis = System.currentTimeMillis()

            activeGameMode = GameMode.fromName(selectedMode)
            activeDifficulty = Difficulty.fromName(selectedDifficulty)
            activeRegion = Region.fromString(selectedRegion)

            val countries = countryRepository.getCountries(activeRegion)
            if (countries.size < 4) {
                countryRepository.refreshCountries()
            }

            val readyCountries = countryRepository.getCountries(activeRegion)
            if (readyCountries.size < 4) {
                _uiState.value = GameUiState.Error("Insufficient country data in this region. Try choosing 'All Continents'.")
                return@launch
            }

            // In Timed Blitz, default to 30 rapid questions; in Marathon, play all available!
            val questionCount = if (activeGameMode == GameMode.TIMED_BLITZ) {
                minOf(30, readyCountries.size)
            } else {
                readyCountries.size
            }

            val questions = questionGenerator.generateQuestions(
                allCountries = readyCountries,
                mode = activeGameMode,
                difficulty = activeDifficulty,
                region = activeRegion,
                count = questionCount
            )

            val session = QuizSession(
                questions = questions,
                remainingTimeSeconds = activeGameMode.defaultTimeLimitSeconds
            )

            _uiState.value = GameUiState.Active(
                session = session,
                currentQuestion = questions.first(),
                remainingSeconds = activeGameMode.defaultTimeLimitSeconds,
                totalSeconds = activeGameMode.defaultTimeLimitSeconds,
                timerProgress = 1.0f
            )

            startTimer(activeGameMode.defaultTimeLimitSeconds)
        }
    }

    private fun startTimer(totalDurationSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(dispatchers.default) {
            val totalMillis = totalDurationSeconds * 1000L
            var remainingMillis = totalMillis

            while (isActive && remainingMillis > 0) {
                delay(50L)
                remainingMillis -= 50L

                val progress = (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
                val secondsRemaining = (remainingMillis / 1000L).toInt() + 1

                _uiState.update { state ->
                    if (state is GameUiState.Active && !state.isAnswerRevealed) {
                        state.copy(
                            remainingSeconds = secondsRemaining,
                            timerProgress = progress,
                            isTimeWarning = progress <= 0.25f
                        )
                    } else state
                }
            }

            if (isActive) {
                handleAnswerSubmitted(selectedOptionId = null, wasTimeout = true)
            }
        }
    }

    fun onOptionSelected(optionId: String) {
        val currentState = _uiState.value as? GameUiState.Active ?: return
        if (currentState.isAnswerRevealed) return

        timerJob?.cancel()
        handleAnswerSubmitted(selectedOptionId = optionId, wasTimeout = false)
    }

    private fun handleAnswerSubmitted(selectedOptionId: String?, wasTimeout: Boolean) {
        viewModelScope.launch(dispatchers.default) {
            val currentState = _uiState.value as? GameUiState.Active ?: return@launch
            val question = currentState.currentQuestion
            val isCorrect = selectedOptionId != null && selectedOptionId == question.correctAnswerId

            if (isCorrect) {
                _uiEffect.emit(GameUiEffect.TriggerHapticSuccess)
                _uiEffect.emit(GameUiEffect.PlaySoundCorrect)
            } else {
                _uiEffect.emit(GameUiEffect.TriggerHapticFailure)
                _uiEffect.emit(GameUiEffect.PlaySoundWrong)
            }

            val scoreEarned = scoringEngine.calculateQuestionScore(
                isCorrect = isCorrect,
                remainingSeconds = if (wasTimeout) 0 else currentState.remainingSeconds,
                totalAllocatedSeconds = currentState.totalSeconds,
                difficulty = activeDifficulty,
                currentStreak = currentState.session.currentStreak
            )

            val newStreak = scoringEngine.computeNewStreak(isCorrect, currentState.session.currentStreak)
            val newBestStreak = maxOf(currentState.session.bestStreak, newStreak)
            val newLives = if (isCorrect || activeGameMode == GameMode.TIMED_BLITZ) {
                currentState.session.livesRemaining
            } else {
                currentState.session.livesRemaining - 1
            }

            val record = AnsweredQuestionRecord(
                question = question,
                selectedOptionId = selectedOptionId,
                wasCorrect = isCorrect,
                responseTimeMillis = ((currentState.totalSeconds - currentState.remainingSeconds) * 1000L).coerceAtLeast(0L),
                scoreAwarded = scoreEarned
            )

            launch(dispatchers.io) {
                countryRepository.recordAnswerResult(question.targetCountry.code, isCorrect)
            }

            _uiState.update { state ->
                if (state is GameUiState.Active) {
                    state.copy(
                        selectedOptionId = selectedOptionId,
                        isAnswerRevealed = true,
                        wasLastAnswerCorrect = isCorrect
                    )
                } else state
            }

            delay(1800L)

            val updatedSession = currentState.session.copy(
                score = currentState.session.score + scoreEarned,
                currentStreak = newStreak,
                bestStreak = newBestStreak,
                livesRemaining = newLives,
                answeredQuestions = currentState.session.answeredQuestions + record
            )

            val isLastQuestion = currentState.session.currentQuestionIndex >= currentState.session.questions.lastIndex
            val isOutOfLives = newLives <= 0

            if (isLastQuestion || isOutOfLives) {
                finalizeGame(updatedSession)
            } else {
                advanceToNextQuestion(updatedSession)
            }
        }
    }

    private fun advanceToNextQuestion(updatedSession: QuizSession) {
        val nextIndex = updatedSession.currentQuestionIndex + 1
        val nextQuestion = updatedSession.questions[nextIndex]
        val sessionForNext = updatedSession.copy(currentQuestionIndex = nextIndex)

        _uiState.value = GameUiState.Active(
            session = sessionForNext,
            currentQuestion = nextQuestion,
            selectedOptionId = null,
            isAnswerRevealed = false,
            wasLastAnswerCorrect = null,
            remainingSeconds = activeGameMode.defaultTimeLimitSeconds,
            totalSeconds = activeGameMode.defaultTimeLimitSeconds,
            timerProgress = 1.0f,
            isFiftyFiftyActive = false,
            isTimeWarning = false
        )

        startTimer(activeGameMode.defaultTimeLimitSeconds)
    }

    fun onUseFiftyFiftyHint() {
        val state = _uiState.value as? GameUiState.Active ?: return
        if (state.isAnswerRevealed || state.session.hintsFiftyFiftyRemaining <= 0 || state.isFiftyFiftyActive) return

        val question = state.currentQuestion
        val incorrectOptions = question.options.filter { !it.isCorrect }.shuffled()
        val optionsToHide = incorrectOptions.take(2).map { it.id }.toSet()

        val updatedOptions = question.options.map { option ->
            if (optionsToHide.contains(option.id)) option.copy(isHiddenByHint = true) else option
        }

        val updatedQuestion = question.copy(options = updatedOptions)
        val updatedSession = state.session.copy(
            hintsFiftyFiftyRemaining = state.session.hintsFiftyFiftyRemaining - 1
        )

        _uiState.value = state.copy(
            session = updatedSession,
            currentQuestion = updatedQuestion,
            isFiftyFiftyActive = true
        )
    }

    fun onUseSkipHint() {
        val state = _uiState.value as? GameUiState.Active ?: return
        if (state.isAnswerRevealed || state.session.hintsSkipRemaining <= 0) return

        timerJob?.cancel()

        val updatedSession = state.session.copy(
            hintsSkipRemaining = state.session.hintsSkipRemaining - 1
        )

        if (state.session.currentQuestionIndex >= state.session.questions.lastIndex) {
            finalizeGame(updatedSession)
        } else {
            advanceToNextQuestion(updatedSession)
        }
    }

    private fun finalizeGame(session: QuizSession) {
        viewModelScope.launch(dispatchers.io) {
            timerJob?.cancel()
            val timeSpentSeconds = ((System.currentTimeMillis() - sessionStartTimeMillis) / 1000L).toInt()

            val missedCodes = session.answeredQuestions
                .filter { !it.wasCorrect }
                .map { it.question.targetCountry.code }

            val history = QuizHistory(
                timestamp = System.currentTimeMillis(),
                gameMode = activeGameMode.name,
                difficulty = activeDifficulty.name,
                score = session.score,
                totalQuestions = session.questions.size,
                highestStreak = session.bestStreak,
                timeSpentSeconds = timeSpentSeconds,
                missedCountryCodes = missedCodes
            )

            val historyId = historyRepository.saveQuizResult(history)
            Timber.i("Saved quiz result with ID: %d", historyId)

            _uiState.value = GameUiState.Completed(
                finalScore = session.score,
                totalQuestions = session.questions.size,
                bestStreak = session.bestStreak,
                historyId = historyId
            )

            _uiEffect.emit(
                GameUiEffect.NavigateToResults(
                    score = session.score,
                    total = session.questions.size,
                    streak = session.bestStreak,
                    historyId = historyId
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}