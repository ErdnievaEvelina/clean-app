package com.example.myapplication.domain.usecase.task

import com.example.myapplication.data.model.task.TaskResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.TaskRepository

class AssignTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<TaskResponseDTO> {
        return repository.assignTask(taskId)
    }
}