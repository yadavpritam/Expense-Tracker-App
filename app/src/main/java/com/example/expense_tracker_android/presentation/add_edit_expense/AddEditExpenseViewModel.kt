package com.example.expense_tracker_android.presentation.add_edit_expense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker_android.data.remote.dto.toCreateRequest
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
import com.google.gson.Gson
import android.util.Log

import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: String? = savedStateHandle.get<String>("expenseId")

    @RequiresApi(Build.VERSION_CODES.O)
    private val _state = MutableStateFlow(AddEditExpenseState())
    @RequiresApi(Build.VERSION_CODES.O)
    val state: StateFlow<AddEditExpenseState> = _state.asStateFlow()

    init {
        expenseId?.let { loadExpense(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: AddEditExpenseEvent) {
        when (event) {
            is AddEditExpenseEvent.AmountChanged -> {
                _state.update {
                    it.copy(
                        amount = event.amount,
                        amountError = null
                    )
                }
            }
            is AddEditExpenseEvent.DescriptionChanged -> {
                _state.update {
                    it.copy(
                        description = event.description,
                        descriptionError = null
                    )
                }
            }
            is AddEditExpenseEvent.DateChanged -> {
                _state.update { it.copy(date = event.date) }
            }
            is AddEditExpenseEvent.CategoryChanged -> {
                _state.update {
                    it.copy(
                        category = event.category,
                        categoryError = null
                    )
                }
            }
            is AddEditExpenseEvent.SaveExpense -> saveExpense()
            is AddEditExpenseEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadExpense(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.getExpense(id).fold(
                onSuccess = { expense ->
                    _state.update {
                        it.copy(
                            amount = expense.amount.toString(),
                            description = expense.description,
                            date = expense.date,
                            category = expense.category,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load expense"
                        )
                    }
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveExpense() {
        if (!validateForm()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // build expense domain model
            val expense = Expense(
                id = expenseId ?: "",
                amount = _state.value.amount.toDouble(),
                description = _state.value.description,
                date = _state.value.date,
                category = _state.value.category!!
            )

            // create request DTO (only once)
            val request = expense.toCreateRequest()

            // Log serialized JSON so we can inspect what we send
            val gson = Gson()
            Log.d("DEBUG_REQ", "CreateExpenseRequest JSON: ${gson.toJson(request)}")

            // Call repository (create or update)
            val result = if (expenseId != null) {
                repository.updateExpense(expenseId, expense)
            } else {
                repository.createExpense(expense)
            }

            // Handle result
            result.fold(
                onSuccess = { savedExpense ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSaved = true
                        )
                    }
                },
                onFailure = { err ->
                    // log error for debugging
                    Log.e("AddEditVM", "saveExpense failed", err)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "Failed to save expense"
                        )
                    }
                }
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateForm(): Boolean {
        val amount = _state.value.amount
        val description = _state.value.description
        val category = _state.value.category

        var hasError = false

        // Validate amount
        if (amount.isBlank()) {
            _state.update { it.copy(amountError = "Amount is required") }
            hasError = true
        } else {
            val amountValue = amount.toDoubleOrNull()
            if (amountValue == null) {
                _state.update { it.copy(amountError = "Invalid amount") }
                hasError = true
            } else if (amountValue <= 0) {
                _state.update { it.copy(amountError = "Amount must be greater than 0") }
                hasError = true
            }
        }

        // Validate description
        if (description.isBlank()) {
            _state.update { it.copy(descriptionError = "Description is required") }
            hasError = true
        } else if (description.length > 500) {
            _state.update { it.copy(descriptionError = "Description is too long (max 500 characters)") }
            hasError = true
        }

        // Validate category
        if (category == null) {
            _state.update { it.copy(categoryError = "Please select a category") }
            hasError = true
        }

        return !hasError
    }
}

data class AddEditExpenseState @RequiresApi(Build.VERSION_CODES.O) constructor(
    val amount: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val category: Category? = null,
    val amountError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed class AddEditExpenseEvent {
    data class AmountChanged(val amount: String) : AddEditExpenseEvent()
    data class DescriptionChanged(val description: String) : AddEditExpenseEvent()
    data class DateChanged(val date: LocalDate) : AddEditExpenseEvent()
    data class CategoryChanged(val category: Category) : AddEditExpenseEvent()
    object SaveExpense : AddEditExpenseEvent()
    object DismissError : AddEditExpenseEvent()
}

