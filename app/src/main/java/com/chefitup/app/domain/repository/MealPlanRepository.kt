package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.MealType
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {
    fun observeWeek(weekStartEpochDay: Long): Flow<List<MealPlanEntry>>
    fun observeAll(): Flow<List<MealPlanEntry>>

    suspend fun upsert(entry: MealPlanEntry): ApiOutcome<MealPlanEntry>
    suspend fun delete(entryId: String): ApiOutcome<Unit>
    suspend fun clearDay(weekStartEpochDay: Long, dayOfWeek: Int): ApiOutcome<Unit>
    suspend fun entriesForMeal(
        weekStartEpochDay: Long,
        dayOfWeek: Int,
        mealType: MealType
    ): List<MealPlanEntry>
}
