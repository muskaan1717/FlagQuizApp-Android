
package com.flagquiz.app.domain.repository

import com.flagquiz.app.core.common.Resource
import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.Region
import kotlinx.coroutines.flow.Flow

interface CountryRepository {
    fun getCountriesStream(region: Region = Region.ALL): Flow<List<Country>>
    suspend fun getCountries(region: Region = Region.ALL): List<Country>
    suspend fun getCountryByCode(code: String): Country?
    suspend fun refreshCountries(): Resource<Unit>
    suspend fun toggleFavorite(code: String, isFavorite: Boolean)
    suspend fun recordAnswerResult(code: String, wasCorrect: Boolean)
    fun searchCountriesStream(query: String): Flow<List<Country>>
    fun getFavoriteCountriesStream(): Flow<List<Country>>
}