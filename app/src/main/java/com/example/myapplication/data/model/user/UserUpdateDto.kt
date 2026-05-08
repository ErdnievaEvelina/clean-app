package com.example.myapplication.data.model.user

data class UserUpdateDto(
    val name: String,
    val avatarUrl: String? = null
)