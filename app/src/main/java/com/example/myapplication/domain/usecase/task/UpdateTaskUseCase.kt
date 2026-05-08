package com.example.myapplication.domain.usecase.task

import com.example.myapplication.data.model.task.TaskResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.TaskRepository

class UpdateTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(
        taskId: String,
        title: String,
        description: String?,
        reward: Int
    ): Result<TaskResponseDTO>{
        if (title.isBlank()) {
            return Result.Error("Введите название задачи")
        }
        return repository.updateTask(taskId, title, description, reward)
    }
}