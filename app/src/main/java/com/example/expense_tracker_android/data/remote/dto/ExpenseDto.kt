package com.example.expense_tracker_android.data.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.expense_tracker_android.domain.model.Category
import com.example.expense_tracker_android.domain.model.Expense
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.LocalDateTime

data class ExpenseDto(
    @SerializedName("_id")
    val id: String? = null,
    val amount: Double,
    val description: String,
    val date: String, // ISO 8601 format
    val category: String,
    val userId: String = "default_user",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CreateExpenseRequest(
    val amount: Double,
    val description: String,
    val date: String,
    val category: String,
    val userId: String = "default_user"
)

data class DashboardDto(
    val categoryBreakdown: List<CategoryTotalDto>,
    val overall: OverallTotalDto
)

data class CategoryTotalDto(
    @SerializedName("_id")
    val category: String?,
    val total: Double,
    val count: Int
)

data class OverallTotalDto(
    val total: Double,
    val count: Int
)

// Extension functions for mapping
@RequiresApi(Build.VERSION_CODES.O)
fun ExpenseDto.toDomain(): Expense {
    return Expense(
        id = id ?: "",
        amount = amount,
        description = description,
        date = parseDate(date),
        category = Category.fromString(category),
        userId = userId
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun Expense.toDto(): ExpenseDto {
    return ExpenseDto(
        id = if (id.isNotEmpty()) id else null,
        amount = amount,
        description = description,
        date = date.format(DateTimeFormatter.ISO_DATE), // Simple format use karein
        category = category.name,
        userId = userId
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun Expense.toCreateRequest(): CreateExpenseRequest {
    return CreateExpenseRequest(
        amount = amount,
        description = description,
        date = date.format(DateTimeFormatter.ISO_DATE), // Simple date format
        category = category.displayName,
        userId = userId
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseDate(dateString: String): LocalDate {
    return try {
        // Pehle ZonedDateTime se try karein
        ZonedDateTime.parse(dateString).toLocalDate()
    } catch (e: DateTimeParseException) {
        try {
            // Phir LocalDateTime se try karein
            LocalDateTime.parse(dateString).toLocalDate()
        } catch (e: DateTimeParseException) {
            try {
                // Direct LocalDate se try karein
                LocalDate.parse(dateString)
            } catch (e: DateTimeParseException) {
                try {
                    // Custom format se try karein
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    LocalDateTime.parse(dateString, formatter).toLocalDate()
                } catch (e: DateTimeParseException) {
                    try {
                        // Another common format
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        LocalDateTime.parse(dateString, formatter).toLocalDate()
                    } catch (e: DateTimeParseException) {
                        // Last resort - extract date part only
                        try {
                            val datePart = dateString.substringBefore("T")
                            LocalDate.parse(datePart)
                        } catch (e: Exception) {
                            println("Failed to parse date: $dateString, using current date. Error: ${e.message}")
                            LocalDate.now()
                        }
                    }
                }
            }
        }
    }
}