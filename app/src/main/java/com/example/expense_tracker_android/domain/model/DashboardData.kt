package com.example.expense_tracker_android.domain.model

data class DashboardData(
    val categoryBreakdown: List<CategoryTotal>,
    val overall: OverallTotal
)

data class CategoryTotal(
    val category: Category,
    val total: Double,
    val count: Int
)

data class OverallTotal(
    val total: Double,
    val count: Int
)
