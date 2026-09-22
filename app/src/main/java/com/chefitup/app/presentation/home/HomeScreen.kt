package com.chefitup.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chefitup.app.R
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.presentation.components.ChefItUpLoading
import com.chefitup.app.presentation.components.ErrorState
import com.chefitup.app.presentation.components.MealCard
import com.chefitup.app.presentation.components.OfflineBanner
import com.chefitup.app.presentation.components.RecipeCard
import com.chefitup.app.presentation.components.SectionHeader
import com.chefitup.app.presentation.components.chefLevelLabel
import com.chefitup.app.presentation.theme.ChefEnterFadeSlide
import com.chefitup.app.presentation.theme.FlameDeep
import com.chefitup.app.presentation.theme.FlameOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSearch: () -> Unit,
    onOpenCookWithIngredients: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    onOpenMealPlanner: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenProfile: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.isOffline) {
            OfflineBanner()
        }
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> ChefItUpLoading()
                uiState.errorResId != null && uiState.recommended.isEmpty() -> {
                    ErrorState(
                        message = uiState.errorMessage
                            ?: stringResource(uiState.errorResId!!),
                        onRetry = viewModel::refresh
                    )
                }
                else -> {
                    HomeContent(
                        uiState = uiState,
                        onOpenSearch = onOpenSearch,
                        onOpenCookWithIngredients = onOpenCookWithIngredients,
                        onOpenRecipe = onOpenRecipe,
                        onOpenMealPlanner = onOpenMealPlanner,
                        onOpenSaved = onOpenSaved,
                        onOpenProfile = onOpenProfile
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onOpenSearch: () -> Unit,
    onOpenCookWithIngredients: () -> Unit,
    onOpenMealPlanner: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRecipe: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
    ) {
        AnimatedVisibility(visible = true, enter = ChefEnterFadeSlide) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = FlameOrange
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (uiState.displayName.isBlank()) {
                        stringResource(R.string.home_welcome_guest)
                    } else {
                        stringResource(R.string.home_welcome, uiState.displayName)
                    },
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onOpenProfile)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = chefLevelLabel(uiState.gamification.level),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                when (uiState.spoonacularConfigured) {
                    false -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.recipes_source_mock),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    true -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.recipes_source_live),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    null -> Unit
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onOpenSearch)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.home_search_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(listOf(FlameOrange, FlameDeep))
                )
                .clickable(onClick = onOpenCookWithIngredients)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.home_cook_with_what_i_have_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(R.string.home_cook_with_what_i_have_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        RecipeSection(
            title = stringResource(R.string.home_section_recommended),
            recipes = uiState.recommended,
            onOpenRecipe = onOpenRecipe
        )
        RecipeSection(
            title = stringResource(R.string.home_section_quick),
            recipes = uiState.quick,
            onOpenRecipe = onOpenRecipe
        )
        RecipeSection(
            title = stringResource(R.string.home_section_popular),
            recipes = uiState.popular,
            onOpenRecipe = onOpenRecipe
        )
        if (uiState.favourites.isNotEmpty()) {
            RecipeSection(
                title = stringResource(R.string.home_section_favourites),
                recipes = uiState.favourites,
                onOpenRecipe = onOpenRecipe,
                onSeeAll = onOpenSaved
            )
        }
        if (uiState.continueCooking.isNotEmpty()) {
            RecipeSection(
                title = stringResource(R.string.home_section_continue),
                recipes = uiState.continueCooking,
                onOpenRecipe = onOpenRecipe
            )
        }
        if (uiState.mealPlanPreview.isNotEmpty()) {
            SectionHeader(
                title = stringResource(R.string.home_section_meal_plan),
                onAction = onOpenMealPlanner
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.mealPlanPreview.forEach { entry ->
                    MealCard(
                        entry = entry,
                        onClick = { onOpenRecipe(entry.recipeId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeSection(
    title: String,
    recipes: List<RecipeSummary>,
    onOpenRecipe: (String) -> Unit,
    onSeeAll: (() -> Unit)? = null
) {
    if (recipes.isEmpty()) return
    SectionHeader(title = title, onAction = onSeeAll)
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        recipes.forEach { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onOpenRecipe(recipe.id) },
                compact = true
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}
