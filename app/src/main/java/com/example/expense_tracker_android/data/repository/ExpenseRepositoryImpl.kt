package com.example.expense_tracker_android.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.expense_tracker_android.data.remote.ExpenseApiService
import com.example.expense_tracker_android.data.remote.dto.toCreateRequest
import com.example.expense_tracker_android.data.remote.dto.toDomain
import com.example.expense_tracker_android.domain.model.Category
import com.example.expense_tracker_android.domain.model.CategoryTotal
import com.example.expense_tracker_android.domain.model.DashboardData
import com.example.expense_tracker_android.domain.model.Expense
import com.example.expense_tracker_android.domain.model.OverallTotal
import com.example.expense_tracker_android.domain.repository.ExpenseRepository
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val apiService: ExpenseApiService
) : ExpenseRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getExpenses(
        category: Category?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Result<List<Expense>> {
        return try {
            val response = apiService.getExpenses(
                category = category?.displayName,
                startDate = startDate?.atStartOfDay()?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                endDate = endDate?.atTime(23, 59, 59)?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            )
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getExpense(id: String): Result<Expense> {
        return try {
            val response = apiService.getExpense(id)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createExpense(expense: Expense): Result<Expense> {
        return try {
            val response = apiService.createExpense(expense.toCreateRequest())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            // If HttpException, try to read error body
            if (e is HttpException) {
                val errBody = try {
                    e.response()?.errorBody()?.string()
                } catch (ex: Exception) {
                    null
                }
                Log.e("Repo", "createExpense HTTP error: code=${e.code()}, body=$errBody")
            } else {
                Log.e("Repo", "createExpense error", e)
            }
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateExpense(id: String, expense: Expense): Result<Expense> {
        return try {
            val response = apiService.updateExpense(id, expense.toCreateRequest())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(id: String): Result<Unit> {
        return try {
            apiService.deleteExpense(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDashboard(): Result<DashboardData> {
        return try {
            val response = apiService.getDashboard()

            // Log raw response for debugging
            Log.d("REPO_DEBUG", "Dashboard raw response: $response")

            val dashboardData = DashboardData(
                categoryBreakdown = response.categoryBreakdown.map { dto ->
                    // Try map to Category; if unknown, still include serverLabel in domain model
                    val mappedCategory = Category.fromString(dto.category)
                    CategoryTotal(
                        category = mappedCategory,
                        total = dto.total,
                        count = dto.count
                    )
                },
                overall = OverallTotal(
                    total = response.overall.total,
                    count = response.overall.count
                )
            )
            Result.success(dashboardData)
        } catch (e: Exception) {
            Log.e("Repo", "getDashboard error", e)
            Result.failure(e)
        }
    }

}