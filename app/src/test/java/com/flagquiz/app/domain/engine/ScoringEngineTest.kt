package com.flagquiz.app.domain.engine

import com.flagquiz.app.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Automated Unit Tests for the Scoring Engine.
 * Verifies that scores, streak multipliers, and time bonuses calculate accurately!
 */
class ScoringEngineTest {

    private lateinit var scoringEngine: ScoringEngine

    @Before
    fun setUp() {
        scoringEngine = ScoringEngine()
    }

    @Test
    fun wrongAnswerYieldsZeroScore() {
        val score = scoringEngine.calculateQuestionScore(
            isCorrect = false,
            remainingSeconds = 15,
            totalAllocatedSeconds = 15,
            difficulty = Difficulty.HARD,
            currentStreak = 10
        )
        assertEquals(0, score)
    }

    @Test
    fun wrongAnswerResetsStreakToZero() {
        val newStreak = scoringEngine.computeNewStreak(isCorrect = false, previousStreak = 8)
        assertEquals(0, newStreak)
    }

    @Test
    fun correctAnswerIncrementsStreakByOne() {
        val newStreak = scoringEngine.computeNewStreak(isCorrect = true, previousStreak = 4)
        assertEquals(5, newStreak)
    }

    @Test
    fun correctAnswerWithFullTimeOnEasyAwardsFullBonus() {
        // Base 100 + Time 50 = 150 points
        val score = scoringEngine.calculateQuestionScore(
            isCorrect = true,
            remainingSeconds = 15,
            totalAllocatedSeconds = 15,
            difficulty = Difficulty.EASY,
            currentStreak = 0
        )
        assertEquals(150, score)
    }

    @Test
    fun hardDifficultyAndStreakMultiplierDoublesPoints() {
        // Hard = 2.0x, Streak 7+ = 2.0x -> 100 * 2.0 * 2.0 = 400 points!
        val score = scoringEngine.calculateQuestionScore(
            isCorrect = true,
            remainingSeconds = 0,
            totalAllocatedSeconds = 15,
            difficulty = Difficulty.HARD,
            currentStreak = 7
        )
        assertEquals(400, score)
    }
}