package com.chefitup.app.presentation.recipe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.chefitup.app.R
import com.chefitup.app.domain.model.MealType
import com.chefitup.app.presentation.components.ChefItUpButton
import com.chefitup.app.presentation.components.ChefItUpOutlinedButton
import com.chefitup.app.presentation.components.ErrorState
import com.chefitup.app.presentation.components.ChefItUpLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onStartCooking: (String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val addedShopping = stringResource(R.string.recipe_added_to_shopping)
    val addedMealPlan = stringResource(R.string.recipe_added_to_meal_plan)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RecipeDetailEvent.AddedToShopping -> snackbarHostState.showSnackbar(addedShopping)
                RecipeDetailEvent.AddedToMealPlan -> snackbarHostState.showSnackbar(addedMealPlan)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recipe_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavourite) {
                        Icon(
                            imageVector = if (uiState.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.cd_favourite),
                            tint = if (uiState.isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.padding(padding).fillMaxSize()) { ChefItUpLoading() }
            uiState.errorResId != null && uiState.recipe == null -> {
                ErrorState(
                    message = uiState.errorMessage ?: stringResource(uiState.errorResId!!),
                    onRetry = viewModel::load,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.recipe != null -> {
                val recipe = uiState.recipe!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = stringResource(R.string.cd_recipe_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(recipe.title, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Text(stringResource(R.string.rating_value, recipe.rating))
                            }
                            Text(stringResource(R.string.recipe_ready_in, recipe.readyInMinutes))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.recipe_servings), style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = viewModel::decreaseServings) {
                                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.decrease_servings))
                            }
                            Text("${uiState.servings}", style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = viewModel::increaseServings) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.increase_servings))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        ChefItUpButton(
                            text = stringResource(R.string.recipe_start_cooking),
                            onClick = { onStartCooking(recipe.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ChefItUpOutlinedButton(
                            text = stringResource(R.string.recipe_add_to_meal_plan),
                            onClick = { viewModel.showMealPlanDialog(true) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ChefItUpOutlinedButton(
                            text = stringResource(R.string.recipe_add_to_shopping),
                            onClick = viewModel::addIngredientsToShoppingList
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(stringResource(R.string.recipe_ingredients), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.scaledIngredients.forEach { ingredient ->
                            Text(
                                text = "• ${ingredient.original.ifBlank { "${ingredient.amount} ${ingredient.unit} ${ingredient.name}".trim() }}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(stringResource(R.string.recipe_instructions), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        recipe.instructions.forEach { step ->
                            Text(
                                text = stringResource(R.string.recipe_step_number, step.number),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(step.step, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    if (uiState.showMealPlanDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showMealPlanDialog(false) },
            title = { Text(stringResource(R.string.recipe_add_to_meal_plan)) },
            text = {
                Column {
                    Text(stringResource(R.string.meal_plan_pick_day))
                    Spacer(modifier = Modifier.height(8.dp))
                    val days = listOf(1 to R.string.day_mon, 2 to R.string.day_tue, 3 to R.string.day_wed, 4 to R.string.day_thu, 5 to R.string.day_fri, 6 to R.string.day_sat, 7 to R.string.day_sun)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        days.forEach { (value, label) ->
                            FilterChip(
                                selected = uiState.selectedDayOfWeek == value,
                                onClick = { viewModel.selectDay(value) },
                                label = { Text(stringResource(label)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.meal_plan_pick_meal))
                    Spacer(modifier = Modifier.height(8.dp))
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedMealType == type,
                            onClick = { viewModel.selectMealType(type) },
                            label = {
                                Text(
                                    when (type) {
                                        MealType.BREAKFAST -> stringResource(R.string.meal_type_breakfast)
                                        MealType.LUNCH -> stringResource(R.string.meal_type_lunch)
                                        MealType.DINNER -> stringResource(R.string.meal_type_dinner)
                                        MealType.SNACK -> stringResource(R.string.meal_type_snack)
                                    }
                                )
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmAddToMealPlan) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showMealPlanDialog(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
