package com.example.myapplication.data.model.household

data class HouseholdResponseDTO(
    val id: String,  // UUID как строка
    val name: String,
    val inviteCode: String,
    val createdAt: String,
    val createdByUser: String,  // UUID создателя
    val isActive: Boolean
)