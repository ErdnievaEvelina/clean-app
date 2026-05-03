package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.HouseholdRepository

class RemoveUserFromHouseholdUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String, userId: String): Result<Unit> {
        return repository.removeUserFromHousehold(householdId, userId)
    }
}