package com.example.myapplication.data.model.user

data class UserRegisterRequest(
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)