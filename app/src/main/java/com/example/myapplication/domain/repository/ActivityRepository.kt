package com.example.myapplication.domain.repository
import android.util.Log
import com.example.myapplication.data.api.ActivityApi
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.model.ActivityActorScope
import com.example.myapplication.data.model.ActivityResponseDTO
import com.example.myapplication.data.model.ActivityType
import com.example.myapplication.domain.common.Result
import retrofit2.HttpException
import java.io.IOException

class ActivityRepository(
    private val api: ActivityApi,
    private val preferencesManager: PreferenceManager
) {
    companion object {
        private const val TAG = "ActivityRepository"
    }

    private fun getAuthHeader(): String? {
        val token = preferencesManager.getIdToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else null
    }

    suspend fun getHouseholdActivity(
        householdId: String,
        activityType: ActivityType? = null,
        actorScope: ActivityActorScope = ActivityActorScope.ALL
    ): Result<List<ActivityResponseDTO>> {
        return try {
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return Result.Error("Пользователь не авторизован")
            }

            val typeParam = activityType?.name
            val scopeParam = actorScope.name

            Log.d(TAG, "Получение активности: householdId=$householdId, type=$typeParam, scope=$scopeParam")

            val response = api.getHouseholdActivity(
                authHeader,
                householdId,
                typeParam,
                scopeParam
            )
            Result.Success(response)
        } catch (e: HttpException) {
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
            Result.Error(e.message ?: "Ошибка получения активности")
        }
    }
}