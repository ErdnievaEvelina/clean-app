package com.example.myapplication.data.api

import com.example.myapplication.data.model.UserRegisterRequest
import com.example.myapplication.data.model.UserUpdateDto
import com.example.myapplication.domain.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface UserApi {

    @GET("/api/users/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String?
    ): User
    @PUT("/api/users/me")
    suspend fun updateUser(
        @Header("Authorization") authorization: String?,
        @Body request: UserUpdateDto
    ): User

    //Синхронизация email из Firebase
    @PUT("/api/users/me/email/sync")
    suspend fun syncEmailFromFirebase(
        @Header("Authorization") authorization: String?
    ): User

    @DELETE("/api/users/me")
    suspend fun deleteUser(
        @Header("Authorization") authorization: String?
    ): Response<Unit>
    companion object{
        const val URL="http://10.0.2.2:8080"
    }
}