package com.example.myapplication.data.model.task

data class TaskResponseDTO(
    val id: String,  // UUID
    val householdId: String,  // UUID хозяйства
    val createdBy: String,  // UUID создателя
    val createdAt: String,  // LocalDateTime

    val title: String,
    val description: String? = null,
    val reward: Int,

    val isAssigned: Boolean = false,
    val assignedTo: String? = null,  // UUID пользователя
    val assignedAt: String? = null,  // LocalDateTime

    val isCompleted: Boolean = false,
    val completedBy: String? = null,  // UUID пользователя
    val completedAt: String? = null   // LocalDateTime
)
