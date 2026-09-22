package com.chefitup.app.presentation.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.domain.model.ShoppingListItem
import com.chefitup.app.domain.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ShoppingListUiState(
    val items: List<ShoppingListItem> = emptyList(),
    val draft: String = "",
    val grouped: Map<String, List<ShoppingListItem>> = emptyMap()
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            shoppingListRepository.observeItems().collect { items ->
                _uiState.update {
                    it.copy(
                        items = items,
                        grouped = items.groupBy { item -> item.category.ifBlank { "Pantry" } }
                    )
                }
            }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun addCustom() {
        val name = _uiState.value.draft.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            shoppingListRepository.upsert(
                ShoppingListItem(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    amount = 0.0,
                    unit = "",
                    category = "Other",
                    isCustom = true
                )
            )
            _uiState.update { it.copy(draft = "") }
        }
    }

    fun setChecked(itemId: String, checked: Boolean) {
        viewModelScope.launch { shoppingListRepository.setChecked(itemId, checked) }
    }

    fun delete(itemId: String) {
        viewModelScope.launch { shoppingListRepository.delete(itemId) }
    }

    fun clearCompleted() {
        viewModelScope.launch { shoppingListRepository.clearCompleted() }
    }
}
