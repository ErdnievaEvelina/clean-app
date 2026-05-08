package com.example.myapplication.data.repository


import android.util.Log
import com.example.myapplication.data.api.TaskApi
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.model.task.TaskFilterType
import com.example.myapplication.data.model.task.TaskRegisterDTO
import com.example.myapplication.data.model.task.TaskResponseDTO
import com.example.myapplication.data.model.task.TaskUiModel
import com.example.myapplication.data.model.task.toApiString
import com.example.myapplication.data.model.task.toUiModel
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.TaskRepository
import retrofit2.HttpException
import java.io.IOException

class TaskRepositoryImpl(
    private val api: TaskApi,
    private val preferencesManager: PreferenceManager
): TaskRepository {
    companion object {
        private const val TAG = "TaskRepository"
    }

    private fun getAuthHeader(): String? {
        val token = preferencesManager.getIdToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else null
    }
    override suspend fun getTasks(
        householdId: String,
        filter: TaskFilterType
    ): Result<List<TaskUiModel>> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            Log.d(TAG, "Получение задач для хозяйства $householdId с фильтром $filter")
            val filterString = filter.toApiString()
            val response = api.getHouseholdTasks(authHeader, householdId, filterString)
            val userIds = mutableSetOf<String>()
            response.forEach { task ->
                userIds.add(task.createdBy)
                task.assignedTo?.let { userIds.add(it) }
                task.completedBy?.let { userIds.add(it) }
            }
            val userNameMap = emptyMap<String, String>()

            val uiModels = response.map { it.toUiModel(userNameMap) }
            Result.Success(uiModels)

        }catch (e: retrofit2.HttpException) {
            Result.Error("Ошибка: ${e.code()}")
        } catch (e: IOException) {
            Result.Error("Ошибка сети")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка получения задач")
        }
    }

    override suspend fun createTask(
        householdId: String,
        title: String,
        description: String?,
        reward: Int
    ): Result<TaskResponseDTO> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            Log.d(TAG, "Создание задачи: $title")
            val request = TaskRegisterDTO(title, description, reward)
            val response = api.createTask(authHeader, householdId, request)
            Result.Success(response)
        }catch (e: HttpException) {
            Result.Error("Ошибка: ${e.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка создания задачи")
        }
    }

    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        reward: Int
    ): Result<TaskResponseDTO> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            val request = TaskRegisterDTO(title, description, reward)
            val response = api.updateTask(authHeader, taskId, request)
            Result.Success(response)
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления")
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            val response = api.deleteTask(authHeader, taskId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Ошибка удаления: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка удаления")
        }
    }

    override suspend fun assignTask(taskId: String): Result<TaskResponseDTO> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            val response = api.assignTask(authHeader, taskId)
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка бронирования")
        }
    }

    override suspend fun unassignTask(taskId: String): Result<TaskResponseDTO> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            val response = api.unassignTask(authHeader, taskId)
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка снятия брони")
        }
    }

    override suspend fun completeTask(taskId: String): Result<TaskResponseDTO> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            val response = api.completeTask(authHeader, taskId)
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка выполнения")
        }
    }
}