package com.chefitup.app.domain.gamification

import com.chefitup.app.domain.model.BadgeDefinition
import com.chefitup.app.domain.model.ChefLevel
import com.chefitup.app.domain.model.UserGamification

/**
 * Pure XP / level / badge rules — unit-tested without Android dependencies.
 */
object GamificationRules {

    const val XP_PER_RECIPE = 50
    const val XP_DIFFICULTY_BONUS_EASY = 0
    const val XP_DIFFICULTY_BONUS_MEDIUM = 10
    const val XP_DIFFICULTY_BONUS_HARD = 25

    /** Inclusive lower bounds for each level. */
    val LEVEL_THRESHOLDS: List<Pair<ChefLevel, Int>> = listOf(
        ChefLevel.HOME_COOK to 0,
        ChefLevel.SOUS_CHEF to 500,
        ChefLevel.MASTER_CHEF to 1500,
        ChefLevel.AWARD_WINNING_CHEF to 3000
    )

    fun levelForXp(xp: Int): ChefLevel {
        val safeXp = xp.coerceAtLeast(0)
        return LEVEL_THRESHOLDS
            .lastOrNull { safeXp >= it.second }
            ?.first
            ?: ChefLevel.HOME_COOK
    }

    /**
     * @return Pair of (xp earned within current level, xp needed to reach next level).
     * For max level, needed equals progress (full bar).
     */
    fun xpProgressInLevel(xp: Int): Pair<Int, Int> {
        val safeXp = xp.coerceAtLeast(0)
        val level = levelForXp(safeXp)
        val currentThreshold = thresholdFor(level)
        val nextThreshold = nextThreshold(level)
        return if (nextThreshold == null) {
            val span = 500
            span to span
        } else {
            val inLevel = safeXp - currentThreshold
            val needed = nextThreshold - currentThreshold
            inLevel to needed
        }
    }

    fun thresholdFor(level: ChefLevel): Int =
        LEVEL_THRESHOLDS.first { it.first == level }.second

    fun nextThreshold(level: ChefLevel): Int? {
        val index = LEVEL_THRESHOLDS.indexOfFirst { it.first == level }
        return LEVEL_THRESHOLDS.getOrNull(index + 1)?.second
    }

    fun xpForCompletedRecipe(difficulty: String): Int {
        val bonus = when (difficulty.lowercase()) {
            "easy" -> XP_DIFFICULTY_BONUS_EASY
            "medium" -> XP_DIFFICULTY_BONUS_MEDIUM
            "hard" -> XP_DIFFICULTY_BONUS_HARD
            else -> XP_DIFFICULTY_BONUS_MEDIUM
        }
        return XP_PER_RECIPE + bonus
    }

    val ALL_BADGES: List<BadgeDefinition> = listOf(
        BadgeDefinition("first_recipe", "First Recipe", "Cook your first recipe", "badge_first"),
        BadgeDefinition("five_recipes", "5 Recipes Cooked", "Complete 5 recipes", "badge_five"),
        BadgeDefinition("ten_recipes", "10 Recipes Cooked", "Complete 10 recipes", "badge_ten"),
        BadgeDefinition("meal_planner", "Meal Planner", "Add a meal to your weekly plan", "badge_planner"),
        BadgeDefinition("recipe_explorer", "Recipe Explorer", "View 10 different recipes", "badge_explorer"),
        BadgeDefinition("weekend_chef", "Weekend Chef", "Cook on a weekend", "badge_weekend"),
        BadgeDefinition("master_cook", "Master Cook", "Reach Master Chef level", "badge_master")
    )

    fun evaluateNewBadges(
        previous: UserGamification,
        updated: UserGamification,
        viewedRecipeCount: Int = 0,
        hasMealPlanEntry: Boolean = false,
        cookedOnWeekend: Boolean = false
    ): List<String> {
        val earned = updated.earnedBadgeIds.toMutableSet()
        fun unlock(id: String) {
            if (id !in previous.earnedBadgeIds) earned += id
        }
        if (updated.recipesCompleted >= 1) unlock("first_recipe")
        if (updated.recipesCompleted >= 5) unlock("five_recipes")
        if (updated.recipesCompleted >= 10) unlock("ten_recipes")
        if (hasMealPlanEntry) unlock("meal_planner")
        if (viewedRecipeCount >= 10) unlock("recipe_explorer")
        if (cookedOnWeekend) unlock("weekend_chef")
        if (updated.level == ChefLevel.MASTER_CHEF ||
            updated.level == ChefLevel.AWARD_WINNING_CHEF
        ) {
            unlock("master_cook")
        }
        return earned.toList()
    }

    fun buildState(
        xp: Int,
        recipesCompleted: Int,
        earnedBadgeIds: List<String>
    ): UserGamification {
        val (inLevel, needed) = xpProgressInLevel(xp)
        return UserGamification(
            xp = xp.coerceAtLeast(0),
            level = levelForXp(xp),
            recipesCompleted = recipesCompleted.coerceAtLeast(0),
            earnedBadgeIds = earnedBadgeIds,
            xpInCurrentLevel = inLevel,
            xpNeededForNextLevel = needed
        )
    }
}
