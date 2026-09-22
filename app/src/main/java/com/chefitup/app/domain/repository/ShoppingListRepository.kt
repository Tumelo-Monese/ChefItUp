package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun observeItems(): Flow<List<ShoppingListItem>>

    suspend fun upsert(item: ShoppingListItem): ApiOutcome<ShoppingListItem>
    suspend fun delete(itemId: String): ApiOutcome<Unit>
    suspend fun setChecked(itemId: String, checked: Boolean): ApiOutcome<Unit>
    suspend fun clearCompleted(): ApiOutcome<Unit>
    suspend fun clearAll(): ApiOutcome<Unit>

    /** Builds / merges shopping items from meal-plan recipes. */
    suspend fun generateFromMealPlan(
        entries: List<MealPlanEntry>,
        ingredientsByRecipeId: Map<String, List<ShoppingListItem>>
    ): ApiOutcome<List<ShoppingListItem>>
}
