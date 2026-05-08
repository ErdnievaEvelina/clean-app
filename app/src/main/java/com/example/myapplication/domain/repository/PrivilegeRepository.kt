package com.example.myapplication.domain.repository

import android.util.Log
import com.example.myapplication.data.api.PrivilegeApi
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.model.privilege.PrivilegeRegisterDTO
import com.example.myapplication.data.model.privilege.PrivilegeResponseDTO
import com.example.myapplication.data.model.privilege.PrivilegeUiModel
import com.example.myapplication.data.model.privilege.map.PrivilegeFilterType
import com.example.myapplication.data.model.privilege.map.toApiString
import com.example.myapplication.data.model.privilege.map.toUiModel
import com.example.myapplication.domain.common.Result
import retrofit2.HttpException
import java.io.IOException

class PrivilegeRepository(
    private val api: PrivilegeApi,
    private val preferencesManager: PreferenceManager
) {
    companion object {
        private const val TAG = "PrivilegeRepository"
    }

    private fun getAuthHeader(): String? {
        val token = preferencesManager.getIdToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else null
    }
    suspend fun getPrivileges(
        householdId: String,
        filter: PrivilegeFilterType = PrivilegeFilterType.ALL
    ): Result<List<PrivilegeUiModel>>{
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }

            val filterString = filter.toApiString()
            Log.d(TAG, "Получение привилегий: householdId=$householdId, filter=$filterString")

            val privileges = api.getPrivileges(authHeader, householdId, filterString)

            val members = api.getHouseholdMembers(authHeader, householdId)
            val userNameMap = members.associate { it.id to it.name }

            Log.d(TAG, "Загружено членов хозяйства: ${members.size}")
            Log.d(TAG, "Карта имен: $userNameMap")

            val uiModels = privileges.map { it.toUiModel(userNameMap) }

            uiModels.forEach { model ->
                Log.d(TAG, "Модель: ${model.title}, createdByName=${model.createdByName}, boughtByName=${model.boughtByName}")
            }

            Result.Success(uiModels)
        }catch (e: HttpException) {
            Log.e(TAG, "HTTP ошибка: ${e.code()}")
            val errorMessage = when (e.code()) {
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                404 -> "Хозяйство не найдено"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        } catch (e: IOException) {
            Result.Error("Ошибка сети")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка получения привилегий")
        }

    }
    suspend fun createPrivilege(
        householdId: String,
        title: String,
        description: String?,
        cost: Int
    ): Result<PrivilegeResponseDTO>{
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")

            }
            if (title.length < 2 || title.length > 120) {
                Result.Error("Название должно быть от 2 до 120 символов")
            }
            if (description != null && description.length > 2000) {
                return Result.Error("Описание не может превышать 2000 символов")
            }
            if (cost < 5 || cost > 500) {
                return Result.Error("Стоимость должна быть от 5 до 500")
            }

            Log.d(TAG, "Создание привилегии: $title, cost=$cost")
            val request = PrivilegeRegisterDTO(title, description, cost)
            val response = api.createPrivilege(authHeader, householdId, request)
            Result.Success(response)

        }catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Неверные данные"
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка создания привилегии")
        }

    }
    suspend fun buyPrivilege(privilegeId: String): Result<PrivilegeResponseDTO>{
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")

            }
            Log.d(TAG, "Покупка привилегии: $privilegeId")
            val response = api.buyPrivilege(authHeader, privilegeId)
            Result.Success(response)
        }catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Неверные данные"
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка создания привилегии")
        }
    }
    suspend fun deletePrivilege(privilegeId: String): Result<Unit>{
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")

            }
            val response = api.deletePrivilege(authHeader, privilegeId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Ошибка удаления: ${response.code()}")
            }

        }catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Неверные данные"
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка создания привилегии")
        }
    }
    suspend fun getPrivilegeById(privilegeId: String): Result<PrivilegeResponseDTO> {
        return try {
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }

            Log.d(TAG, "Получение привилегии по ID: $privilegeId")
            val response = api.getPrivilegeById(authHeader, privilegeId)
            Result.Success(response)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                404 -> "Привилегия не найдена"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка получения привилегии")
        }
    }
    suspend fun updatePrivilege(
        privilegeId: String,
        title: String,
        description: String?,
        cost: Int
    ): Result<PrivilegeResponseDTO> {
        return try {
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }

            if (title.length < 2 || title.length > 120) {
                return Result.Error("Название должно быть от 2 до 120 символов")
            }
            if (description != null && description.length > 2000) {
                return Result.Error("Описание не может превышать 2000 символов")
            }
            if (cost < 5 || cost > 500) {
                return Result.Error("Стоимость должна быть от 5 до 500")
            }

            Log.d(TAG, "Обновление привилегии: $privilegeId, title=$title, cost=$cost")
            val request = PrivilegeRegisterDTO(title, description, cost)
            val response = api.updatePrivilege(authHeader, privilegeId, request)
            Result.Success(response)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Неверные данные"
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                404 -> "Привилегия не найдена"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления привилегии")
        }
    }
}