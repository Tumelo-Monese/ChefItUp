package com.chefitup.app.data.repository

import com.chefitup.app.data.firestore.FirestoreUserSync
import com.chefitup.app.data.local.JsonListConverter
import com.chefitup.app.data.local.PendingActionType
import com.chefitup.app.data.local.dao.GamificationDao
import com.chefitup.app.data.local.entity.GamificationEntity
import com.chefitup.app.domain.gamification.GamificationRules
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.BadgeDefinition
import com.chefitup.app.domain.model.UserGamification
import com.chefitup.app.domain.repository.GamificationRepository
import com.chefitup.app.domain.repository.SyncRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepositoryImpl @Inject constructor(
    private val gamificationDao: GamificationDao,
    private val syncRepository: SyncRepository,
    private val firestoreUserSync: FirestoreUserSync,
    private val gson: Gson
) : GamificationRepository {

    override fun observeGamification(): Flow<UserGamification> =
        gamificationDao.observe().map { entity ->
            entity?.toDomain() ?: GamificationRules.buildState(0, 0, emptyList())
        }

    override fun allBadges(): List<BadgeDefinition> = GamificationRules.ALL_BADGES

    override suspend fun completeRecipe(
        recipeId: String,
        difficulty: String,
        cookedOnWeekend: Boolean
    ): ApiOutcome<UserGamification> {
        val current = ensureEntity()
        val previous = current.toDomain()
        val xpGain = GamificationRules.xpForCompletedRecipe(difficulty)
        val newXp = current.xp + xpGain
        val newCompleted = current.recipesCompleted + 1
        val viewed = JsonListConverter.jsonToStringList(current.viewedRecipeIdsJson)
        val provisional = GamificationRules.buildState(
            xp = newXp,
            recipesCompleted = newCompleted,
            earnedBadgeIds = JsonListConverter.jsonToStringList(current.earnedBadgesJson)
        )
        val badges = GamificationRules.evaluateNewBadges(
            previous = previous,
            updated = provisional,
            viewedRecipeCount = viewed.size,
            hasMealPlanEntry = current.hasUsedMealPlan,
            cookedOnWeekend = cookedOnWeekend
        )
        val updated = current.copy(
            xp = newXp,
            recipesCompleted = newCompleted,
            earnedBadgesJson = JsonListConverter.stringListToJson(badges),
            updatedAtEpochMs = System.currentTimeMillis()
        )
        gamificationDao.upsert(updated)
        enqueueSync(updated)
        runCatching { firestoreUserSync.pushGamification(updated.toDomain()) }
        return ApiOutcome.Success(updated.toDomain())
    }

    override suspend fun recordRecipeViewed(recipeId: String): ApiOutcome<UserGamification> {
        val current = ensureEntity()
        val previous = current.toDomain()
        val viewed = JsonListConverter.jsonToStringList(current.viewedRecipeIdsJson).toMutableSet()
        viewed += recipeId
        val provisional = previous
        val badges = GamificationRules.evaluateNewBadges(
            previous = previous,
            updated = provisional,
            viewedRecipeCount = viewed.size,
            hasMealPlanEntry = current.hasUsedMealPlan,
            cookedOnWeekend = false
        )
        val updated = current.copy(
            viewedRecipeIdsJson = JsonListConverter.stringListToJson(viewed.toList()),
            earnedBadgesJson = JsonListConverter.stringListToJson(badges),
            updatedAtEpochMs = System.currentTimeMillis()
        )
        gamificationDao.upsert(updated)
        return ApiOutcome.Success(updated.toDomain())
    }

    override suspend fun recordMealPlanUsed(): ApiOutcome<UserGamification> {
        val current = ensureEntity()
        val previous = current.toDomain()
        val badges = GamificationRules.evaluateNewBadges(
            previous = previous,
            updated = previous,
            viewedRecipeCount = JsonListConverter.jsonToStringList(current.viewedRecipeIdsJson).size,
            hasMealPlanEntry = true,
            cookedOnWeekend = false
        )
        val updated = current.copy(
            hasUsedMealPlan = true,
            earnedBadgesJson = JsonListConverter.stringListToJson(badges),
            updatedAtEpochMs = System.currentTimeMillis()
        )
        gamificationDao.upsert(updated)
        enqueueSync(updated)
        return ApiOutcome.Success(updated.toDomain())
    }

    override suspend fun getSnapshot(): UserGamification =
        ensureEntity().toDomain()

    private suspend fun ensureEntity(): GamificationEntity {
        val existing = gamificationDao.get()
        if (existing != null) return existing
        val fresh = GamificationEntity(
            id = 1,
            xp = 0,
            recipesCompleted = 0,
            earnedBadgesJson = "[]",
            viewedRecipeIdsJson = "[]",
            hasUsedMealPlan = false,
            updatedAtEpochMs = System.currentTimeMillis()
        )
        gamificationDao.upsert(fresh)
        return fresh
    }

    private suspend fun enqueueSync(entity: GamificationEntity) {
        syncRepository.enqueue(
            type = PendingActionType.SYNC_GAMIFICATION.name,
            payloadJson = gson.toJson(entity),
            entityKey = "gamification"
        )
    }

    private fun GamificationEntity.toDomain(): UserGamification =
        GamificationRules.buildState(
            xp = xp,
            recipesCompleted = recipesCompleted,
            earnedBadgeIds = JsonListConverter.jsonToStringList(earnedBadgesJson)
        )
}
