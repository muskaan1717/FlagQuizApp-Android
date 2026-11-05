package com.flagquiz.app.domain.engine

import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.Difficulty
import com.flagquiz.app.domain.model.GameMode
import com.flagquiz.app.domain.model.Region
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuestionGeneratorTest {

    private lateinit var mockCountries: List<Country>

    @Before
    fun setUp() {
        mockCountries = listOf(
            Country("FR", "FRA", "France", "French Republic", "Paris", Region.EUROPE, 67000000L, "", ""),
            Country("DE", "DEU", "Germany", "Federal Republic of Germany", "Berlin", Region.EUROPE, 83000000L, "", ""),
            Country("IT", "ITA", "Italy", "Italian Republic", "Rome", Region.EUROPE, 59000000L, "", ""),
            Country("ES", "ESP", "Spain", "Kingdom of Spain", "Madrid", Region.EUROPE, 47000000L, "", ""),
            Country("JP", "JPN", "Japan", "State of Japan", "Tokyo", Region.ASIA, 125000000L, "", ""),
            Country("KR", "KOR", "South Korea", "Republic of Korea", "Seoul", Region.ASIA, 51000000L, "", "")
        )
    }

    @Test
    fun eachQuestionHasExactlyFourDistinctChoicesWithOneCorrectAnswer() {
        val generator = QuestionGenerator(seed = 42L)
        val questions = generator.generateQuestions(
            allCountries = mockCountries,
            mode = GameMode.FLAG_TO_NAME,
            count = 4
        )

        for (q in questions) {
            assertEquals(4, q.options.size)
            val correctCount = q.options.count { it.isCorrect }
            assertEquals("Exactly one option must be flagged correct", 1, correctCount)
            assertEquals(q.targetCountry.code, q.correctAnswerId)
        }
    }

    @Test
    fun deterministicSeedGeneratesIdenticalQuestionSequence() {
        val seed = 9999L
        val gen1 = QuestionGenerator(seed = seed)
        val gen2 = QuestionGenerator(seed = seed)

        val q1 = gen1.generateQuestions(mockCountries, GameMode.FLAG_TO_NAME, count = 3)
        val q2 = gen2.generateQuestions(mockCountries, GameMode.FLAG_TO_NAME, count = 3)

        for (i in 0 until 3) {
            assertEquals(q1[i].targetCountry.code, q2[i].targetCountry.code)
            assertEquals(q1[i].correctAnswerId, q2[i].correctAnswerId)
        }
    }
}