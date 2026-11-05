package com.flagquiz.app.presentation.game

import com.flagquiz.app.domain.model.Question
import com.flagquiz.app.domain.model.QuizSession

sealed interface GameUiState {
    data object Loading : GameUiState

    data class Active(
        val session: QuizSession,
        val currentQuestion: Question,
        val selectedOptionId: String? = null,
        val isAnswerRevealed: Boolean = false,
        val wasLastAnswerCorrect: Boolean? = null,
        val remainingSeconds: Int = 15,
        val totalSeconds: Int = 15,
        val timerProgress: Float = 1.0f,
        val isFiftyFiftyActive: Boolean = false,
        val isTimeWarning: Boolean = false
    ) : GameUiState

    data class Completed(
        val finalScore: Int,
        val totalQuestions: Int,
        val bestStreak: Int,
        val historyId: Long
    ) : GameUiState

    data class Error(val message: String) : GameUiState
}