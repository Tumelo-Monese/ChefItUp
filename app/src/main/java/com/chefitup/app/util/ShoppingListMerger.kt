package com.chefitup.app.util

import com.chefitup.app.domain.model.ShoppingListItem
import java.util.UUID

/**
 * Pure merge logic for shopping-list ingredients (duplicate name+unit combined).
 */
object ShoppingListMerger {

    fun merge(items: List<ShoppingListItem>): List<ShoppingListItem> {
        if (items.isEmpty()) return emptyList()
        val grouped = linkedMapOf<String, ShoppingListItem>()
        for (item in items) {
            val key = mergeKey(item.name, item.unit)
            val existing = grouped[key]
            if (existing == null) {
                grouped[key] = item.copy(
                    name = normalizeDisplayName(item.name),
                    unit = item.unit.trim()
                )
            } else {
                grouped[key] = existing.copy(
                    amount = IngredientScaler.roundToNice(existing.amount + item.amount),
                    isChecked = existing.isChecked && item.isChecked,
                    category = preferCategory(existing.category, item.category),
                    recipeId = existing.recipeId ?: item.recipeId,
                    isCustom = existing.isCustom || item.isCustom
                )
            }
        }
        return grouped.values
            .sortedWith(compareBy({ it.category.lowercase() }, { it.name.lowercase() }))
    }

    fun mergeIntoExisting(
        existing: List<ShoppingListItem>,
        incoming: List<ShoppingListItem>
    ): List<ShoppingListItem> = merge(existing + incoming)

    fun mergeKey(name: String, unit: String): String =
        "${normalizeName(name)}|${unit.trim().lowercase()}"

    fun normalizeName(name: String): String =
        name.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun normalizeDisplayName(name: String): String {
        val trimmed = name.trim().replace(Regex("\\s+"), " ")
        return trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun preferCategory(a: String, b: String): String {
        if (a.isNotBlank() && a != "Pantry") return a
        if (b.isNotBlank()) return b
        return a.ifBlank { "Pantry" }
    }

    fun newId(): String = UUID.randomUUID().toString()
}
