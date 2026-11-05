
package com.flagquiz.app.domain.engine

import com.flagquiz.app.domain.model.Difficulty
import kotlin.math.max
import kotlin.math.roundToInt

class ScoringEngine {
    companion object {
        const val BASE_POINTS_PER_QUESTION = 100
        const val MAX_TIME_BONUS_POINTS = 50
    }

    fun calculateQuestionScore(
        isCorrect: Boolean,
        remainingSeconds: Int,
        totalAllocatedSeconds: Int,
        difficulty: Difficulty,
        currentStreak: Int
    ): Int {
        if (!isCorrect) return 0

        val timeFraction = if (totalAllocatedSeconds > 0) {
            (remainingSeconds.toFloat() / totalAllocatedSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f
        val timeBonus = (timeFraction * MAX_TIME_BONUS_POINTS).roundToInt()
        val streakMultiplier = calculateStreakMultiplier(currentStreak)
        val diffMultiplier = difficulty.scoreMultiplier

        val rawScore = (BASE_POINTS_PER_QUESTION + timeBonus) * diffMultiplier * streakMultiplier
        return max(0, rawScore.roundToInt())
    }

    fun computeNewStreak(isCorrect: Boolean, previousStreak: Int): Int {
        return if (isCorrect) previousStreak + 1 else 0
    }

    fun calculateStreakMultiplier(streak: Int): Float {
        return when {
            streak >= 7 -> 2.0f
            streak >= 5 -> 1.5f
            streak >= 3 -> 1.25f
            else -> 1.0f
        }
    }
}