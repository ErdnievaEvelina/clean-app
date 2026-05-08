package com.example.myapplication.data.model.task

data class TaskRegisterDTO(
    val title: String,
    val description: String? = null,
    val reward: Int = 0
)