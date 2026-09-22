package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.BadgeDefinition
import com.chefitup.app.domain.model.UserGamification
import kotlinx.coroutines.flow.Flow

interface GamificationRepository {
    fun observeGamification(): Flow<UserGamification>
    fun allBadges(): List<BadgeDefinition>

    suspend fun completeRecipe(
        recipeId: String,
        difficulty: String,
        cookedOnWeekend: Boolean = false
    ): ApiOutcome<UserGamification>

    suspend fun recordRecipeViewed(recipeId: String): ApiOutcome<UserGamification>
    suspend fun recordMealPlanUsed(): ApiOutcome<UserGamification>
    suspend fun getSnapshot(): UserGamification
}
