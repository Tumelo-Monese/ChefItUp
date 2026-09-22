package com.chefitup.app.presentation.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import com.chefitup.app.presentation.components.FilterChipRow
import com.chefitup.app.presentation.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onOpenRecipe: (String) -> Unit,
    viewModel: SavedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.saved_title)) })
        val labels = listOf(
            SavedFilter.ALL to stringResource(R.string.saved_all),
            SavedFilter.BREAKFAST to stringResource(R.string.saved_breakfast),
            SavedFilter.LUNCH to stringResource(R.string.saved_lunch),
            SavedFilter.DINNER to stringResource(R.string.saved_dinner),
            SavedFilter.DESSERTS to stringResource(R.string.saved_desserts)
        )
        FilterChipRow(
            options = labels.map { it.second },
            selected = labels.first { it.first == uiState.filter }.second,
            onSelected = { label ->
                labels.firstOrNull { it.second == label }?.first?.let(viewModel::setFilter)
            },
            allowDeselect = false
        )
        if (uiState.filtered.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.saved_empty_title),
                body = stringResource(R.string.saved_empty_body),
                modifier = Modifier.padding(top = 32.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filtered, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onOpenRecipe(recipe.id) }
                    )
                }
            }
        }
    }
}
