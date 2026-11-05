package com.flagquiz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.flagquiz.app.data.local.entity.CountryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {

    @Query("SELECT * FROM countries ORDER BY common_name ASC")
    fun getAllCountriesStream(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries ORDER BY common_name ASC")
    suspend fun getAllCountries(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE region = :region ORDER BY common_name ASC")
    fun getCountriesByRegionStream(region: String): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE region = :region ORDER BY common_name ASC")
    suspend fun getCountriesByRegion(region: String): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE code = :code LIMIT 1")
    suspend fun getCountryByCode(code: String): CountryEntity?

    @Query("SELECT * FROM countries WHERE is_favorite = 1 ORDER BY common_name ASC")
    fun getFavoriteCountriesStream(): Flow<List<CountryEntity>>

    @Query("SELECT COUNT(*) FROM countries")
    suspend fun getCountryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Query("UPDATE countries SET is_favorite = :isFavorite WHERE code = :code")
    suspend fun updateFavoriteStatus(code: String, isFavorite: Boolean)

    @Query("""
        UPDATE countries 
        SET times_answered = times_answered + 1,
            times_correct = times_correct + (CASE WHEN :wasCorrect = 1 THEN 1 ELSE 0 END)
        WHERE code = :code
    """)
    suspend fun recordCountryAnswer(code: String, wasCorrect: Boolean)

    @Query("SELECT * FROM countries WHERE common_name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' ORDER BY common_name ASC")
    fun searchCountries(query: String): Flow<List<CountryEntity>>

    @Transaction
    suspend fun clearAndInsert(countries: List<CountryEntity>) {
        clearAllCountries()
        insertCountries(countries)
    }

    @Query("DELETE FROM countries")
    suspend fun clearAllCountries()
}