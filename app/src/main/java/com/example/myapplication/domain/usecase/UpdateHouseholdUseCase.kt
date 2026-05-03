package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.Household
import com.example.myapplication.domain.repository.HouseholdRepository

class UpdateHouseholdUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String, name: String): Result<Household> {
        if (name.isBlank()) {
            return Result.Error("Введите название хозяйства")
        }
        return repository.updateHousehold(householdId, name)
    }
}