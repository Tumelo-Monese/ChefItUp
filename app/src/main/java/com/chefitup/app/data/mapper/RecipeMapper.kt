package com.chefitup.app.data.mapper

import com.chefitup.app.data.local.JsonListConverter
import com.chefitup.app.data.local.entity.FavouriteEntity
import com.chefitup.app.data.local.entity.MealPlanEntity
import com.chefitup.app.data.local.entity.RecentlyViewedEntity
import com.chefitup.app.data.local.entity.RecipeEntity
import com.chefitup.app.data.local.entity.ShoppingItemEntity
import com.chefitup.app.data.remote.dto.IngredientDto
import com.chefitup.app.data.remote.dto.InstructionStepDto
import com.chefitup.app.data.remote.dto.NutritionItemDto
import com.chefitup.app.data.remote.dto.RecipeDetailDto
import com.chefitup.app.data.remote.dto.RecipeSummaryDto
import com.chefitup.app.domain.model.Ingredient
import com.chefitup.app.domain.model.InstructionStep
import com.chefitup.app.domain.model.MealPlanEntry
import com.chefitup.app.domain.model.MealType
import com.chefitup.app.domain.model.NutritionItem
import com.chefitup.app.domain.model.RecipeDetail
import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.model.ShoppingListItem
import com.google.gson.reflect.TypeToken

object RecipeMapper {

    fun summaryDtoToDomain(dto: RecipeSummaryDto): RecipeSummary = RecipeSummary(
        id = dto.id,
        title = dto.title,
        imageUrl = dto.imageUrl,
        readyInMinutes = dto.readyInMinutes,
        servings = dto.servings,
        difficulty = dto.difficulty,
        rating = dto.rating,
        vegetarian = dto.vegetarian,
        vegan = dto.vegan,
        glutenFree = dto.glutenFree,
        dairyFree = dto.dairyFree,
        cuisines = dto.cuisines,
        dishTypes = dto.dishTypes,
        usedIngredientCount = dto.usedIngredientCount,
        missedIngredientCount = dto.missedIngredientCount,
        matchedIngredientsLabel = dto.matchedIngredientsLabel
    )

    fun detailDtoToDomain(dto: RecipeDetailDto): RecipeDetail = RecipeDetail(
        id = dto.id,
        title = dto.title,
        imageUrl = dto.imageUrl,
        readyInMinutes = dto.readyInMinutes,
        servings = dto.servings,
        difficulty = dto.difficulty,
        rating = dto.rating,
        vegetarian = dto.vegetarian,
        vegan = dto.vegan,
        glutenFree = dto.glutenFree,
        dairyFree = dto.dairyFree,
        cuisines = dto.cuisines,
        dishTypes = dto.dishTypes,
        summary = dto.summary ?: dto.description.orEmpty(),
        ingredients = dto.ingredients.map { it.toDomain() },
        instructions = dto.instructions.map { InstructionStep(it.number, it.step) },
        nutrition = dto.nutrition.map { NutritionItem(it.name, it.amount, it.unit) }
    )

    fun detailToEntity(detail: RecipeDetail, cachedAt: Long): RecipeEntity = RecipeEntity(
        id = detail.id,
        title = detail.title,
        imageUrl = detail.imageUrl,
        readyInMinutes = detail.readyInMinutes,
        servings = detail.servings,
        difficulty = detail.difficulty,
        rating = detail.rating,
        vegetarian = detail.vegetarian,
        vegan = detail.vegan,
        glutenFree = detail.glutenFree,
        dairyFree = detail.dairyFree,
        cuisinesJson = JsonListConverter.stringListToJson(detail.cuisines),
        dishTypesJson = JsonListConverter.stringListToJson(detail.dishTypes),
        summary = detail.summary,
        ingredientsJson = JsonListConverter.toJson(detail.ingredients.map { it.toDto() }),
        instructionsJson = JsonListConverter.toJson(
            detail.instructions.map { InstructionStepDto(it.number, it.step) }
        ),
        nutritionJson = JsonListConverter.toJson(
            detail.nutrition.map { NutritionItemDto(it.name, it.amount, it.unit) }
        ),
        cachedAtEpochMs = cachedAt,
        hasFullDetail = true
    )

