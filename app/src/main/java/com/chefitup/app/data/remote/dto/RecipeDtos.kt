package com.chefitup.app.data.remote.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

/** Accepts JSON string or number ids from the ASP.NET / Spoonacular layer. */
class StringOrNumberAdapter : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): String {
        if (json.isJsonNull) return ""
        val primitive = json.asJsonPrimitive
        return when {
            primitive.isNumber -> primitive.asNumber.toLong().toString()
            else -> primitive.asString
        }
    }
}

data class RecipeSummaryDto(
    @JsonAdapter(StringOrNumberAdapter::class)
    val id: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val readyInMinutes: Int = 0,
    val servings: Int = 1,
    val difficulty: String = "Medium",
    val rating: Double = 0.0,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val dairyFree: Boolean = false,
    val cuisines: List<String> = emptyList(),
    val dishTypes: List<String> = emptyList(),
    val usedIngredientCount: Int? = null,
    val missedIngredientCount: Int? = null,
    val matchedIngredientsLabel: String? = null
)

data class IngredientDto(
    val name: String = "",
    val amount: Double = 0.0,
    val unit: String = "",
    val original: String = ""
)

data class InstructionStepDto(
    val number: Int = 0,
    val step: String = ""
)

data class NutritionItemDto(
    val name: String = "",
    val amount: Double = 0.0,
    val unit: String = ""
)

data class RecipeDetailDto(
    @JsonAdapter(StringOrNumberAdapter::class)
    val id: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val readyInMinutes: Int = 0,
    val servings: Int = 1,
    val difficulty: String = "Medium",
    val rating: Double = 0.0,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val dairyFree: Boolean = false,
    val cuisines: List<String> = emptyList(),
    val dishTypes: List<String> = emptyList(),
    val summary: String? = null,
    val description: String? = null,
    val ingredients: List<IngredientDto> = emptyList(),
    val instructions: List<InstructionStepDto> = emptyList(),
    val nutrition: List<NutritionItemDto> = emptyList()
)

data class RecipeSearchResponseDto(
    val offset: Int = 0,
    val number: Int = 0,
    val totalResults: Int = 0,
    val results: List<RecipeSummaryDto> = emptyList()
)

data class RandomRecipesResponseDto(
    val recipes: List<RecipeSummaryDto> = emptyList()
)

data class ByIngredientsRequest(
    val ingredients: List<String>
)

data class SyncFavouriteRequest(
    val recipeId: String,
    val action: String
)

data class SyncMealPlanRequest(
    val id: String,
    val payloadJson: String,
    val action: String
)

data class SyncShoppingRequest(
    val id: String,
    val payloadJson: String,
    val action: String
)

data class HealthResponse(
    val status: String = "",
    val spoonacularConfigured: Boolean = false
)
