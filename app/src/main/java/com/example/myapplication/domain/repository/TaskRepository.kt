package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.task.TaskFilterType
import com.example.myapplication.data.model.task.TaskResponseDTO
import com.example.myapplication.data.model.task.TaskUiModel
import com.example.myapplication.domain.common.Result

interface TaskRepository {
    suspend fun getTasks(householdId: String, filter: TaskFilterType = TaskFilterType.ALL): Result<List<TaskUiModel>>
    suspend fun createTask(householdId: String, title: String, description: String?, reward: Int,): Result<TaskResponseDTO>
    suspend fun updateTask(taskId: String, title: String, description: String?, reward: Int, ): Result<TaskResponseDTO>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun assignTask(taskId: String): Result<TaskResponseDTO>
    suspend fun unassignTask(taskId: String): Result<TaskResponseDTO>
    suspend fun completeTask(taskId: String): Result<TaskResponseDTO>
}