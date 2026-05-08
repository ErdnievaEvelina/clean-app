package com.example.myapplication.data.model.household

data class UserHouseholdResponseDTO(
    val id: String,
    val householdId: String,
    val balance: Int,
    val joinedAt: String,
    val isUserActive: Boolean
)