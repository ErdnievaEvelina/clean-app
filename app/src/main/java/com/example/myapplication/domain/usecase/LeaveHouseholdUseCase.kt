package com.example.myapplication.domain.usecase
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.HouseholdRepository

class LeaveHouseholdUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String): Result<Unit> {
        return repository.leaveHousehold(householdId)
    }
}