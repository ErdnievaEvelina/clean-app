package com.example.myapplication.domain.repository

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateUser(email:String,name:String): Result<User>
    suspend fun deleteUser(): Result<Unit>
}