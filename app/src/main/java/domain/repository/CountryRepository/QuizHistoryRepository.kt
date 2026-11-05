
package com.flagquiz.app.domain.repository

import com.flagquiz.app.domain.model.QuizHistory
import kotlinx.coroutines.flow.Flow

interface QuizHistoryRepository {
    fun getHistoryStream(): Flow<List<QuizHistory>>
    fun getTopScoresStream(limit: Int = 10): Flow<List<QuizHistory>>
    suspend fun getHistoryById(id: Long): QuizHistory?
    suspend fun saveQuizResult(history: QuizHistory): Long
    suspend fun clearAllHistory()
}