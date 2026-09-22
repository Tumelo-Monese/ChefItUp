package com.chefitup.app.data.repository

import com.chefitup.app.data.firestore.FirestoreUserSync
import com.chefitup.app.data.local.PendingActionType
import com.chefitup.app.data.local.dao.FavouriteDao
import com.chefitup.app.data.mapper.RecipeMapper
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.repository.FavouriteRepository
import com.chefitup.app.domain.repository.SyncRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteRepositoryImpl @Inject constructor(
    private val favouriteDao: FavouriteDao,
    private val syncRepository: SyncRepository,
    private val firestoreUserSync: FirestoreUserSync,
    private val gson: Gson
) : FavouriteRepository {

    override fun observeFavourites(): Flow<List<RecipeSummary>> =
        favouriteDao.observeAll().map { list -> list.map(RecipeMapper::favouriteToSummary) }

    override fun observeIsFavourite(recipeId: String): Flow<Boolean> =
        favouriteDao.observeIsFavourite(recipeId)

    override suspend fun addFavourite(recipe: RecipeSummary): ApiOutcome<Unit> {
        favouriteDao.upsert(
            RecipeMapper.summaryToFavourite(recipe, System.currentTimeMillis())
        )
        syncRepository.enqueue(
            type = PendingActionType.ADD_FAVOURITE.name,
            payloadJson = gson.toJson(mapOf("recipeId" to recipe.id)),
            entityKey = recipe.id
        )
        runCatching { firestoreUserSync.pushFavourite(recipe.id, true) }
        return ApiOutcome.Success(Unit)
    }

    override suspend fun removeFavourite(recipeId: String): ApiOutcome<Unit> {
        favouriteDao.delete(recipeId)
        syncRepository.enqueue(
            type = PendingActionType.REMOVE_FAVOURITE.name,
            payloadJson = gson.toJson(mapOf("recipeId" to recipeId)),
            entityKey = recipeId
        )
        runCatching { firestoreUserSync.pushFavourite(recipeId, false) }
        return ApiOutcome.Success(Unit)
    }

    override suspend fun toggleFavourite(recipe: RecipeSummary): ApiOutcome<Boolean> {
        val currentlyFavourite = favouriteDao.isFavourite(recipe.id)
        return if (currentlyFavourite) {
            removeFavourite(recipe.id).map { false }
        } else {
            addFavourite(recipe).map { true }
        }
    }

    override suspend fun isFavourite(recipeId: String): Boolean =
        favouriteDao.isFavourite(recipeId)

    private fun <T> ApiOutcome<Unit>.map(transform: () -> T): ApiOutcome<T> = when (this) {
        is ApiOutcome.Success -> ApiOutcome.Success(transform())
        is ApiOutcome.Failure -> this
    }
}
