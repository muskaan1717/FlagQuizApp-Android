package com.flagquiz.app.domain.model

data class AnswerOption(
    val id: String,                 // ISO country code
    val text: String,               // Display name
    val flagUrl: String,            // Image URL
    val isCorrect: Boolean,
    val isHiddenByHint: Boolean = false
)

data class Question(
    val id: Int,
    val targetCountry: Country,
    val promptText: String,
    val promptImageUrl: String? = null,
    val options: List<AnswerOption>,
    val gameMode: GameMode,
    val comparisonCountry: Country? = null,
    val funFact: String
) {
    val correctAnswerId: String get() = options.first { it.isCorrect }.id
    val correctAnswerText: String get() = options.first { it.isCorrect }.text
}