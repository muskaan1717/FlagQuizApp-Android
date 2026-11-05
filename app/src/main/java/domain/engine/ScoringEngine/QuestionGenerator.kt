package com.flagquiz.app.domain.engine

import com.flagquiz.app.domain.model.AnswerOption
import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.Difficulty
import com.flagquiz.app.domain.model.GameMode
import com.flagquiz.app.domain.model.Question
import com.flagquiz.app.domain.model.Region
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

class QuestionGenerator(
    private val seed: Long? = null
) {
    private val random: Random = if (seed != null) Random(seed) else Random.Default

    fun generateQuestions(
        allCountries: List<Country>,
        mode: GameMode,
        difficulty: Difficulty = Difficulty.MEDIUM,
        region: Region = Region.ALL,
        count: Int = mode.defaultQuestionCount
    ): List<Question> {
        require(allCountries.size >= 4) { "Requires at least 4 countries." }

        // 1. Regional Filter
        val regionFiltered = if (region == Region.ALL) allCountries else allCountries.filter { it.region == region }
        val baseCandidatePool = if (regionFiltered.size >= 4) regionFiltered else allCountries

        // 2. Difficulty Filter
        val difficultyFiltered = when (difficulty) {
            Difficulty.EASY -> baseCandidatePool.filter { it.isProminent }
            Difficulty.MEDIUM -> baseCandidatePool.filter { it.population >= difficulty.minPopulationThreshold }
            Difficulty.HARD -> baseCandidatePool
        }

        val eligibleTargets = if (difficultyFiltered.size >= count) difficultyFiltered else baseCandidatePool
        val targetCountries = eligibleTargets.shuffled(random).take(minOf(count, eligibleTargets.size))

        // 3. Build specific question type based on selected GameMode
        return targetCountries.mapIndexed { index, targetCountry ->
            buildQuestion(index + 1, targetCountry, allCountries, mode)
        }
    }

    private fun buildQuestion(id: Int, target: Country, fullPool: List<Country>, mode: GameMode): Question {
        return when (mode) {
            GameMode.FLAG_TO_NAME -> buildFlagToNameQuestion(id, target, fullPool, isBlitz = false)
            GameMode.TIMED_BLITZ -> buildFlagToNameQuestion(id, target, fullPool, isBlitz = true)
            GameMode.NAME_TO_FLAG -> buildNameToFlagQuestion(id, target, fullPool)
            GameMode.CAPITAL_TO_COUNTRY -> buildCapitalQuestion(id, target, fullPool)
            GameMode.POPULATION_HIGHER_LOWER -> buildPopulationQuestion(id, target, fullPool)
        }
    }

    private fun buildFlagToNameQuestion(id: Int, target: Country, fullPool: List<Country>, isBlitz: Boolean): Question {
        val distractors = pickRegionalDistractors(target, fullPool, 3)
        val options = (distractors + target).shuffled(random).map {
            AnswerOption(it.code, it.commonName, it.flagPngUrl, it.code == target.code)
        }
        return Question(
            id = id,
            targetCountry = target,
            promptText = if (isBlitz) "⚡ FAST! Which country has this flag?" else "Which country does this flag belong to?",
            promptImageUrl = target.flagPngUrl,
            options = options,
            gameMode = if (isBlitz) GameMode.TIMED_BLITZ else GameMode.FLAG_TO_NAME,
            funFact = "${target.commonName} is in ${target.region.displayName} with capital ${target.capital}."
        )
    }

    private fun buildNameToFlagQuestion(id: Int, target: Country, fullPool: List<Country>): Question {
        val distractors = pickRegionalDistractors(target, fullPool, 3)
        val options = (distractors + target).shuffled(random).map {
            AnswerOption(it.code, it.commonName, it.flagPngUrl, it.code == target.code)
        }
        return Question(
            id = id,
            targetCountry = target,
            promptText = "Select the official flag of ${target.commonName}:",
            promptImageUrl = null, // No prompt image because the OPTIONS are the flag images!
            options = options,
            gameMode = GameMode.NAME_TO_FLAG,
            funFact = "${target.commonName}'s capital city is ${target.capital}."
        )
    }

    private fun buildCapitalQuestion(id: Int, target: Country, fullPool: List<Country>): Question {
        val distractors = pickRegionalDistractors(target, fullPool, 3)
        val options = (distractors + target).shuffled(random).map {
            AnswerOption(it.code, it.commonName, it.flagPngUrl, it.code == target.code)
        }
        return Question(
            id = id,
            targetCountry = target,
            promptText = "${target.capital} is the capital city of which country?",
            promptImageUrl = null,
            options = options,
            gameMode = GameMode.CAPITAL_TO_COUNTRY,
            funFact = "${target.capital} is the capital of ${target.commonName} (${target.region.displayName})."
        )
    }

    private fun buildPopulationQuestion(id: Int, target: Country, fullPool: List<Country>): Question {
        val comparisonPool = fullPool.filter { it.code != target.code && it.population != target.population }
        val comparison = comparisonPool.filter { it.region == target.region }.randomOrNull(random) ?: comparisonPool.random(random)

        val isTargetHigher = target.population > comparison.population
        val compPopFormatted = NumberFormat.getNumberInstance(Locale.US).format(comparison.population)

        val options = listOf(
            AnswerOption("HIGHER", "Higher Population", target.flagPngUrl, isTargetHigher),
            AnswerOption("LOWER", "Lower Population", comparison.flagPngUrl, !isTargetHigher)
        )

        return Question(
            id = id,
            targetCountry = target,
            comparisonCountry = comparison,
            promptText = "Does ${target.commonName} have a HIGHER or LOWER population than ${comparison.commonName} (~$compPopFormatted)?",
            promptImageUrl = target.flagPngUrl,
            options = options,
            gameMode = GameMode.POPULATION_HIGHER_LOWER,
            funFact = "${target.commonName} has ~${NumberFormat.getNumberInstance(Locale.US).format(target.population)} people, while ${comparison.commonName} has ~$compPopFormatted."
        )
    }

    private fun pickRegionalDistractors(target: Country, fullPool: List<Country>, count: Int): List<Country> {
        val sameRegionPool = fullPool.filter { it.region == target.region && it.code != target.code }
        return if (sameRegionPool.size >= count) {
            sameRegionPool.shuffled(random).take(count)
        } else {
            val backfillPool = fullPool.filter { it.code != target.code && !sameRegionPool.contains(it) }
            (sameRegionPool + backfillPool.shuffled(random).take(count - sameRegionPool.size)).shuffled(random)
        }
    }
}