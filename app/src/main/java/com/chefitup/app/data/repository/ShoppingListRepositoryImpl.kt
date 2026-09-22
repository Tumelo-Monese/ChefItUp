package com.chefitup.app.data.repository

import com.chefitup.app.data.local.PendingActionType
import com.chefitup.app.data.local.dao.ShoppingItemDao
import com.chefitup.app.data.mapper.RecipeMapper
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.ShoppingListItem
import com.chefitup.app.domain.repository.ShoppingListRepository
import com.chefitup.app.domain.repository.SyncRepository
import com.chefitup.app.util.ShoppingListMerger
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val shoppingItemDao: ShoppingItemDao,
    private val syncRepository: SyncRepository,
    private val gson: Gson
) : ShoppingListRepository {

    override fun observeItems(): Flow<List<ShoppingListItem>> =
        shoppingItemDao.observeAll().map { list ->
            list.map(RecipeMapper::shoppingEntityToDomain)
        }

    override suspend fun upsert(item: ShoppingListItem): ApiOutcome<ShoppingListItem> {
        val withId = if (item.id.isBlank()) item.copy(id = UUID.randomUUID().toString()) else item
        val existing = shoppingItemDao.getAll().map(RecipeMapper::shoppingEntityToDomain)
        val merged = ShoppingListMerger.mergeIntoExisting(existing, listOf(withId))
        val now = System.currentTimeMillis()
        shoppingItemDao.clearAll()
        shoppingItemDao.upsertAll(merged.map { RecipeMapper.shoppingToEntity(it, now) })
        syncRepository.enqueue(
            type = PendingActionType.UPSERT_SHOPPING.name,
            payloadJson = gson.toJson(withId),
            entityKey = withId.id
        )
        val result = merged.firstOrNull {
            ShoppingListMerger.mergeKey(it.name, it.unit) ==
                ShoppingListMerger.mergeKey(withId.name, withId.unit)
        } ?: withId
        return ApiOutcome.Success(result)
    }

    override suspend fun delete(itemId: String): ApiOutcome<Unit> {
        shoppingItemDao.delete(itemId)
        syncRepository.enqueue(
            type = PendingActionType.DELETE_SHOPPING.name,
            payloadJson = gson.toJson(mapOf("id" to itemId)),
            entityKey = itemId
        )
        return ApiOutcome.Success(Unit)
    }

    override suspend fun setChecked(itemId: String, checked: Boolean): ApiOutcome<Unit> {
        shoppingItemDao.setChecked(itemId, checked)
        syncRepository.enqueue(
            type = PendingActionType.UPDATE_SHOPPING_CHECKED.name,
            payloadJson = gson.toJson(mapOf("id" to itemId, "checked" to checked)),
            entityKey = itemId
        )
        return ApiOutcome.Success(Unit)
    }

    override suspend fun clearCompleted(): ApiOutcome<Unit> {
        shoppingItemDao.clearCompleted()
        return ApiOutcome.Success(Unit)
    }

    override suspend fun clearAll(): ApiOutcome<Unit> {
        shoppingItemDao.clearAll()
        return ApiOutcome.Success(Unit)
    }

    override suspend fun generateFromMealPlan(
        entries: List<MealPlanEntry>,
        ingredientsByRecipeId: Map<String, List<ShoppingListItem>>
    ): ApiOutcome<List<ShoppingListItem>> {
        val generated = entries.flatMap { entry ->
            val base = ingredientsByRecipeId[entry.recipeId].orEmpty()
            base.map { item ->
                item.copy(
                    id = ShoppingListMerger.newId(),
                    amount = item.amount * entry.servings.coerceAtLeast(1),
                    recipeId = entry.recipeId,
                    isCustom = false,
                    isChecked = false
                )
            }
        }
        val existing = shoppingItemDao.getAll().map(RecipeMapper::shoppingEntityToDomain)
        val merged = ShoppingListMerger.mergeIntoExisting(existing, generated)
        val now = System.currentTimeMillis()
        shoppingItemDao.clearAll()
        shoppingItemDao.upsertAll(merged.map { RecipeMapper.shoppingToEntity(it, now) })
        syncRepository.enqueue(
            type = PendingActionType.UPSERT_SHOPPING.name,
            payloadJson = gson.toJson(mapOf("generated" to merged.size)),
            entityKey = "meal-plan-generate"
        )
        return ApiOutcome.Success(merged)
    }
}
