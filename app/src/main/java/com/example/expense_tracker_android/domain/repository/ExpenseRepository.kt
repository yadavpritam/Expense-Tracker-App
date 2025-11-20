package com.example.expense_tracker_android.domain.repository


import com.example.expense_tracker_android.domain.model.Category
import com.example.expense_tracker_android.domain.model.DashboardData
import com.example.expense_tracker_android.domain.model.Expense
import java.time.LocalDate

interface ExpenseRepository {
    suspend fun getExpenses(
        category: Category? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Result<List<Expense>>

    suspend fun getExpense(id: String): Result<Expense>

    suspend fun createExpense(expense: Expense): Result<Expense>

    suspend fun updateExpense(id: String, expense: Expense): Result<Expense>

    suspend fun deleteExpense(id: String): Result<Unit>

    suspend fun getDashboard(): Result<DashboardData>
}