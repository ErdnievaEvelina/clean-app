package com.example.myapplication.data.model.task

enum class TaskFilterType {
    ALL,        // все задачи
    FREE,       // все не выполненные и не забронированные (свободные)
    MY,         // все не выполненные и забронированные за юзером (мои)
    COMPLETED   // все выполненные
}
fun TaskFilterType.toApiString(): String {
    return when (this) {
        TaskFilterType.ALL -> "ALL"
        TaskFilterType.FREE -> "FREE"
        TaskFilterType.MY -> "MY"
        TaskFilterType.COMPLETED -> "COMPLETED"
    }
}
fun TaskResponseDTO.toUiModel(userNameMap: Map<String, String> = emptyMap()): TaskUiModel {
    val status = when {
        isCompleted -> TaskStatus.COMPLETED
        isAssigned -> TaskStatus.ASSIGNED
        else -> TaskStatus.OPEN
    }

    return TaskUiModel(
        id = id,
        title = title,
        description = description,
        reward = reward,
        status = status,
        assignedToUserId = assignedTo,
        assignedToUserName = assignedTo?.let { userNameMap[it] },
        createdByUserName = createdBy.let { userNameMap[it] },
        completedAt = completedAt
    )
}