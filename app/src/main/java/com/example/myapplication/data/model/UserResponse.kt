package com.example.myapplication.data.model

data class UserResponse(
    val id: String,
    val firebaseUid: String,
    val name: String,
    val email: String,
    val createdAt: String,
    val avatarUrl: String?
)
