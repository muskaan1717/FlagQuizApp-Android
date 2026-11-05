package com.flagquiz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flagquiz.app.data.local.entity.QuizHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizHistoryDao {

    @Query("SELECT * FROM quiz_history ORDER BY timestamp DESC")
    fun getAllHistoryStream(): Flow<List<QuizHistoryEntity>>

    @Query("SELECT * FROM quiz_history WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: Long): QuizHistoryEntity?

    @Query("SELECT * FROM quiz_history ORDER BY score DESC LIMIT :limit")
    fun getTopScoresStream(limit: Int = 10): Flow<List<QuizHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: QuizHistoryEntity): Long

    @Query("DELETE FROM quiz_history")
    suspend fun clearHistory()
}