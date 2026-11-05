package com.flagquiz.app.di

import com.flagquiz.app.data.remote.RestCountriesApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                // Pretend to be a clean Android browser so carrier firewalls don't block us
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile; FlagQuizMaster/1.0)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRestCountriesApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): RestCountriesApi {
        val contentType = "application/json".toMediaType()
        return retrofit2.Retrofit.Builder()
            .baseUrl(RestCountriesApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(RestCountriesApi::class.java)
    }
}