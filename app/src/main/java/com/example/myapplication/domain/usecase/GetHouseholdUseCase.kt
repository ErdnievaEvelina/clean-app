package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.Household
import com.example.myapplication.domain.repository.HouseholdRepository

class GetHouseholdUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String): Result<Household> {
        return repository.getHousehold(householdId)
    }
}