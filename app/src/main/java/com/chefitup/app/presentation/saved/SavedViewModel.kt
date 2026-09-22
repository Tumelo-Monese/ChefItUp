package com.chefitup.app.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.repository.FavouriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SavedFilter {
    ALL, BREAKFAST, LUNCH, DINNER, DESSERTS
}

data class SavedUiState(
    val filter: SavedFilter = SavedFilter.ALL,
    val allFavourites: List<RecipeSummary> = emptyList(),
    val filtered: List<RecipeSummary> = emptyList()
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favouriteRepository.observeFavourites().collect { favs ->
                _uiState.update { state ->
                    state.copy(
                        allFavourites = favs,
                        filtered = applyFilter(favs, state.filter)
                    )
                }
            }
        }
    }

    fun setFilter(filter: SavedFilter) {
        _uiState.update {
            it.copy(filter = filter, filtered = applyFilter(it.allFavourites, filter))
        }
    }

    private fun applyFilter(list: List<RecipeSummary>, filter: SavedFilter): List<RecipeSummary> {
        if (filter == SavedFilter.ALL) return list
        val keywords = when (filter) {
            SavedFilter.BREAKFAST -> listOf("breakfast", "brunch")
            SavedFilter.LUNCH -> listOf("lunch", "main course", "salad")
            SavedFilter.DINNER -> listOf("dinner", "main course", "supper")
            SavedFilter.DESSERTS -> listOf("dessert", "sweet", "cake", "cookie")
            SavedFilter.ALL -> emptyList()
        }
        return list.filter { recipe ->
            val types = recipe.dishTypes.map { it.lowercase() }
            val title = recipe.title.lowercase()
            keywords.any { key -> types.any { it.contains(key) } || title.contains(key) }
        }
    }
}
