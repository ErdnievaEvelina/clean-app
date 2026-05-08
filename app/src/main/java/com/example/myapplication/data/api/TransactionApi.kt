package com.example.myapplication.data.api

import com.example.myapplication.data.model.transaction.TransactionResponseDTO
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface TransactionApi {
    @GET("/api/households/{householdId}/transactions/my")
    suspend fun getMyTransactions(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String
    ): List<TransactionResponseDTO>

}