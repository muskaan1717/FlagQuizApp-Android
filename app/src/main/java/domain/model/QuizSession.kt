
package com.flagquiz.app.domain.model

data class AnsweredQuestionRecord(
    val question: Question,
    val selectedOptionId: String?,
    val wasCorrect: Boolean,
    val responseTimeMillis: Long,
    val scoreAwarded: Int
)

data class QuizSession(
    val questions: List<Question>,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val livesRemaining: Int = 3,
    val remainingTimeSeconds: Int = 15,
    val hintsFiftyFiftyRemaining: Int = 1,
    val hintsSkipRemaining: Int = 1,
    val answeredQuestions: List<AnsweredQuestionRecord> = emptyList(),
    val isGameOver: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)
    val totalQuestions: Int get() = questions.size
}