package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow

interface FavouriteRepository {
    fun observeFavourites(): Flow<List<RecipeSummary>>
    fun observeIsFavourite(recipeId: String): Flow<Boolean>

    suspend fun addFavourite(recipe: RecipeSummary): ApiOutcome<Unit>
    suspend fun removeFavourite(recipeId: String): ApiOutcome<Unit>
    suspend fun toggleFavourite(recipe: RecipeSummary): ApiOutcome<Boolean>
    suspend fun isFavourite(recipeId: String): Boolean
}
