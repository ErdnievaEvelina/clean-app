package com.example.myapplication.domain.model

data class Household(
    val id: String,  // UUID как строка
    val name: String,
    val inviteCode: String,
    val createdAt: String,
    val createdByUser: String,  // UUID создателя
    val isActive: Boolean
)
