    package com.example.expense_tracker_android.data.remote

    import com.example.expense_tracker_android.data.remote.dto.CreateExpenseRequest
    import com.example.expense_tracker_android.data.remote.dto.DashboardDto
    import com.example.expense_tracker_android.data.remote.dto.ExpenseDto
    import retrofit2.http.*

    interface ExpenseApiService {
        @GET("expenses")
        suspend fun getExpenses(
            @Query("userId") userId: String = "default_user",
            @Query("category") category: String? = null,
            @Query("startDate") startDate: String? = null,
            @Query("endDate") endDate: String? = null
        ): List<ExpenseDto>

        @GET("expenses/{id}")
        suspend fun getExpense(
            @Path("id") id: String
        ): ExpenseDto

        @POST("expenses")
        suspend fun createExpense(
            @Body expense: CreateExpenseRequest
        ): ExpenseDto

        @PUT("expenses/{id}")
        suspend fun updateExpense(
            @Path("id") id: String,
            @Body expense: CreateExpenseRequest
        ): ExpenseDto

        @DELETE("expenses/{id}")
        suspend fun deleteExpense(
            @Path("id") id: String
        ): DeleteResponse

        @GET("expenses/dashboard")
        suspend fun getDashboard(
            @Query("userId") userId: String = "default_user"
        ): DashboardDto
    }

    data class DeleteResponse(
        val message: String
    )
