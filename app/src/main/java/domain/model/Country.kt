
package com.flagquiz.app.domain.model

/**
 * Country: What makes up a nation in our game?
 * A 'data class' in Kotlin is just a smart container for holding information.
 */
data class Country(
    val code: String,              // e.g. "FR" for France, "JP" for Japan
    val codeAlpha3: String,        // e.g. "FRA", "JPN"
    val commonName: String,        // e.g. "France"
    val officialName: String,      // e.g. "French Republic"
    val capital: String,           // Primary capital city
    val region: Region,            // Continental region
    val population: Long,          // Number of people
    val flagPngUrl: String,        // Picture URL
    val flagSvgUrl: String,        // Vector SVG URL
    val isFavorite: Boolean = false,
    val timesAnswered: Int = 0,
    val timesCorrect: Int = 0
) {
    // A country is "prominent" for Easy mode if population >= 15M or it's a famous nation
    val isProminent: Boolean
        get() = population >= 15_000_000L || prominentCountryCodes.contains(code)

    companion object {
        private val prominentCountryCodes = setOf(
            "CH", "AT", "PT", "GR", "SE", "NO", "DK", "FI", "IE", "NZ", "SG", "IL", "AE", "QA", "IS"
        )
    }
}