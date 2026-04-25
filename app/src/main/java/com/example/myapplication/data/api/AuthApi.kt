package com.example.myapplication.data.api

import com.example.myapplication.data.model.UserRegisterRequest
import com.example.myapplication.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/register")
    suspend fun registerUser(
        @Header("Authorization") authorization: String,  // "Bearer {idToken}"
        @Body request: UserRegisterRequest
    ): UserResponse

    @GET("/api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserResponse

    companion object{
        const val URL="http://10.0.2.2:8080"
        //const val URL = "192.168.0.13:8080"
    }
}