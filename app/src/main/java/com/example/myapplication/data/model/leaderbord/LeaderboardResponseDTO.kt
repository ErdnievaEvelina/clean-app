package com.example.myapplication.data.model.leaderbord

data class LeaderboardResponseDTO(
    val periodDays: Int,
    val items: List<LeaderboardItemResponseDTO>
)

data class LeaderboardItemResponseDTO(
    val place: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val earnedCoins: Long,
    val earnedCoinsDelta: Long,
    val completedTasksCount: Long,
    val completedTasksDelta: Long,
    val isCurrentUser: Boolean
)