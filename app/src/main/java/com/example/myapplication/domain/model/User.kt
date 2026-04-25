package com.example.myapplication.domain.model

import java.time.LocalDateTime

data class User(
    val id: String,
    val firebaseUid: String,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime,
    val avatarUrl: String?
)
