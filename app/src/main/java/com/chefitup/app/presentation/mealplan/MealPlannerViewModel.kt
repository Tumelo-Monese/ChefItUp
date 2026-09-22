package com.chefitup.app.presentation.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.MealType
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.model.ShoppingListItem
import com.chefitup.app.domain.repository.MealPlanRepository
import com.chefitup.app.domain.repository.RecipeRepository
import com.chefitup.app.domain.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject

data class MealPlannerUiState(
    val weekStartEpochDay: Long = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .toEpochDay(),
    val selectedDay: Int = LocalDate.now().dayOfWeek.value,
    val entries: List<MealPlanEntry> = emptyList(),
    val showAddDialog: Boolean = false,
    val addMealType: MealType = MealType.DINNER,
    val recipeSuggestions: List<RecipeSummary> = emptyList(),
    val isGenerating: Boolean = false,
    val errorResId: Int? = null
)

sealed class MealPlannerEvent {
    data object ShoppingGenerated : MealPlannerEvent()
}

@HiltViewModel
class MealPlannerViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlannerUiState())
    val uiState: StateFlow<MealPlannerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MealPlannerEvent>()
    val events: SharedFlow<MealPlannerEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val weekStart = _uiState.value.weekStartEpochDay
            mealPlanRepository.observeWeek(weekStart).collect { entries ->
                _uiState.update { it.copy(entries = entries) }
            }
        }
    }

    fun selectDay(dayOfWeek: Int) {
        _uiState.update { it.copy(selectedDay = dayOfWeek) }
    }

    fun showAddDialog(show: Boolean, mealType: MealType = MealType.DINNER) {
        _uiState.update { it.copy(showAddDialog = show, addMealType = mealType) }
        if (show) {
            viewModelScope.launch {
                when (val outcome = recipeRepository.getHomeFeed()) {
                    is ApiOutcome.Success -> {
                        _uiState.update { it.copy(recipeSuggestions = outcome.data.take(20)) }
                    }
                    is ApiOutcome.Failure -> {
                        _uiState.update {
                            it.copy(errorResId = outcome.messageResId ?: R.string.error_generic)
                        }
                    }
                }
            }
        }
    }

    fun addMeal(recipe: RecipeSummary) {
        val state = _uiState.value
        viewModelScope.launch {
            mealPlanRepository.upsert(
                MealPlanEntry(
                    id = UUID.randomUUID().toString(),
                    recipeId = recipe.id,
                    recipeTitle = recipe.title,
                    recipeImageUrl = recipe.imageUrl,
                    weekStartEpochDay = state.weekStartEpochDay,
                    dayOfWeek = state.selectedDay,
                    mealType = state.addMealType,
                    servings = recipe.servings.coerceAtLeast(1)
                )
            )
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun removeMeal(entryId: String) {
        viewModelScope.launch { mealPlanRepository.delete(entryId) }
    }

    fun generateShoppingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val entries = _uiState.value.entries
            val ingredientsByRecipe = mutableMapOf<String, List<ShoppingListItem>>()
            entries.map { it.recipeId }.distinct().forEach { recipeId ->
                when (val detail = recipeRepository.getDetail(recipeId)) {
                    is ApiOutcome.Success -> {
                        ingredientsByRecipe[recipeId] = detail.data.ingredients.map { ing ->
                            ShoppingListItem(
                                id = UUID.randomUUID().toString(),
                                name = ing.name,
                                amount = ing.amount,
                                unit = ing.unit,
                                category = "Pantry",
                                recipeId = recipeId
                            )
                        }
                    }
                    is ApiOutcome.Failure -> Unit
                }
            }
            shoppingListRepository.generateFromMealPlan(entries, ingredientsByRecipe)
            _uiState.update { it.copy(isGenerating = false) }
            _events.emit(MealPlannerEvent.ShoppingGenerated)
        }
    }

    fun entriesForSelectedDay(): Map<MealType, List<MealPlanEntry>> {
        val dayEntries = _uiState.value.entries.filter { it.dayOfWeek == _uiState.value.selectedDay }
        return MealType.entries.associateWith { type ->
            dayEntries.filter { it.mealType == type }
        }
    }
}
