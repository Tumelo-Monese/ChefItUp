package com.chefitup.app.domain.gamification

import com.chefitup.app.domain.model.ChefLevel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChefLevelProgressionTest {

    @Test
    fun levelsIncreaseWithXp() {
        assertThat(GamificationRules.buildState(0, 0, emptyList()).level)
            .isEqualTo(ChefLevel.HOME_COOK)
        assertThat(GamificationRules.buildState(500, 5, emptyList()).level)
            .isEqualTo(ChefLevel.SOUS_CHEF)
        assertThat(GamificationRules.buildState(1500, 12, emptyList()).level)
            .isEqualTo(ChefLevel.MASTER_CHEF)
        assertThat(GamificationRules.buildState(4000, 30, emptyList()).level)
            .isEqualTo(ChefLevel.AWARD_WINNING_CHEF)
    }

    @Test
    fun xpForHardRecipeIsHigherThanEasy() {
        val easy = GamificationRules.xpForCompletedRecipe("Easy")
        val hard = GamificationRules.xpForCompletedRecipe("Hard")
        assertThat(hard).isGreaterThan(easy)
    }
}
