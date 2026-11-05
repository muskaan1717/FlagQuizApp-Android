
package com.flagquiz.app.domain.model

enum class GameMode(
    val title: String,
    val description: String,
    val defaultQuestionCount: Int,
    val defaultTimeLimitSeconds: Int
) {
    FLAG_TO_NAME("Flag to Country", "Identify the country for this flag.", 10, 15),
    NAME_TO_FLAG("Country to Flag", "Select the correct flag matching the name.", 10, 15),
    CAPITAL_TO_COUNTRY("Capital Quiz", "Match the capital city to its nation.", 10, 15),
    POPULATION_HIGHER_LOWER("Population Showdown", "Higher or lower population?", 10, 12),
    TIMED_BLITZ("Timed Blitz", "Score as many correct answers as possible in 60s.", 30, 60);

    companion object {
        fun fromName(name: String): GameMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: FLAG_TO_NAME
        }
    }
}