package com.chefitup.app.data.repository

import com.chefitup.app.data.local.PendingActionType
import com.chefitup.app.data.local.dao.MealPlanDao
import com.chefitup.app.data.mapper.RecipeMapper
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.MealType
import com.chefitup.app.domain.repository.GamificationRepository
import com.chefitup.app.domain.repository.MealPlanRepository
import com.chefitup.app.domain.repository.SyncRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepositoryImpl @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val syncRepository: SyncRepository,
    private val gamificationRepository: GamificationRepository,
    private val gson: Gson
) : MealPlanRepository {

    override fun observeWeek(weekStartEpochDay: Long): Flow<List<MealPlanEntry>> =
        mealPlanDao.observeWeek(weekStartEpochDay).map { list ->
            list.map(RecipeMapper::mealPlanEntityToDomain)
        }

    override fun observeAll(): Flow<List<MealPlanEntry>> =
        mealPlanDao.observeAll().map { list -> list.map(RecipeMapper::mealPlanEntityToDomain) }

    override suspend fun upsert(entry: MealPlanEntry): ApiOutcome<MealPlanEntry> {
        val withId = if (entry.id.isBlank()) entry.copy(id = UUID.randomUUID().toString()) else entry
        val entity = RecipeMapper.mealPlanToEntity(withId, System.currentTimeMillis())
        mealPlanDao.upsert(entity)
        syncRepository.enqueue(
            type = PendingActionType.UPSERT_MEAL.name,
            payloadJson = gson.toJson(entity),
            entityKey = entity.id
        )
        gamificationRepository.recordMealPlanUsed()
        return ApiOutcome.Success(withId)
    }

    override suspend fun delete(entryId: String): ApiOutcome<Unit> {
        mealPlanDao.delete(entryId)
        syncRepository.enqueue(
            type = PendingActionType.DELETE_MEAL.name,
            payloadJson = gson.toJson(mapOf("id" to entryId)),
            entityKey = entryId
        )
        return ApiOutcome.Success(Unit)
    }

    override suspend fun clearDay(weekStartEpochDay: Long, dayOfWeek: Int): ApiOutcome<Unit> {
        mealPlanDao.clearDay(weekStartEpochDay, dayOfWeek)
        return ApiOutcome.Success(Unit)
    }

    override suspend fun entriesForMeal(
        weekStartEpochDay: Long,
        dayOfWeek: Int,
        mealType: MealType
    ): List<MealPlanEntry> =
        mealPlanDao.forMeal(weekStartEpochDay, dayOfWeek, mealType.name)
            .map(RecipeMapper::mealPlanEntityToDomain)
}
