package com.example.myapplication.domain.repository

import android.util.Log
import com.example.myapplication.data.api.TransactionApi
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.model.transaction.TransactionResponseDTO
import com.example.myapplication.domain.common.Result
import retrofit2.HttpException
import java.io.IOException

class TransactionRepository(
    private val api: TransactionApi,
    private val preferencesManager: PreferenceManager
) {
    companion object {
        private const val TAG = "TransactionRepository"
    }

    private fun getAuthHeader(): String? {
        val token = preferencesManager.getIdToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else null
    }
    suspend fun getMyTransactions(householdId: String): Result<List<TransactionResponseDTO>> {
        return try {
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                return com.example.myapplication.domain.common.Result.Error("Пользователь не авторизован")
            }

            Log.d(TAG, "Получение транзакций для хозяйства: $householdId")
            val response = api.getMyTransactions(authHeader, householdId)
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
            Result.Error(e.message ?: "Ошибка получения транзакций")
        }
    }
}