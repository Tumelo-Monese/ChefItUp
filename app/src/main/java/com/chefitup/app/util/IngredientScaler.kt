package com.chefitup.app.util

import com.chefitup.app.domain.model.Ingredient
import kotlin.math.round

/**
 * Scales ingredient amounts when the user changes serving size.
 */
object IngredientScaler {

    fun scaleAmount(amount: Double, fromServings: Int, toServings: Int): Double {
        if (fromServings <= 0 || toServings <= 0) return amount
        val scaled = amount * (toServings.toDouble() / fromServings.toDouble())
        return roundToNice(scaled)
    }

    fun scaleIngredients(
        ingredients: List<Ingredient>,
        fromServings: Int,
        toServings: Int
    ): List<Ingredient> {
        if (fromServings == toServings) return ingredients
        return ingredients.map { ingredient ->
            val scaled = scaleAmount(ingredient.amount, fromServings, toServings)
            ingredient.copy(
                amount = scaled,
                original = formatOriginal(ingredient.name, scaled, ingredient.unit)
            )
        }
    }

    fun roundToNice(value: Double): Double {
        if (value == 0.0) return 0.0
        return when {
            value >= 100 -> round(value)
            value >= 10 -> round(value * 10.0) / 10.0
            value >= 1 -> round(value * 100.0) / 100.0
            else -> round(value * 1000.0) / 1000.0
        }
    }

    private fun formatOriginal(name: String, amount: Double, unit: String): String {
        val amountText = if (amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            amount.toString()
        }
        return listOf(amountText, unit, name)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
    }
}
