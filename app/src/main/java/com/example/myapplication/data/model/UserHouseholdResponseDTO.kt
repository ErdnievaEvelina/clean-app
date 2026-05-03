package com.example.myapplication.data.model

data class UserHouseholdResponseDTO(
    val id: String,
    val householdId: String,
    val balance: Int,
    val joinedAt: String,
    val isUserActive: Boolean
)
