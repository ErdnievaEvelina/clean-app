package com.example.myapplication.domain.usecase.task

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        return repository.deleteTask(taskId)
    }
}