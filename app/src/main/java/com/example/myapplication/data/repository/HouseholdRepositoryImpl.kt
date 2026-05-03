package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.HouseholdApi
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.model.HouseholdRegisterDTO
import com.example.myapplication.data.model.HouseholdWithUserInfo
import com.example.myapplication.data.model.UserHouseholdJoinDTO
import com.example.myapplication.data.model.UserHouseholdResponseDTO
import com.example.myapplication.data.model.UserResponse
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.Household
import com.example.myapplication.domain.repository.HouseholdRepository
import retrofit2.HttpException
import java.io.IOException

class HouseholdRepositoryImpl(
    private val api: HouseholdApi,
    private val preferencesManager: PreferenceManager
): HouseholdRepository {
    companion object {
        private const val TAG = "HouseholdRepository"
    }

    private fun getAuthHeader(): String? {
        val token = preferencesManager.getIdToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else null
    }

    override suspend fun createHousehold(name: String): Result<Household> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            Log.d(TAG, "Создание хозяйства: name=$name")
            val request = HouseholdRegisterDTO(name)
            val response = api.createHousehold(authHeader, request)

            Log.d(TAG, "Хозяйство создано: id=${response.id}, name=${response.name}")
            Log.d(TAG, "Invite code: ${response.inviteCode}")

            preferencesManager.saveCurrentHouseholdId(response.id)
            Result.Success(response)
        }catch (e: HttpException){
            val errorMessage = when (e.code()) {
                400 -> "Неверные данные"
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                409 -> "Хозяйство с таким именем уже существует"
                else -> "Ошибка сервера: ${e.code()}"
            }
            Log.e(TAG, "Ошибка создания хозяйства: $errorMessage")
            Result.Error(errorMessage)
        }catch (e: IOException) {
            Log.e(TAG, "Ошибка сети: ${e.message}")
            Result.Error("Ошибка сети. Проверьте подключение")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка: ${e.message}")
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getHousehold(householdId: String): Result<Household>{
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            Log.d(TAG, "Получение хозяйства: id=$householdId")
            val response = api.getHousehold(authHeader, householdId)
            Result.Success(response)
        }catch (e: HttpException){
            when (e.code()) {
                404 -> Result.Error("Хозяйство не найдено")
                else -> Result.Error("Ошибка: ${e.code()}")
            }
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка получения хозяйства")
        }
    }

    override suspend fun updateHousehold(
        householdId: String,
        name: String
    ): Result<Household> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            Log.d(TAG, "Обновление хозяйства: id=$householdId, name=$name")
            val request = HouseholdRegisterDTO(name)
            val response = api.updateHousehold(authHeader, householdId, request)
            Result.Success(response)
        }catch (e: HttpException) {
            Result.Error("Ошибка обновления: ${e.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления")
        }
    }

    override suspend fun deleteHousehold(householdId: String): Result<Unit> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            Log.d(TAG, "Удаление хозяйства: id=$householdId")
            val response = api.deleteHousehold(authHeader, householdId)

            if (response.isSuccessful) {
                preferencesManager.clearCurrentHouseholdId()
                Result.Success(Unit)
            } else {
                Result.Error("Ошибка удаления: ${response.code()}")
            }
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка удаления")
        }
    }

    override suspend fun getUserHouseholds(): Result<List<UserHouseholdResponseDTO>> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            val response = api.getUserHouseholds(authHeader)
            Result.Success(response)
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка получения списка")
        }
    }

    override suspend fun getUserHouseholdsWithDetails(): Result<List<HouseholdWithUserInfo>> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            val userHouseholds = api.getUserHouseholds(authHeader)
            val householdsWithInfo = mutableListOf<HouseholdWithUserInfo>()

            for (userHousehold in userHouseholds) {
                try {
                    val household = api.getHousehold(authHeader, userHousehold.householdId)
                    householdsWithInfo.add(HouseholdWithUserInfo(household, userHousehold))
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка получения хозяйства ${userHousehold.householdId}")
                    Result.Error(e.message ?: "Ошибка получения хозяйства")
                }
            }
            Result.Success(householdsWithInfo)
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка получения списка")
        }
    }

    override suspend fun joinHousehold(inviteCode: String): Result<UserHouseholdResponseDTO> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            val response = api.joinHousehold(authHeader, UserHouseholdJoinDTO(inviteCode))
            Result.Success(response)
        }catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Хозяйство с таким кодом не найдено"
                409 -> "Вы уже состоите в этом хозяйстве"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка вступления")
        }
    }

    override suspend fun leaveHousehold(householdId: String): Result<Unit> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            val response = api.leaveHousehold(authHeader, householdId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Ошибка: ${response.code()}")
            }
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка выхода")
        }
    }

    override suspend fun getHouseholdMembers(householdId: String): Result<List<UserResponse>> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            val response = api.getHouseholdMembers(authHeader, householdId)
            Result.Success(response)
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка получения участников")
        }
    }

    override suspend fun removeUserFromHousehold(
        householdId: String,
        userToRemoveId: String
    ): Result<Unit> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            val response = api.removeUserFromHousehold(authHeader, householdId, userToRemoveId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Ошибка: ${response.code()}")
            }
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка удаления пользователя")
        }
    }

}