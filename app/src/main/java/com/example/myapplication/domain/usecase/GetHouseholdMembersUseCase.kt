package com.example.myapplication.domain.usecase
import com.example.myapplication.data.model.user.UserResponse
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.HouseholdRepository

class GetHouseholdMembersUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String): Result<List<UserResponse>>{
        return repository.getHouseholdMembers(householdId)
    }
}