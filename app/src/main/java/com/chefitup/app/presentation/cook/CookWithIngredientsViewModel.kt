package com.chefitup.app.presentation.cook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CookWithIngredientsUiState(
    val draft: String = "",
    val ingredients: List<String> = emptyList(),
    val results: List<RecipeSummary> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorResId: Int? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CookWithIngredientsViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookWithIngredientsUiState())
    val uiState: StateFlow<CookWithIngredientsUiState> = _uiState.asStateFlow()

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun addIngredient() {
        val name = _uiState.value.draft.trim()
        if (name.isBlank()) return
        _uiState.update {
            val next = (it.ingredients + name).distinctBy { ing -> ing.lowercase() }
            it.copy(draft = "", ingredients = next)
        }
    }

    fun removeIngredient(name: String) {
        _uiState.update {
            it.copy(ingredients = it.ingredients.filterNot { ing -> ing.equals(name, true) })
        }
    }

    fun clearAll() {
        _uiState.update {
            it.copy(ingredients = emptyList(), results = emptyList(), hasSearched = false)
        }
    }

    fun search() {
        val ingredients = _uiState.value.ingredients
        if (ingredients.isEmpty()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorResId = null, errorMessage = null, hasSearched = true)
            }
            when (val outcome = recipeRepository.findByIngredients(ingredients)) {
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
