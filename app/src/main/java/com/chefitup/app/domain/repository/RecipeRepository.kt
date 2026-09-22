package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.RecipeDetail
import com.chefitup.app.domain.model.RecipeFilters
import com.chefitup.app.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeCachedRecipes(): Flow<List<RecipeSummary>>
    fun observeRecentlyViewed(): Flow<List<RecipeSummary>>
    fun observeSearchHistory(): Flow<List<String>>

    suspend fun search(filters: RecipeFilters): ApiOutcome<List<RecipeSummary>>
    suspend fun getDetail(recipeId: String): ApiOutcome<RecipeDetail>
    suspend fun findByIngredients(
        ingredients: List<String>,
        number: Int = 20
    ): ApiOutcome<List<RecipeSummary>>

    suspend fun getRandom(number: Int = 10, tags: String? = null): ApiOutcome<List<RecipeSummary>>
    suspend fun getHomeFeed(): ApiOutcome<List<RecipeSummary>>

    suspend fun clearSearchHistory()
    suspend fun addSearchQuery(query: String)
}
