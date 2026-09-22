package com.chefitup.app.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chefitup.app.R
import com.chefitup.app.presentation.components.EmptyState
import com.chefitup.app.presentation.components.ErrorState
import com.chefitup.app.presentation.components.FilterChipRow
import com.chefitup.app.presentation.components.ChefItUpLoading
import com.chefitup.app.presentation.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenRecipe: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.search_title)) },
            actions = {
                IconButton(onClick = viewModel::toggleFilters) {
                    Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.search_filters))
                }
            }
        )
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        if (uiState.showFilters) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.search_cuisine),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            FilterChipRow(
                options = listOf("African", "Italian", "Indian", "Chinese", "Mexican"),
                selected = uiState.cuisine,
                onSelected = viewModel::setCuisine
            )
            Text(
                text = stringResource(R.string.search_diet),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            FilterChipRow(
                options = listOf("vegetarian", "vegan", "gluten free", "dairy free"),
                selected = uiState.diet,
                onSelected = viewModel::setDiet
            )
            Text(
                text = stringResource(R.string.search_meal_type),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            FilterChipRow(
                options = listOf("breakfast", "lunch", "dinner", "dessert", "snack"),
                selected = uiState.mealType,
                onSelected = viewModel::setMealType
            )
            Text(
                text = stringResource(R.string.search_max_time),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            val timeLabels = listOf(15, 30, 45, 60)
            FilterChipRow(
                options = timeLabels.map { "$it min" },
                selected = uiState.maxReadyTime?.let { "$it min" },
                onSelected = { label ->
                    viewModel.setMaxReadyTime(label?.substringBefore(' ')?.toIntOrNull())
                }
            )
            TextButton(
                onClick = viewModel::clearFilters,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(stringResource(R.string.filter_clear))
            }
        }

        if (!uiState.hasSearched && uiState.recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.search_recent),
                    style = MaterialTheme.typography.titleSmall
                )
                TextButton(onClick = viewModel::clearHistory) {
                    Text(stringResource(R.string.search_clear_history))
                }
            }
            uiState.recentSearches.forEach { query ->
                Text(
                    text = query,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onRecentClick(query) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        when {
            uiState.isLoading -> ChefItUpLoading()
            uiState.errorResId != null -> {
                ErrorState(
                    message = uiState.errorMessage ?: stringResource(uiState.errorResId!!),
                    onRetry = viewModel::retry
                )
            }
            uiState.hasSearched && uiState.results.isEmpty() -> {
                EmptyState(
                    title = stringResource(R.string.search_empty_title),
                    body = stringResource(R.string.search_empty_body)
                )
            }
            uiState.results.isNotEmpty() -> {
                Text(
                    text = stringResource(R.string.search_results_count, uiState.results.size),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.results, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onOpenRecipe(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}
