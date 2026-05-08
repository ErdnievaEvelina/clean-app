package com.example.myapplication.data.model.task

data class TaskUiModel(
    val id: String,
    val title: String,
    val description: String?,
    val reward: Int,
    val status: TaskStatus,
    val assignedToUserId: String?,
    val assignedToUserName: String?,
    val createdByUserName: String?,
    val completedAt: String?
)
enum class TaskStatus {
    OPEN,      // Свободна - не назначена и не выполнена
    ASSIGNED,  // Забронирована - назначена, но не выполнена
    COMPLETED  // Выполнена
}
