package com.example.myapplication.data.api

import com.example.myapplication.data.model.leaderbord.LeaderboardResponseDTO
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface LeaderboardApi {
    @GET("/api/households/{householdId}/leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String
    ): LeaderboardResponseDTO
}