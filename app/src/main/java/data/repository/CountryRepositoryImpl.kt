package com.flagquiz.app.data.repository

import com.flagquiz.app.core.common.DispatcherProvider
import com.flagquiz.app.core.common.Resource
import com.flagquiz.app.data.local.CountrySeedData
import com.flagquiz.app.data.local.dao.CountryDao
import com.flagquiz.app.data.local.entity.CountryEntity
import com.flagquiz.app.data.local.preferences.UserPreferencesDataStore
import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.Region
import com.flagquiz.app.domain.repository.CountryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryRepositoryImpl @Inject constructor(
    private val dao: CountryDao,
    private val preferences: UserPreferencesDataStore,
    private val dispatchers: DispatcherProvider
) : CountryRepository {

    override fun getCountriesStream(region: Region): Flow<List<Country>> {
        val flow = if (region == Region.ALL) {
            dao.getAllCountriesStream()
        } else {
            dao.getCountriesByRegionStream(region.apiName)
        }
        return flow.map { entities -> entities.map(CountryEntity::toDomain) }
            .flowOn(dispatchers.io)
    }

    override suspend fun getCountries(region: Region): List<Country> = withContext(dispatchers.io) {
        // Auto-seed if database has less than 100 countries!
        if (dao.getCountryCount() < 100) {
            dao.clearAndInsert(CountrySeedData.countries)
            preferences.recordSyncSuccess()
            Timber.i("Installed full world dataset of %d countries!", CountrySeedData.countries.size)
        }

        val entities = if (region == Region.ALL) {
            dao.getAllCountries()
        } else {
            dao.getCountriesByRegion(region.apiName)
        }
        entities.map(CountryEntity::toDomain)
    }

    override suspend fun getCountryByCode(code: String): Country? = withContext(dispatchers.io) {
        dao.getCountryByCode(code)?.toDomain()
    }

    override suspend fun refreshCountries(): Resource<Unit> = withContext(dispatchers.io) {
        if (dao.getCountryCount() < 100) {
            dao.clearAndInsert(CountrySeedData.countries)
            preferences.recordSyncSuccess()
        }
        Resource.Success(Unit)
    }

    override suspend fun toggleFavorite(code: String, isFavorite: Boolean) = withContext(dispatchers.io) {
        dao.updateFavoriteStatus(code, isFavorite)
    }

    override suspend fun recordAnswerResult(code: String, wasCorrect: Boolean) = withContext(dispatchers.io) {
        dao.recordCountryAnswer(code, wasCorrect)
    }

    override fun searchCountriesStream(query: String): Flow<List<Country>> {
        return dao.searchCountries(query)
            .map { list -> list.map(CountryEntity::toDomain) }
            .flowOn(dispatchers.io)
    }

    override fun getFavoriteCountriesStream(): Flow<List<Country>> {
        return dao.getFavoriteCountriesStream()
            .map { list -> list.map(CountryEntity::toDomain) }
            .flowOn(dispatchers.io)
    }
}