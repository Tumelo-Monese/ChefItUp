package com.chefitup.app.util

import com.chefitup.app.domain.model.Ingredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IngredientScalerTest {

    @Test
    fun scaleAmount_doublesWhenServingsDouble() {
        assertThat(IngredientScaler.scaleAmount(2.0, fromServings = 2, toServings = 4))
            .isEqualTo(4.0)
    }

    @Test
    fun scaleAmount_halvesWhenServingsHalve() {
        assertThat(IngredientScaler.scaleAmount(100.0, fromServings = 4, toServings = 2))
            .isEqualTo(50.0)
    }

    @Test
    fun scaleAmount_returnsOriginalWhenInvalidServings() {
        assertThat(IngredientScaler.scaleAmount(5.0, fromServings = 0, toServings = 3))
            .isEqualTo(5.0)
    }

    @Test
    fun scaleIngredients_updatesAmountsAndOriginal() {
        val ingredients = listOf(
            Ingredient(name = "flour", amount = 200.0, unit = "g", original = "200 g flour"),
            Ingredient(name = "milk", amount = 1.0, unit = "cup", original = "1 cup milk")
        )
        val scaled = IngredientScaler.scaleIngredients(ingredients, fromServings = 2, toServings = 4)
        assertThat(scaled[0].amount).isEqualTo(400.0)
        assertThat(scaled[1].amount).isEqualTo(2.0)
        assertThat(scaled[0].original).contains("flour")
    }

    @Test
    fun roundToNice_keepsReasonablePrecision() {
        assertThat(IngredientScaler.roundToNice(12.34)).isEqualTo(12.3)
        assertThat(IngredientScaler.roundToNice(0.1234)).isEqualTo(0.123)
    }
}
