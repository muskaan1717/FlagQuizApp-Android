package com.flagquiz.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.Region

@Entity(
    tableName = "countries",
    indices = [
        Index(value = ["region"]),
        Index(value = ["population"])
    ]
)
data class CountryEntity(
    @PrimaryKey
    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "code_alpha3")
    val codeAlpha3: String,

    @ColumnInfo(name = "common_name")
    val commonName: String,

    @ColumnInfo(name = "official_name")
    val officialName: String,

    @ColumnInfo(name = "capital")
    val capital: String,

    @ColumnInfo(name = "region")
    val region: String,

    @ColumnInfo(name = "population")
    val population: Long,

    @ColumnInfo(name = "flag_png_url")
    val flagPngUrl: String,

    @ColumnInfo(name = "flag_svg_url")
    val flagSvgUrl: String,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "times_answered")
    val timesAnswered: Int = 0,

    @ColumnInfo(name = "times_correct")
    val timesCorrect: Int = 0
) {
    fun toDomain(): Country {
        return Country(
            code = code,
            codeAlpha3 = codeAlpha3,
            commonName = commonName,
            officialName = officialName,
            capital = capital,
            region = Region.fromString(region),
            population = population,
            flagPngUrl = flagPngUrl,
            flagSvgUrl = flagSvgUrl,
            isFavorite = isFavorite,
            timesAnswered = timesAnswered,
            timesCorrect = timesCorrect
        )
    }

    companion object {
        fun fromDomain(country: Country): CountryEntity {
            return CountryEntity(
                code = country.code,
                codeAlpha3 = country.codeAlpha3,
                commonName = country.commonName,
                officialName = country.officialName,
                capital = country.capital,
                region = country.region.apiName,
                population = country.population,
                flagPngUrl = country.flagPngUrl,
                flagSvgUrl = country.flagSvgUrl,
                isFavorite = country.isFavorite,
                timesAnswered = country.timesAnswered,
                timesCorrect = country.timesCorrect
            )
        }
    }
}