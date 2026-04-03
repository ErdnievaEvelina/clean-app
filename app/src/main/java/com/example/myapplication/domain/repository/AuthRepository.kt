package com.example.myapplication.domain.repository

import com.example.myapplication.domain.common.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email:String,password:String): Result<Unit>
    suspend fun register(email: String,password: String, name: String):Result<Unit>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>

}