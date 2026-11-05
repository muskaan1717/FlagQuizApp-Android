
package com.flagquiz.app.domain.model

enum class Difficulty(
    val displayName: String,
    val scoreMultiplier: Float,
    val minPopulationThreshold: Long
) {
    EASY("Easy", 1.0f, 15_000_000L),
    MEDIUM("Medium", 1.5f, 3_000_000L),
    HARD("Hard", 2.0f, 0L);

    companion object {
        fun fromName(name: String): Difficulty {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MEDIUM
        }
    }
}