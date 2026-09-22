package com.chefitup.app.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IngredientChip(
    label: String,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onRemove != null) {
        InputChip(
            selected = true,
            onClick = {},
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            },
            modifier = modifier
        )
    } else {
        AssistChip(
            onClick = {},
            label = { Text(label) },
            modifier = modifier
        )
    }
}

@Composable
fun FilterChipRow(
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    allowDeselect: Boolean = true
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            FilterChip(
                selected = isSelected,
                onClick = {
                    onSelected(
                        if (isSelected && allowDeselect) null else option
                    )
                },
                label = { Text(option) }
            )
        }
    }
}
