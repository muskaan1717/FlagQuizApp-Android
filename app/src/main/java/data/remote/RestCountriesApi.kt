package com.flagquiz.app.data.remote

import com.flagquiz.app.data.remote.dto.CountryDto
import retrofit2.http.GET

interface RestCountriesApi {

    // Comma characters are preserved literally so the server sends all 250 nations!
    @GET("v3.1/all?fields=name,cca2,cca3,flags,capital,region,population")
    suspend fun getAllCountries(): List<CountryDto>

    companion object {
        const val BASE_URL = "https://restcountries.com/"
    }
}