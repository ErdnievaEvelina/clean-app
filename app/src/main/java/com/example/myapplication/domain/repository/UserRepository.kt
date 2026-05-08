package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.household.UserHouseholdResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateUser(name:String,avatarUrl:String?): Result<User>
    suspend fun syncEmailFromFirebase(): Result<User>
    suspend fun deleteUser(): Result<Unit>
    suspend fun getUserHouseholds(): Result<List<UserHouseholdResponseDTO>>
}