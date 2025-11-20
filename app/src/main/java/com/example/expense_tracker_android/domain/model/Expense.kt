package com.example.expense_tracker_android.domain.model

import java.time.LocalDate

data class Expense(
    val id: String = "",
    val amount: Double,
    val description: String,
    val date: LocalDate,
    val category: Category,
    val userId: String = "default_user"
)

enum class Category(val displayName: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): Category {
            if (value.isNullOrBlank()) return OTHER

            val trimmed = value.trim()

            // 1) match enum name (ENTERTAINMENT)
            entries.find { it.name.equals(trimmed, ignoreCase = true) }?.let { return it }

            // 2) match displayName (Entertainment)
            entries.find { it.displayName.equals(trimmed, ignoreCase = true) }?.let { return it }

            // 3) match lowercase displayName variants (just in case)
            entries.find { it.displayName.equals(trimmed.capitalize(), ignoreCase = true) }?.let { return it }

            // 4) last resort: if server sends something like "entertainment" (lowercase)
            entries.find { it.displayName.equals(trimmed, ignoreCase = true) }?.let { return it }

            // fallback
            return OTHER
        }
    }


}