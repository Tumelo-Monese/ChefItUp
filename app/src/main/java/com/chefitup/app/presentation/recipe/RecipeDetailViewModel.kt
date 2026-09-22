package com.chefitup.app.presentation.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.Ingredient
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.MealType
import com.chefitup.app.domain.model.RecipeDetail
import com.chefitup.app.domain.model.ShoppingListItem
import com.chefitup.app.domain.repository.FavouriteRepository
import com.chefitup.app.domain.repository.GamificationRepository
import com.chefitup.app.domain.repository.MealPlanRepository
import com.chefitup.app.domain.repository.RecipeRepository
import com.chefitup.app.domain.repository.ShoppingListRepository
import com.chefitup.app.presentation.navigation.Screen
import com.chefitup.app.util.IngredientScaler
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

data class RecipeDetailUiState(
    val recipe: RecipeDetail? = null,
    val servings: Int = 1,
    val scaledIngredients: List<Ingredient> = emptyList(),
    val isFavourite: Boolean = false,
    val isLoading: Boolean = true,
    val errorResId: Int? = null,
    val errorMessage: String? = null,
    val showMealPlanDialog: Boolean = false,
    val selectedDayOfWeek: Int = LocalDate.now().dayOfWeek.value,
    val selectedMealType: MealType = MealType.DINNER
)

sealed class RecipeDetailEvent {
    data object AddedToShopping : RecipeDetailEvent()
    data object AddedToMealPlan : RecipeDetailEvent()
}

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val favouriteRepository: FavouriteRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle[Screen.RecipeDetail.ARG_RECIPE_ID])

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RecipeDetailEvent>()
    val events: SharedFlow<RecipeDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            favouriteRepository.observeIsFavourite(recipeId).collect { fav ->
                _uiState.update { it.copy(isFavourite = fav) }
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorResId = null, errorMessage = null) }
            when (val outcome = recipeRepository.getDetail(recipeId)) {
                is ApiOutcome.Success -> {
                    val detail = outcome.data
                    gamificationRepository.recordRecipeViewed(detail.id)
                    _uiState.update {
                        it.copy(
                            recipe = detail,
                            servings = detail.servings.coerceAtLeast(1),
                            scaledIngredients = detail.ingredients,
                            isLoading = false
                        )
                    }
                }
                is ApiOutcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorResId = outcome.messageResId ?: R.string.error_not_found,
                            errorMessage = outcome.message
                        )
                    }
                }
            }
        }
    }

    fun increaseServings() = changeServings(_uiState.value.servings + 1)

    fun decreaseServings() {
        if (_uiState.value.servings > 1) changeServings(_uiState.value.servings - 1)
    }

    private fun changeServings(newServings: Int) {
        val recipe = _uiState.value.recipe ?: return
        val scaled = IngredientScaler.scaleIngredients(
            ingredients = recipe.ingredients,
            fromServings = recipe.servings.coerceAtLeast(1),
            toServings = newServings
        )
        _uiState.update { it.copy(servings = newServings, scaledIngredients = scaled) }
    }

    fun toggleFavourite() {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch {
            favouriteRepository.toggleFavourite(recipe.toSummary())
        }
    }

    fun showMealPlanDialog(show: Boolean) {
        _uiState.update { it.copy(showMealPlanDialog = show) }
    }

    fun selectDay(dayOfWeek: Int) {
        _uiState.update { it.copy(selectedDayOfWeek = dayOfWeek) }
    }

    fun selectMealType(type: MealType) {
        _uiState.update { it.copy(selectedMealType = type) }
    }

    fun confirmAddToMealPlan() {
        val recipe = _uiState.value.recipe ?: return
        val state = _uiState.value
        viewModelScope.launch {
            val weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toEpochDay()
            mealPlanRepository.upsert(
                MealPlanEntry(
                    id = UUID.randomUUID().toString(),
                    recipeId = recipe.id,
                    recipeTitle = recipe.title,
                    recipeImageUrl = recipe.imageUrl,
                    weekStartEpochDay = weekStart,
                    dayOfWeek = state.selectedDayOfWeek,
                    mealType = state.selectedMealType,
                    servings = state.servings
                )
            )
            _uiState.update { it.copy(showMealPlanDialog = false) }
            _events.emit(RecipeDetailEvent.AddedToMealPlan)
        }
    }

    fun addIngredientsToShoppingList() {
        val state = _uiState.value
        viewModelScope.launch {
            state.scaledIngredients.forEach { ingredient ->
                shoppingListRepository.upsert(
                    ShoppingListItem(
                        id = UUID.randomUUID().toString(),
                        name = ingredient.name,
                        amount = ingredient.amount,
                        unit = ingredient.unit,
                        category = guessCategory(ingredient.name),
                        recipeId = recipeId,
                        isCustom = false
                    )
                )
            }
            _events.emit(RecipeDetailEvent.AddedToShopping)
        }
    }

    private fun guessCategory(name: String): String {
        val lower = name.lowercase()
        return when {
            listOf("milk", "cheese", "yogurt", "butter", "cream").any { it in lower } -> "Dairy"
            listOf("chicken", "beef", "pork", "lamb", "fish", "meat").any { it in lower } -> "Meat"
            listOf("tomato", "onion", "garlic", "lettuce", "pepper", "lemon", "fruit", "spinach")
                .any { it in lower } -> "Produce"
            else -> "Pantry"
        }
    }
}
