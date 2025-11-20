package com.example.expense_tracker_android.presentation.expense_list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker_android.domain.model.Category
import com.example.expense_tracker_android.domain.model.Expense
import com.example.expense_tracker_android.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseListState())
    val state: StateFlow<ExpenseListState> = _state.asStateFlow()

    init {
        loadExpenses()
    }

    fun onEvent(event: ExpenseListEvent) {
        when (event) {
            is ExpenseListEvent.LoadExpenses -> loadExpenses()
            is ExpenseListEvent.FilterByCategory -> filterByCategory(event.category)
            is ExpenseListEvent.FilterByDateRange -> filterByDateRange(event.startDate, event.endDate)
            is ExpenseListEvent.ClearFilters -> clearFilters()
            is ExpenseListEvent.DeleteExpense -> deleteExpense(event.id)
            is ExpenseListEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getExpenses(
                category = _state.value.selectedCategory,
                startDate = _state.value.startDate,
                endDate = _state.value.endDate
            ).fold(
                onSuccess = { expenses ->
                    _state.update {
                        it.copy(
                            expenses = expenses,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load expenses"
                        )
                    }
                }
            )
        }
    }

    private fun filterByCategory(category: Category?) {
        _state.update { it.copy(selectedCategory = category) }
        loadExpenses()
    }

    private fun filterByDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        _state.update {
            it.copy(
                startDate = startDate,
                endDate = endDate
            )
        }
        loadExpenses()
    }

    private fun clearFilters() {
        _state.update {
            it.copy(
                selectedCategory = null,
                startDate = null,
                endDate = null
            )
        }
        loadExpenses()
    }

    private fun deleteExpense(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.deleteExpense(id).fold(
                onSuccess = {
                    loadExpenses()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete expense"
                        )
                    }
                }
            )
        }
    }
}

data class ExpenseListState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: Category? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

sealed class ExpenseListEvent {
    object LoadExpenses : ExpenseListEvent()
    data class FilterByCategory(val category: Category?) : ExpenseListEvent()
    data class FilterByDateRange(val startDate: LocalDate?, val endDate: LocalDate?) : ExpenseListEvent()
    object ClearFilters : ExpenseListEvent()
    data class DeleteExpense(val id: String) : ExpenseListEvent()
    object DismissError : ExpenseListEvent()
}
