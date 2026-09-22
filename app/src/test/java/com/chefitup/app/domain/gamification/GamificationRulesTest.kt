package com.chefitup.app.domain.gamification

import com.chefitup.app.domain.model.ChefLevel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GamificationRulesTest {

    @Test
    fun levelForXp_homeCookBelow500() {
        assertThat(GamificationRules.levelForXp(0)).isEqualTo(ChefLevel.HOME_COOK)
        assertThat(GamificationRules.levelForXp(499)).isEqualTo(ChefLevel.HOME_COOK)
    }

    @Test
    fun levelForXp_sousChefAt500() {
        assertThat(GamificationRules.levelForXp(500)).isEqualTo(ChefLevel.SOUS_CHEF)
        assertThat(GamificationRules.levelForXp(1499)).isEqualTo(ChefLevel.SOUS_CHEF)
    }

    @Test
    fun levelForXp_masterChefAt1500() {
        assertThat(GamificationRules.levelForXp(1500)).isEqualTo(ChefLevel.MASTER_CHEF)
    }

    @Test
    fun levelForXp_awardWinningAt3000() {
        assertThat(GamificationRules.levelForXp(3000))
            .isEqualTo(ChefLevel.AWARD_WINNING_CHEF)
    }

    @Test
    fun xpProgressInLevel_reportsProgressWithinSousChef() {
        // Sous Chef starts at 500; next at 1500 → span 1000
        val (inLevel, needed) = GamificationRules.xpProgressInLevel(750)
        assertThat(inLevel).isEqualTo(250)
        assertThat(needed).isEqualTo(1000)
    }

    @Test
    fun xpForCompletedRecipe_addsDifficultyBonus() {
        assertThat(GamificationRules.xpForCompletedRecipe("Easy"))
            .isEqualTo(GamificationRules.XP_PER_RECIPE)
        assertThat(GamificationRules.xpForCompletedRecipe("Hard"))
            .isEqualTo(GamificationRules.XP_PER_RECIPE + GamificationRules.XP_DIFFICULTY_BONUS_HARD)
    }

    @Test
    fun evaluateNewBadges_unlocksFirstRecipe() {
        val previous = GamificationRules.buildState(0, 0, emptyList())
        val updated = GamificationRules.buildState(50, 1, emptyList())
        val badges = GamificationRules.evaluateNewBadges(previous, updated)
        assertThat(badges).contains("first_recipe")
    }
}
