package com.chefitup.app.presentation.mealplan

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chefitup.app.R
import com.chefitup.app.domain.model.MealType
import com.chefitup.app.presentation.components.MealCard
import com.chefitup.app.presentation.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerScreen(
    onOpenRecipe: (String) -> Unit,
    onOpenShoppingList: () -> Unit,
    viewModel: MealPlannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val shoppingMsg = stringResource(R.string.meal_plan_shopping_generated)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                MealPlannerEvent.ShoppingGenerated -> {
                    snackbarHostState.showSnackbar(shoppingMsg)
                    onOpenShoppingList()
                }
            }
        }
    }

    val dayLabels = listOf(
        1 to R.string.day_mon,
        2 to R.string.day_tue,
        3 to R.string.day_wed,
        4 to R.string.day_thu,
        5 to R.string.day_fri,
        6 to R.string.day_sat,
        7 to R.string.day_sun
    )
    val byMeal = viewModel.entriesForSelectedDay()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.meal_planner_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.generateShoppingList()
            }) {
                Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.meal_plan_generate_shopping))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayLabels.forEach { (day, label) ->
                    FilterChip(
                        selected = uiState.selectedDay == day,
                        onClick = { viewModel.selectDay(day) },
                        label = { Text(stringResource(label)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            MealType.entries.forEach { mealType ->
                val title = when (mealType) {
                    MealType.BREAKFAST -> stringResource(R.string.meal_type_breakfast)
                    MealType.LUNCH -> stringResource(R.string.meal_type_lunch)
                    MealType.DINNER -> stringResource(R.string.meal_type_dinner)
                    MealType.SNACK -> stringResource(R.string.meal_type_snack)
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { viewModel.showAddDialog(true, mealType) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.meal_plan_add))
                        }
                    }
                    val entries = byMeal[mealType].orEmpty()
                    if (entries.isEmpty()) {
                        Text(
                            text = stringResource(R.string.meal_plan_empty_slot),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        entries.forEach { entry ->
                            MealCard(
                                entry = entry,
                                onClick = { onOpenRecipe(entry.recipeId) },
                                onRemove = { viewModel.removeMeal(entry.id) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showAddDialog(false) },
            title = { Text(stringResource(R.string.meal_plan_pick_recipe)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.recipeSuggestions.take(8).forEach { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { viewModel.addMeal(recipe) },
                            compact = false
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showAddDialog(false) }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}
