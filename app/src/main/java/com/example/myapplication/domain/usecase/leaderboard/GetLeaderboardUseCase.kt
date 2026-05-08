package com.example.myapplication.domain.usecase.leaderboard
import com.example.myapplication.data.model.leaderbord.LeaderboardResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.LeaderboardRepository

class GetLeaderboardUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(householdId: String): Result<LeaderboardResponseDTO> {
        return repository.getLeaderboard(householdId)
    }
}