    fun summaryToEntity(summary: RecipeSummary, cachedAt: Long): RecipeEntity = RecipeEntity(
        id = summary.id,
        title = summary.title,
        imageUrl = summary.imageUrl,
        readyInMinutes = summary.readyInMinutes,
        servings = summary.servings,
        difficulty = summary.difficulty,
        rating = summary.rating,
        vegetarian = summary.vegetarian,
        vegan = summary.vegan,
        glutenFree = summary.glutenFree,
        dairyFree = summary.dairyFree,
        cuisinesJson = JsonListConverter.stringListToJson(summary.cuisines),
        dishTypesJson = JsonListConverter.stringListToJson(summary.dishTypes),
        summary = null,
        ingredientsJson = null,
        instructionsJson = null,
        nutritionJson = null,
        cachedAtEpochMs = cachedAt,
        hasFullDetail = false
    )

    fun entityToSummary(entity: RecipeEntity): RecipeSummary = RecipeSummary(
        id = entity.id,
        title = entity.title,
        imageUrl = entity.imageUrl,
        readyInMinutes = entity.readyInMinutes,
        servings = entity.servings,
        difficulty = entity.difficulty,
        rating = entity.rating,
        vegetarian = entity.vegetarian,
        vegan = entity.vegan,
        glutenFree = entity.glutenFree,
        dairyFree = entity.dairyFree,
        cuisines = JsonListConverter.jsonToStringList(entity.cuisinesJson),
        dishTypes = JsonListConverter.jsonToStringList(entity.dishTypesJson)
    )

    fun entityToDetail(entity: RecipeEntity): RecipeDetail? {
        if (!entity.hasFullDetail) return null
        val ingredientsType = object : TypeToken<List<IngredientDto>>() {}
        val instructionsType = object : TypeToken<List<InstructionStepDto>>() {}
        val nutritionType = object : TypeToken<List<NutritionItemDto>>() {}
        val ingredients = JsonListConverter.fromJson(entity.ingredientsJson, ingredientsType)
            .orEmpty()
            .map { it.toDomain() }
        val instructions = JsonListConverter.fromJson(entity.instructionsJson, instructionsType)
            .orEmpty()
            .map { InstructionStep(it.number, it.step) }
        val nutrition = JsonListConverter.fromJson(entity.nutritionJson, nutritionType)
            .orEmpty()
            .map { NutritionItem(it.name, it.amount, it.unit) }
        return RecipeDetail(
            id = entity.id,
            title = entity.title,
            imageUrl = entity.imageUrl,
            readyInMinutes = entity.readyInMinutes,
            servings = entity.servings,
            difficulty = entity.difficulty,
            rating = entity.rating,
            vegetarian = entity.vegetarian,
            vegan = entity.vegan,
            glutenFree = entity.glutenFree,
            dairyFree = entity.dairyFree,
            cuisines = JsonListConverter.jsonToStringList(entity.cuisinesJson),
            dishTypes = JsonListConverter.jsonToStringList(entity.dishTypesJson),
            summary = entity.summary.orEmpty(),
            ingredients = ingredients,
            instructions = instructions,
            nutrition = nutrition
        )
    }

    fun favouriteToSummary(entity: FavouriteEntity): RecipeSummary = RecipeSummary(
        id = entity.recipeId,
        title = entity.title,
        imageUrl = entity.imageUrl,
        readyInMinutes = entity.readyInMinutes,
        servings = entity.servings,
        difficulty = entity.difficulty,
        rating = entity.rating,
        vegetarian = entity.vegetarian,
        vegan = entity.vegan,
        glutenFree = entity.glutenFree,
        dairyFree = entity.dairyFree,
        cuisines = JsonListConverter.jsonToStringList(entity.cuisinesJson),
        dishTypes = JsonListConverter.jsonToStringList(entity.dishTypesJson)
    )

