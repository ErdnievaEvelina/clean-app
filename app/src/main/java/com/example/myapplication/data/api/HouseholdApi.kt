package com.example.myapplication.data.api

import com.example.myapplication.data.model.household.HouseholdRegisterDTO
import com.example.myapplication.data.model.household.UserHouseholdJoinDTO
import com.example.myapplication.data.model.household.UserHouseholdResponseDTO
import com.example.myapplication.data.model.user.UserResponse
import com.example.myapplication.domain.model.Household
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HouseholdApi {
    @POST("/api/households")
    suspend fun createHousehold(
        @Header("Authorization") authorization: String?,
        @Body request: HouseholdRegisterDTO
    ): Household

    @GET("/api/households/{householdId}")
    suspend fun getHousehold(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String
    ): Household

    @PUT("/api/households/{householdId}")
    suspend fun updateHousehold(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String,
        @Body request: HouseholdRegisterDTO
    ): Household

    @DELETE("/api/households/{householdId}")
    suspend fun deleteHousehold(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String
    ): retrofit2.Response<Unit>
    @POST("/api/households/join")
    suspend fun joinHousehold(
        @Header("Authorization") authorization: String,
        @Body request: UserHouseholdJoinDTO
    ): UserHouseholdResponseDTO

    @DELETE("/api/households/{householdId}/leave")
    suspend fun leaveHousehold(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String
    ): retrofit2.Response<Unit>

    @GET("/api/households/myHouseholds")
    suspend fun getUserHouseholds(
        @Header("Authorization") authorization: String
    ): List<UserHouseholdResponseDTO>
    @GET("/api/households/{householdId}/members")
    suspend fun getHouseholdMembers(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String
    ): List<UserResponse>

    @DELETE("/api/households/{householdId}/members/{userToRemoveId}")
    suspend fun removeUserFromHousehold(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String,
        @Path("userToRemoveId") userToRemoveId: String
    ): retrofit2.Response<Unit>

}