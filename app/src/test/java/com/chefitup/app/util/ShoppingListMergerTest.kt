package com.chefitup.app.util

import com.chefitup.app.domain.model.ShoppingListItem
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShoppingListMergerTest {

    @Test
    fun merge_combinesDuplicateNameAndUnit() {
        val items = listOf(
            item("Chicken", 500.0, "g"),
            item("chicken", 1.0, "kg"),
            item("Chicken", 500.0, "g")
        )
        // Note: different units do not merge; same unit does.
        val sameUnit = listOf(
            item("Chicken", 500.0, "g"),
            item("chicken", 500.0, "g")
        )
        val merged = ShoppingListMerger.merge(sameUnit)
        assertThat(merged).hasSize(1)
        assertThat(merged[0].amount).isEqualTo(1000.0)
        assertThat(merged[0].name).isEqualTo("Chicken")
    }

    @Test
    fun merge_keepsDifferentUnitsSeparate() {
        val merged = ShoppingListMerger.merge(
            listOf(
                item("Chicken", 500.0, "g"),
                item("Chicken", 1.0, "kg")
            )
        )
        assertThat(merged).hasSize(2)
    }

    @Test
    fun mergeIntoExisting_addsNewItems() {
        val existing = listOf(item("Rice", 1.0, "cup", id = "a"))
        val incoming = listOf(item("Onion", 2.0, "", id = "b"))
        val merged = ShoppingListMerger.mergeIntoExisting(existing, incoming)
        assertThat(merged).hasSize(2)
    }

    @Test
    fun mergeKey_normalizesWhitespaceAndCase() {
        assertThat(ShoppingListMerger.mergeKey("  Tomato  Paste ", "g"))
            .isEqualTo(ShoppingListMerger.mergeKey("tomato paste", "G"))
    }

    private fun item(
        name: String,
        amount: Double,
        unit: String,
        id: String = ShoppingListMerger.newId()
    ) = ShoppingListItem(
        id = id,
        name = name,
        amount = amount,
        unit = unit,
        category = "Meat"
    )
}
