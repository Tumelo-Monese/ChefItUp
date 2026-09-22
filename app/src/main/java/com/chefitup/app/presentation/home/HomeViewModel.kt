package com.chefitup.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.data.remote.ChefItUpApi
import com.chefitup.app.data.remote.NetworkMonitor
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.model.UserGamification
import com.chefitup.app.domain.repository.AuthRepository
import com.chefitup.app.domain.repository.FavouriteRepository
import com.chefitup.app.domain.repository.GamificationRepository
import com.chefitup.app.domain.repository.MealPlanRepository
import com.chefitup.app.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HomeUiState(
    val displayName: String = "",
    val gamification: UserGamification = UserGamification(),
    val recommended: List<RecipeSummary> = emptyList(),
    val quick: List<RecipeSummary> = emptyList(),
    val popular: List<RecipeSummary> = emptyList(),
    val favourites: List<RecipeSummary> = emptyList(),
    val continueCooking: List<RecipeSummary> = emptyList(),
    val mealPlanPreview: List<MealPlanEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val spoonacularConfigured: Boolean? = null,
    val errorResId: Int? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val favouriteRepository: FavouriteRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val gamificationRepository: GamificationRepository,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor,
    private val api: ChefItUpApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            displayName = authRepository.currentUser?.displayName
                ?.substringBefore(' ')
                ?.ifBlank { null }
                ?: authRepository.currentUser?.email?.substringBefore('@').orEmpty()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            networkMonitor.connectivity.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
        viewModelScope.launch {
            gamificationRepository.observeGamification().collect { g ->
                _uiState.update { it.copy(gamification = g) }
            }
        }
        viewModelScope.launch {
            favouriteRepository.observeFavourites().collect { favs ->
                _uiState.update { it.copy(favourites = favs.take(8)) }
            }
        }
        viewModelScope.launch {
            recipeRepository.observeRecentlyViewed().collect { recent ->
                _uiState.update { it.copy(continueCooking = recent.take(8)) }
            }
        }
        viewModelScope.launch {
            val weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toEpochDay()
            mealPlanRepository.observeWeek(weekStart).collect { entries ->
                _uiState.update { it.copy(mealPlanPreview = entries.take(4)) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.recommended.isEmpty(),
                    isRefreshing = it.recommended.isNotEmpty(),
                    errorResId = null,
                    errorMessage = null
                )
            }
            runCatching { api.health() }
                .onSuccess { health ->
                    _uiState.update { it.copy(spoonacularConfigured = health.spoonacularConfigured) }
                }
            when (val outcome = recipeRepository.getHomeFeed()) {
                is ApiOutcome.Success -> {
                    val all = outcome.data
                    _uiState.update {
                        it.copy(
                            recommended = all.take(10),
                            quick = all.filter { r -> r.readyInMinutes <= 30 }.ifEmpty { all.take(6) },
                            popular = all.sortedByDescending { r -> r.rating }.take(10),
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is ApiOutcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorResId = outcome.messageResId ?: R.string.error_generic,
                            errorMessage = outcome.message
                        )
                    }
                }
            }
        }
    }
}
