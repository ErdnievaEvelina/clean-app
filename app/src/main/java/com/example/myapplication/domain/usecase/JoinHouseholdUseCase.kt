package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.household.UserHouseholdResponseDTO
import com.example.myapplication.domain.repository.HouseholdRepository
import com.example.myapplication.domain.common.Result

class JoinHouseholdUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(inviteCode: String): Result<UserHouseholdResponseDTO> {
        if (inviteCode.isBlank()) {
            return Result.Error("Введите invite-код")
        }
        return repository.joinHousehold(inviteCode)
    }
}