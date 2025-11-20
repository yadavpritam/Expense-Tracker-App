package com.example.expense_tracker_android.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker_android.domain.model.DashboardData
import com.example.expense_tracker_android.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.LoadDashboard -> loadDashboard()
            is DashboardEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getDashboard().fold(
                onSuccess = { data ->
                    _state.update {
                        it.copy(
                            dashboardData = data,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load dashboard"
                        )
                    }
                }
            )
        }
    }
}

data class DashboardState(
    val dashboardData: DashboardData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class DashboardEvent {
    object LoadDashboard : DashboardEvent()
    object DismissError : DashboardEvent()
}
