package com.chefitup.app.presentation.cook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.chefitup.app.presentation.components.ChefItUpButton
import com.chefitup.app.presentation.components.EmptyState
import com.chefitup.app.presentation.components.ErrorState
import com.chefitup.app.presentation.components.IngredientChip
import com.chefitup.app.presentation.components.ChefItUpLoading
import com.chefitup.app.presentation.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CookWithIngredientsScreen(
    onBack: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    viewModel: CookWithIngredientsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cook_ingredients_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::clearAll) {
                        Text(stringResource(R.string.cook_ingredients_clear))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.draft,
                    onValueChange = viewModel::onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.cook_ingredients_hint)) },
                    singleLine = true
                )
                TextButton(onClick = viewModel::addIngredient) {
                    Text(stringResource(R.string.cook_ingredients_add))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.ingredients.forEach { ingredient ->
                    IngredientChip(
                        label = ingredient,
                        onRemove = { viewModel.removeIngredient(ingredient) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            ChefItUpButton(
                text = stringResource(R.string.cook_ingredients_search),
                onClick = viewModel::search,
                enabled = uiState.ingredients.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                uiState.isLoading -> ChefItUpLoading()
                uiState.errorResId != null -> {
                    ErrorState(
                        message = uiState.errorMessage ?: stringResource(uiState.errorResId!!),
                        onRetry = viewModel::search
                    )
                }
                !uiState.hasSearched && uiState.ingredients.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.cook_ingredients_empty_title),
                        body = stringResource(R.string.cook_ingredients_empty_body)
                    )
                }
                uiState.hasSearched && uiState.results.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.search_empty_title),
                        body = stringResource(R.string.cook_ingredients_no_results)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
}
