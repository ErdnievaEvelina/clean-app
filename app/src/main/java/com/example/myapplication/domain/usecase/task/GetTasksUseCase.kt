package com.example.myapplication.domain.usecase.task

import com.example.myapplication.data.model.task.TaskFilterType
import com.example.myapplication.data.model.task.TaskUiModel
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.TaskRepository

class GetTasksUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(
        householdId: String,
        filter: TaskFilterType = TaskFilterType.ALL
    ): Result<List<TaskUiModel>>{
        return repository.getTasks(householdId,filter)
    }
}