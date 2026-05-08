package com.example.myapplication.data.api

import com.example.myapplication.data.model.user.UserRegisterRequest
import com.example.myapplication.data.model.user.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/register")
    suspend fun registerUser(
        @Header("Authorization") authorization: String,
        @Body request: UserRegisterRequest
    ): UserResponse

    @GET("/api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserResponse

}