package com.example.myapplication.data.repository


import android.util.Log
import com.example.myapplication.data.api.UserApi
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.model.household.UserHouseholdResponseDTO
import com.example.myapplication.data.model.user.UserUpdateDto
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository
import retrofit2.HttpException

class UserRepositoryImpl(
    private val api: UserApi,
    private val preferencesManager: PreferenceManager
): UserRepository{
    companion object {
        private const val TAG = "ProfileRepository"
    }
    override suspend fun getProfile(): Result<User> {
        return try {
            val authHeader = getAuthHeader()
            Log.d(TAG, "getProfile: authHeader = $authHeader")
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            Log.d(TAG, "Получение профиля...")
            val response = api.getProfile(authHeader)
            Log.d(TAG, "Профиль получен: ${response.email}")
            preferencesManager.saveUserData(
                idToken = preferencesManager.getIdToken() ?: "",
                firebaseUid = preferencesManager.getFirebaseUid() ?: "",
                userId = response.id,
                email = response.email,
                name = response.name
            )
            Result.Success(response)
        }catch (e: HttpException){
            val errorMessage = when (e.code()) {
                401 -> "Не авторизован. Войдите снова."
                403 -> "Доступ запрещен"
                404 -> "Пользователь не найден"
                else -> "Ошибка сервера: ${e.code()}"
            }
            Result.Error(errorMessage)
        }catch(e: Exception){
            Result.Error(e.message?:"Не получен профиль user")
        }
    }

    override suspend fun updateUser(
        name: String,
        avatarUrl:String?
    ): Result<User> {
        return try {
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }
            Log.d(TAG, "Обновление профиля: name=$name")
            val request = UserUpdateDto(name, avatarUrl)
            val response = api.updateUser(authHeader, request)

            preferencesManager.saveUserData(
                idToken = preferencesManager.getIdToken() ?: "",
                firebaseUid = preferencesManager.getFirebaseUid() ?: "",
                userId = response.id,
                email = response.email,
                name = response.name
            )

            Log.d(TAG, "Профиль обновлен успешно")
            Result.Success(response)

        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Неверные данные"
                401 -> "Не авторизован"
                409 -> "Email уже используется"
                else -> "Ошибка: ${e.code()}"
            }
            Result.Error(errorMessage)
        }catch(e: Exception){
            Result.Error(e.message?:"Не получен профиль user")
        }
    }

    override suspend fun syncEmailFromFirebase(): Result<User> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не неайден")
            }
            Log.d(TAG, "Синхронизация email из Firebase...")
            val response = api.syncEmailFromFirebase(authHeader)

            preferencesManager.saveUserData(
                idToken = preferencesManager.getIdToken() ?: "",
                firebaseUid = preferencesManager.getFirebaseUid() ?: "",
                userId = response.id,
                email = response.email,
                name = response.name
            )
            Result.Success(response)
        }catch (e: HttpException){
            val errorMessage = when (e.code()) {
                401 -> "Не авторизован"
                404 -> "Пользователь не найден"
                else -> "Ошибка синхронизации: ${e.code()}"
            }
            Result.Error(errorMessage)
        }catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка синхронизации")
        }
    }

    override suspend fun deleteUser(): Result<Unit> {
        return try{
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Профиль не авторизирован")
            }

            Log.d(TAG, "Удаление аккаунта...")
            val response = api.deleteUser(authHeader)

            if (response.isSuccessful) {
                preferencesManager.clear()
                Log.d(TAG, "Аккаунт удален успешно")
                Result.Success(Unit)
            } else {
                Result.Error("Ошибка удаления: ${response.code()}")
            }


        }catch(e: Exception){
            Result.Error(e.message?:"Ошибка удаления")
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

    private fun getAuthHeader(): String? {
        val token = preferencesManager.getIdToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else null
    }
}