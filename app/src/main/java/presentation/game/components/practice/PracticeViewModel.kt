package com.flagquiz.app.presentation.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagquiz.app.core.common.DispatcherProvider
import com.flagquiz.app.domain.model.Country
import com.flagquiz.app.domain.model.Region
import com.flagquiz.app.domain.repository.CountryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PracticeViewModel: Merges 4 reactive streams into one unified filtered list.
 *
 * Why debounce(200L)?
 * As you rapidly type in the search bar, debounce suppresses heavy database queries
 * until your fingers pause for 200 milliseconds, keeping the UI at a buttery 60-120 fps!
 */
@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedRegion = MutableStateFlow(Region.ALL)
    val selectedRegion: StateFlow<Region> = _selectedRegion

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly

    val uiState: StateFlow<PracticeUiState> = combine(
        countryRepository.getCountriesStream(Region.ALL),
        _searchQuery.debounce(200L),
        _selectedRegion,
        _showFavoritesOnly
    ) { allCountries, query, region, favsOnly ->
        if (allCountries.isEmpty()) {
            PracticeUiState.Loading
        } else {
            val filtered = allCountries.filter { country ->
                val matchesQuery = query.isBlank() ||
                        country.commonName.contains(query, ignoreCase = true) ||
                        country.officialName.contains(query, ignoreCase = true) ||
                        country.capital.contains(query, ignoreCase = true) ||
                        country.code.equals(query.trim(), ignoreCase = true)

                val matchesRegion = region == Region.ALL || country.region == region
                val matchesFav = !favsOnly || country.isFavorite

                matchesQuery && matchesRegion && matchesFav
            }

            PracticeUiState.Success(
                countries = filtered,
                totalCount = allCountries.size,
                searchQuery = query,
                selectedRegion = region,
                showFavoritesOnly = favsOnly
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PracticeUiState.Loading
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onRegionSelected(region: Region) {
        _selectedRegion.value = region
    }

    fun onToggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun toggleFavorite(country: Country) {
        viewModelScope.launch(dispatchers.io) {
            countryRepository.toggleFavorite(country.code, !country.isFavorite)
        }
    }
}