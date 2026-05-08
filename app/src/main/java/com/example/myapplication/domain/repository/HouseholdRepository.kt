package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.household.HouseholdWithUserInfo
import com.example.myapplication.data.model.household.UserHouseholdResponseDTO
import com.example.myapplication.data.model.user.UserResponse
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.Household

interface HouseholdRepository {
    suspend fun createHousehold(name:String): Result<Household>
    suspend fun getHousehold(householdId: String): Result<Household>
    suspend fun updateHousehold(householdId: String, name: String): Result<Household>
    suspend fun deleteHousehold(householdId: String): Result<Unit>
    suspend fun getUserHouseholds(): Result<List<UserHouseholdResponseDTO>>
    suspend fun getUserHouseholdsWithDetails(): Result<List<HouseholdWithUserInfo>>
    suspend fun joinHousehold(inviteCode: String): Result<UserHouseholdResponseDTO>
    suspend fun leaveHousehold(householdId: String): Result<Unit>
    suspend fun getHouseholdMembers(householdId: String): Result<List<UserResponse>>
    suspend fun removeUserFromHousehold(householdId: String, userToRemoveId: String): Result<Unit>
}