package com.flagquiz.app.core.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home_screen")

    data object Game : Screen("game_screen?mode={mode}&difficulty={difficulty}&region={region}") {
        fun createRoute(
            mode: String = "FLAG_TO_NAME",
            difficulty: String = "MEDIUM",
            region: String = "ALL"
        ): String {
            return "game_screen?mode=$mode&difficulty=$difficulty&region=$region"
        }
    }

    data object Results : Screen("results_screen/{score}/{totalQuestions}/{streak}/{historyId}") {
        fun createRoute(
            score: Int,
            totalQuestions: Int,
            streak: Int,
            historyId: Long = 0L
        ): String {
            return "results_screen/$score/$totalQuestions/$streak/$historyId"
        }
    }

    data object Stats : Screen("stats_screen")
    data object Practice : Screen("practice_screen")
    data object DailyChallenge : Screen("daily_challenge_screen")
    data object Settings : Screen("settings_screen")
}