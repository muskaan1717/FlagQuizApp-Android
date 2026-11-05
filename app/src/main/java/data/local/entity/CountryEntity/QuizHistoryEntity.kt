package com.flagquiz.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flagquiz.app.domain.model.QuizHistory

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "game_mode")
    val gameMode: String,

    @ColumnInfo(name = "difficulty")
    val difficulty: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,

    @ColumnInfo(name = "highest_streak")
    val highestStreak: Int,

    @ColumnInfo(name = "time_spent_seconds")
    val timeSpentSeconds: Int,

    @ColumnInfo(name = "missed_country_codes")
    val missedCountryCodes: String
) {
    fun toDomain(): QuizHistory {
        return QuizHistory(
            id = id,
            timestamp = timestamp,
            gameMode = gameMode,
            difficulty = difficulty,
            score = score,
            totalQuestions = totalQuestions,
            highestStreak = highestStreak,
            timeSpentSeconds = timeSpentSeconds,
            missedCountryCodes = if (missedCountryCodes.isBlank()) emptyList() else missedCountryCodes.split(",")
        )
    }
}