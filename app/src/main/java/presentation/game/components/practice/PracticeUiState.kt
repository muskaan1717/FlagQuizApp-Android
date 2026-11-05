package com.flagquiz.app.presentation.practice

import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.Region

sealed interface PracticeUiState {
    data object Loading : PracticeUiState

    data class Success(
        val countries: List<Country>,
        val totalCount: Int,
        val searchQuery: String,
        val selectedRegion: Region,
        val showFavoritesOnly: Boolean
    ) : PracticeUiState

    data class Error(val message: String) : PracticeUiState
}