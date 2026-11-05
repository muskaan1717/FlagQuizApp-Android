package com.flagquiz.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    @SerialName("cca2")
    val cca2: String? = null,

    @SerialName("cca3")
    val cca3: String? = null,

    @SerialName("name")
    val name: CountryNameDto? = null,

    @SerialName("capital")
    val capital: List<String>? = null,

    @SerialName("region")
    val region: String? = null,

    @SerialName("population")
    val population: Long? = null,

    @SerialName("flags")
    val flags: FlagsDto? = null
)

@Serializable
data class CountryNameDto(
    @SerialName("common")
    val common: String? = null,

    @SerialName("official")
    val official: String? = null
)

@Serializable
data class FlagsDto(
    @SerialName("png")
    val png: String? = null,

    @SerialName("svg")
    val svg: String? = null
)