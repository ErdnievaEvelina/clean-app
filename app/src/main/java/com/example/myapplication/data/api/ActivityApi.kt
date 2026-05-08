package com.example.myapplication.data.api

import com.example.myapplication.data.model.ActivityResponseDTO
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ActivityApi {
    @GET("/api/households/{householdId}/activity")
    suspend fun getHouseholdActivity(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String,
        @Query("activityType") activityType: String? = null,
        @Query("actorScope") actorScope: String = "ALL"
    ): List<ActivityResponseDTO>
}