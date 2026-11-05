
package com.flagquiz.app.domain.model

data class QuizHistory(
    val id: Long = 0L,
    val timestamp: Long,
    val gameMode: String,
    val difficulty: String,
    val score: Int,
    val totalQuestions: Int,
    val highestStreak: Int,
    val timeSpentSeconds: Int,
    val missedCountryCodes: List<String>
) {
    val accuracyPercentage: Float
        get() = if (totalQuestions > 0) (score.toFloat() / totalQuestions.toFloat()) * 100f else 0f
}