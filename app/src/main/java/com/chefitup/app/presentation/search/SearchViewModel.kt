package com.chefitup.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.RecipeFilters
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val cuisine: String? = null,
    val diet: String? = null,
    val mealType: String? = null,
    val maxReadyTime: Int? = null,
    val results: List<RecipeSummary> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorResId: Int? = null,
    val errorMessage: String? = null,
    val showFilters: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            recipeRepository.observeSearchHistory().collect { history ->
                _uiState.update { it.copy(recentSearches = history) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        debounceSearch()
    }

    fun onRecentClick(query: String) {
        _uiState.update { it.copy(query = query) }
        performSearch()
    }

    fun setCuisine(value: String?) {
        _uiState.update { it.copy(cuisine = value) }
        debounceSearch()
    }

    fun setDiet(value: String?) {
        _uiState.update { it.copy(diet = value) }
        debounceSearch()
    }

    fun setMealType(value: String?) {
        _uiState.update { it.copy(mealType = value) }
        debounceSearch()
    }

    fun setMaxReadyTime(value: Int?) {
        _uiState.update { it.copy(maxReadyTime = value) }
        debounceSearch()
    }

    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(cuisine = null, diet = null, mealType = null, maxReadyTime = null)
        }
        debounceSearch()
    }

    fun clearHistory() {
        viewModelScope.launch { recipeRepository.clearSearchHistory() }
    }

    fun retry() = performSearch()

    private fun debounceSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch()
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.query.isBlank() &&
                state.cuisine == null &&
                state.diet == null &&
                state.mealType == null &&
                state.maxReadyTime == null
            ) {
                _uiState.update {
                    it.copy(results = emptyList(), hasSearched = false, isLoading = false)
                }
                return@launch
            }
            _uiState.update {
                it.copy(isLoading = true, errorResId = null, errorMessage = null, hasSearched = true)
            }
            val filters = RecipeFilters(
                query = state.query.trim(),
                cuisine = state.cuisine,
                diet = state.diet,
                type = state.mealType,
                maxReadyTime = state.maxReadyTime
            )
            when (val outcome = recipeRepository.search(filters)) {
                is ApiOutcome.Success -> {
                    _uiState.update {
                        it.copy(results = outcome.data, isLoading = false)
                    }
                }
                is ApiOutcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorResId = outcome.messageResId ?: R.string.error_generic,
                            errorMessage = outcome.message
                        )
                    }
                }
            }
        }
    }
}
