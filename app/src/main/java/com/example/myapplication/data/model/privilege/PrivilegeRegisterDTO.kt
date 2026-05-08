package com.example.myapplication.data.model.privilege

data class PrivilegeRegisterDTO(
    val title: String,
    val description: String? = null,
    val cost: Int
)