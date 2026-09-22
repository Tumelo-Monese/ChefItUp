package com.chefitup.app.data.remote

import com.chefitup.app.data.remote.dto.ByIngredientsRequest
import com.chefitup.app.data.remote.dto.HealthResponse
import com.chefitup.app.data.remote.dto.RandomRecipesResponseDto
import com.chefitup.app.data.remote.dto.RecipeDetailDto
import com.chefitup.app.data.remote.dto.RecipeSearchResponseDto
import com.chefitup.app.data.remote.dto.RecipeSummaryDto
import com.chefitup.app.data.remote.dto.SyncFavouriteRequest
import com.chefitup.app.data.remote.dto.SyncMealPlanRequest
import com.chefitup.app.data.remote.dto.SyncShoppingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChefItUpApi {

    @GET("api/recipes/search")
    suspend fun searchRecipes(
        @Query("query") query: String? = null,
        @Query("cuisine") cuisine: String? = null,
        @Query("diet") diet: String? = null,
        @Query("type") type: String? = null,
        @Query("maxReadyTime") maxReadyTime: Int? = null,
        @Query("offset") offset: Int = 0,
        @Query("number") number: Int = 20
    ): RecipeSearchResponseDto

    @GET("api/recipes/{id}")
    suspend fun getRecipeDetail(@Path("id") id: String): RecipeDetailDto

    @POST("api/recipes/by-ingredients")
    suspend fun findByIngredients(@Body body: ByIngredientsRequest): List<RecipeSummaryDto>

    @GET("api/recipes/random")
    suspend fun getRandomRecipes(
        @Query("number") number: Int = 10,
        @Query("tags") tags: String? = null
    ): RandomRecipesResponseDto

    @GET("health")
    suspend fun health(): HealthResponse

    @POST("api/sync/favourites")
    suspend fun syncFavourite(@Body body: SyncFavouriteRequest): Response<Unit>

    @POST("api/sync/meal-plans")
    suspend fun syncMealPlan(@Body body: SyncMealPlanRequest): Response<Unit>

    @POST("api/sync/shopping")
    suspend fun syncShopping(@Body body: SyncShoppingRequest): Response<Unit>
}
