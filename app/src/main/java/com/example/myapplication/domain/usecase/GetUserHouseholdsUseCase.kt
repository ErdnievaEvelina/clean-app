package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.HouseholdWithUserInfo
import com.example.myapplication.data.model.UserHouseholdResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.HouseholdRepository
import com.example.myapplication.domain.repository.UserRepository

class GetUserHouseholdsUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(): Result<List<HouseholdWithUserInfo>> {
        return repository.getUserHouseholdsWithDetails()
    }
}
class GetUserHouseholdsSummaryUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<UserHouseholdResponseDTO>> {
        return userRepository.getUserHouseholds()
    }
}