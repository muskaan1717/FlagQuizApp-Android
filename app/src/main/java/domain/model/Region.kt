
package com.flagquiz.app.domain.model

/**
 * Region: The 6 Continents of the World.
 * An 'enum' is simply a fixed list of allowed choices.
 */
enum class Region(val apiName: String, val displayName: String) {
    ALL("All", "All Continents"),
    AFRICA("Africa", "Africa"),
    AMERICAS("Americas", "Americas"),
    ASIA("Asia", "Asia"),
    EUROPE("Europe", "Europe"),
    OCEANIA("Oceania", "Oceania"),
    ANTARCTIC("Antarctic", "Antarctica");

    companion object {
        fun fromString(value: String): Region {
            return entries.firstOrNull {
                it.apiName.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
            } ?: ALL
        }
    }
}