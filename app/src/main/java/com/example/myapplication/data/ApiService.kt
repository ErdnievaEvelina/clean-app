package com.example.myapplication.data

import com.example.myapplication.data.model.UserRegisterRequest
import com.example.myapplication.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("/api/auth/register")
    suspend fun registerUser(
        @Header("Authorization") authorization: String,  // "Bearer {idToken}"
        @Body request: UserRegisterRequest
    ): UserResponse

    @GET("/api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserResponse

    @PUT("/api/users/me")
    suspend fun updateUser(
        @Header("Authorization") authorization: String,
        @Body request: UserRegisterRequest
    ): UserResponse

    @DELETE("/api/users/me")
    suspend fun deleteUser(
        @Header("Authorization") authorization: String
    ): Response<Unit>
    companion object{
        const val URL="http://10.0.2.2:8080"
    }
}