package com.chefitup.app.data.repository

import com.chefitup.app.R
import com.chefitup.app.data.local.MockRecipes
import com.chefitup.app.data.local.dao.RecentlyViewedDao
import com.chefitup.app.data.local.dao.RecipeDao
import com.chefitup.app.data.local.dao.SearchHistoryDao
import com.chefitup.app.data.local.entity.SearchHistoryEntity
import com.chefitup.app.data.mapper.RecipeMapper
import com.chefitup.app.data.remote.ChefItUpApi
import com.chefitup.app.data.remote.NetworkMonitor
import com.chefitup.app.data.remote.dto.ByIngredientsRequest
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.RecipeDetail
import com.chefitup.app.domain.model.RecipeFilters
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.repository.RecipeRepository
import com.chefitup.app.util.NetworkErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val api: ChefItUpApi,
    private val recipeDao: RecipeDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val recentlyViewedDao: RecentlyViewedDao,
    private val networkMonitor: NetworkMonitor
) : RecipeRepository {

    override fun observeCachedRecipes(): Flow<List<RecipeSummary>> =
        recipeDao.observeAll().map { list -> list.map(RecipeMapper::entityToSummary) }

    override fun observeRecentlyViewed(): Flow<List<RecipeSummary>> =
        recentlyViewedDao.observeRecent().map { list ->
            list.map(RecipeMapper::recentlyToSummary)
        }

    override fun observeSearchHistory(): Flow<List<String>> =
        searchHistoryDao.observeRecent().map { list -> list.map { it.query } }

    override suspend fun search(filters: RecipeFilters): ApiOutcome<List<RecipeSummary>> {
        if (filters.query.isNotBlank()) {
            addSearchQuery(filters.query)
        }
        return tryRemoteOrMock(
            remote = {
                val remote = api.searchRecipes(
                    query = filters.query.ifBlank { null },
                    cuisine = filters.cuisine,
                    diet = filters.diet,
                    type = filters.type,
                    maxReadyTime = filters.maxReadyTime,
                    offset = filters.offset,
                    number = filters.number
                ).results.map(RecipeMapper::summaryDtoToDomain)
                cacheSummaries(remote)
                applyLocalFilters(remote, filters)
            },
            fallback = {
                applyLocalFilters(MockRecipes.search(filters.query), filters)
            }
        )
    }

    override suspend fun getDetail(recipeId: String): ApiOutcome<RecipeDetail> {
        try {
            if (networkMonitor.isOnline) {
                val detail = RecipeMapper.detailDtoToDomain(api.getRecipeDetail(recipeId))
                recipeDao.upsert(RecipeMapper.detailToEntity(detail, System.currentTimeMillis()))
                rememberViewed(detail.toSummary())
                return ApiOutcome.Success(detail)
            }
        } catch (t: Throwable) {
            val offline = resolveOfflineDetail(recipeId)
            if (offline != null) {
                rememberViewed(offline.toSummary())
                return ApiOutcome.Success(offline)
            }
            return ApiOutcome.Failure(
                messageResId = NetworkErrorMapper.messageResId(t),
                message = t.message,
                cause = t,
                isNetworkError = NetworkErrorMapper.isNetworkError(t)
            )
        }

        val offline = resolveOfflineDetail(recipeId)
        return if (offline != null) {
            rememberViewed(offline.toSummary())
            ApiOutcome.Success(offline)
        } else {
            ApiOutcome.Failure(messageResId = R.string.error_not_found)
        }
    }

    override suspend fun findByIngredients(
        ingredients: List<String>,
        number: Int
    ): ApiOutcome<List<RecipeSummary>> = tryRemoteOrMock(
        remote = {
            val remote = api.findByIngredients(ByIngredientsRequest(ingredients))
                .map(RecipeMapper::summaryDtoToDomain)
                .take(number)
            cacheSummaries(remote)
            remote
        },
        fallback = { MockRecipes.byIngredients(ingredients).take(number) }
    )

    override suspend fun getRandom(number: Int, tags: String?): ApiOutcome<List<RecipeSummary>> =
        tryRemoteOrMock(
            remote = {
                val remote = api.getRandomRecipes(number, tags)
                    .recipes.map(RecipeMapper::summaryDtoToDomain)
                cacheSummaries(remote)
                remote
            },
            fallback = { MockRecipes.random(number) }
        )

    override suspend fun getHomeFeed(): ApiOutcome<List<RecipeSummary>> =
        getRandom(number = 12)

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clear()
    }

    override suspend fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        searchHistoryDao.upsert(
            SearchHistoryEntity(
                query = trimmed,
                searchedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    private suspend fun resolveOfflineDetail(recipeId: String): RecipeDetail? =
        recipeDao.getById(recipeId)?.let(RecipeMapper::entityToDetail)
            ?: MockRecipes.detailById(recipeId)

    private suspend fun cacheSummaries(summaries: List<RecipeSummary>) {
        val now = System.currentTimeMillis()
        recipeDao.upsertAll(summaries.map { RecipeMapper.summaryToEntity(it, now) })
    }

    private suspend fun rememberViewed(summary: RecipeSummary) {
        recentlyViewedDao.upsert(
            RecipeMapper.summaryToRecentlyViewed(summary, System.currentTimeMillis())
        )
        recentlyViewedDao.trim()
    }

    private fun applyLocalFilters(
        list: List<RecipeSummary>,
        filters: RecipeFilters
    ): List<RecipeSummary> = list.filter { recipe ->
        (filters.vegetarian != true || recipe.vegetarian) &&
            (filters.vegan != true || recipe.vegan) &&
            (filters.glutenFree != true || recipe.glutenFree) &&
            (filters.dairyFree != true || recipe.dairyFree) &&
            (filters.maxReadyTime == null || recipe.readyInMinutes <= filters.maxReadyTime) &&
            (filters.cuisine.isNullOrBlank() ||
                recipe.cuisines.any { it.equals(filters.cuisine, ignoreCase = true) }) &&
            (filters.type.isNullOrBlank() ||
                recipe.dishTypes.any { it.equals(filters.type, ignoreCase = true) }) &&
            (filters.diet.isNullOrBlank() || matchesDiet(recipe, filters.diet))
    }

    private fun matchesDiet(recipe: RecipeSummary, diet: String): Boolean =
        when (diet.lowercase()) {
            "vegetarian" -> recipe.vegetarian
            "vegan" -> recipe.vegan
            "gluten free", "glutenfree" -> recipe.glutenFree
            else -> true
        }

    private suspend fun <T> tryRemoteOrMock(
        remote: suspend () -> T,
        fallback: suspend () -> T
    ): ApiOutcome<T> {
        return try {
            if (!networkMonitor.isOnline) {
                return ApiOutcome.Success(fallback())
            }
            ApiOutcome.Success(remote())
        } catch (t: Throwable) {
            try {
                ApiOutcome.Success(fallback())
            } catch (_: Throwable) {
                ApiOutcome.Failure(
                    messageResId = NetworkErrorMapper.messageResId(t),
                    message = t.message,
                    cause = t,
                    isNetworkError = NetworkErrorMapper.isNetworkError(t)
                )
            }
        }
    }
}
