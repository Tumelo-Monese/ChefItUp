package com.chefitup.app.domain.gamification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class XpProgressTest {

    @Test
    fun progressWithinSousChefBand() {
        val (inLevel, needed) = GamificationRules.xpProgressInLevel(700)
        assertThat(inLevel).isEqualTo(200)
        assertThat(needed).isEqualTo(1000)
    }

    @Test
    fun hardRecipeAwardsSeventyFiveXp() {
        assertThat(GamificationRules.xpForCompletedRecipe("Hard")).isEqualTo(75)
    }
}