    fun summaryToFavourite(summary: RecipeSummary, savedAt: Long): FavouriteEntity =
        FavouriteEntity(
            recipeId = summary.id,
            title = summary.title,
            imageUrl = summary.imageUrl,
            readyInMinutes = summary.readyInMinutes,
            servings = summary.servings,
            difficulty = summary.difficulty,
            rating = summary.rating,
            vegetarian = summary.vegetarian,
            vegan = summary.vegan,
            glutenFree = summary.glutenFree,
            dairyFree = summary.dairyFree,
            cuisinesJson = JsonListConverter.stringListToJson(summary.cuisines),
            dishTypesJson = JsonListConverter.stringListToJson(summary.dishTypes),
            savedAtEpochMs = savedAt
        )

    fun recentlyToSummary(entity: RecentlyViewedEntity): RecipeSummary = RecipeSummary(
        id = entity.recipeId,
        title = entity.title,
        imageUrl = entity.imageUrl,
        readyInMinutes = entity.readyInMinutes,
        servings = entity.servings,
        difficulty = entity.difficulty,
        rating = entity.rating,
        vegetarian = entity.vegetarian,
        vegan = entity.vegan,
        glutenFree = entity.glutenFree,
        dairyFree = entity.dairyFree,
        cuisines = JsonListConverter.jsonToStringList(entity.cuisinesJson),
        dishTypes = JsonListConverter.jsonToStringList(entity.dishTypesJson)
    )

    fun summaryToRecentlyViewed(summary: RecipeSummary, viewedAt: Long): RecentlyViewedEntity =
        RecentlyViewedEntity(
            recipeId = summary.id,
            title = summary.title,
            imageUrl = summary.imageUrl,
            readyInMinutes = summary.readyInMinutes,
            servings = summary.servings,
            difficulty = summary.difficulty,
            rating = summary.rating,
            vegetarian = summary.vegetarian,
            vegan = summary.vegan,
            glutenFree = summary.glutenFree,
            dairyFree = summary.dairyFree,
            cuisinesJson = JsonListConverter.stringListToJson(summary.cuisines),
            dishTypesJson = JsonListConverter.stringListToJson(summary.dishTypes),
            viewedAtEpochMs = viewedAt
        )

    fun mealPlanEntityToDomain(entity: MealPlanEntity): MealPlanEntry = MealPlanEntry(
        id = entity.id,
        recipeId = entity.recipeId,
        recipeTitle = entity.recipeTitle,
        recipeImageUrl = entity.recipeImageUrl,
        weekStartEpochDay = entity.weekStartEpochDay,
        dayOfWeek = entity.dayOfWeek,
        mealType = runCatching { MealType.valueOf(entity.mealType) }.getOrDefault(MealType.DINNER),
        servings = entity.servings,
        notes = entity.notes
    )

    fun mealPlanToEntity(entry: MealPlanEntry, updatedAt: Long): MealPlanEntity =
        MealPlanEntity(
            id = entry.id,
            recipeId = entry.recipeId,
            recipeTitle = entry.recipeTitle,
            recipeImageUrl = entry.recipeImageUrl,
            weekStartEpochDay = entry.weekStartEpochDay,
            dayOfWeek = entry.dayOfWeek,
            mealType = entry.mealType.name,
            servings = entry.servings,
            notes = entry.notes,
            updatedAtEpochMs = updatedAt
        )

    fun shoppingEntityToDomain(entity: ShoppingItemEntity): ShoppingListItem = ShoppingListItem(
        id = entity.id,
        name = entity.name,
        amount = entity.amount,
        unit = entity.unit,
        category = entity.category,
        isChecked = entity.isChecked,
        recipeId = entity.recipeId,
        isCustom = entity.isCustom
    )

    fun shoppingToEntity(item: ShoppingListItem, updatedAt: Long): ShoppingItemEntity =
        ShoppingItemEntity(
            id = item.id,
            name = item.name,
            amount = item.amount,
            unit = item.unit,
            category = item.category,
            isChecked = item.isChecked,
            recipeId = item.recipeId,
            isCustom = item.isCustom,
            updatedAtEpochMs = updatedAt
        )

    private fun IngredientDto.toDomain() = Ingredient(
        name = name,
        amount = amount,
        unit = unit,
        original = original.ifBlank { listOf(amount.toString(), unit, name).joinToString(" ").trim() }
    )

    private fun Ingredient.toDto() = IngredientDto(
        name = name,
        amount = amount,
        unit = unit,
        original = original
    )
}
