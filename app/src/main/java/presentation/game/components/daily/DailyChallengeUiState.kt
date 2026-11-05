package com.flagquiz.app.presentation.daily

import com.flagquiz.app.domain.model.Question
import com.flagquiz.app.domain.model.QuizSession

sealed interface DailyChallengeUiState {
    data object Loading : DailyChallengeUiState

    data class Active(
        val session: QuizSession,
        val currentQuestion: Question,
        val selectedOptionId: String? = null,
        val isAnswerRevealed: Boolean = false,
        val remainingSeconds: Int = 15,
        val timerProgress: Float = 1.0f,
        val streak: Int = 0
    ) : DailyChallengeUiState

    data class AlreadyCompleted(
        val streak: Int,
        val completionDate: String,
        val hoursUntilNextChallenge: Int
    ) : DailyChallengeUiState

    data class CompletedToday(
        val finalScore: Int,
        val totalQuestions: Int,
        val newStreak: Int
    ) : DailyChallengeUiState

    data class Error(val message: String) : DailyChallengeUiState
}