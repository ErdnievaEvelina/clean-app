package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.datastore.PreferenceManager
import com.example.myapplication.data.api.AuthApi
import com.example.myapplication.data.model.UserRegisterRequest
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val preferencesManager: PreferenceManager
): AuthRepository {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    companion object {
        private const val TAG = "AuthRepository"
    }
    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        return try{
            Log.d(TAG, "Шаг 1: Аутентификация в Firebase...")
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("Failed to get user from Firebase")

            val firebaseUid = firebaseUser.uid
            Log.d(TAG, "Firebase аутентификация успешна")
            Log.d(TAG, "Firebase UID: $firebaseUid")

            Log.d(TAG, "Шаг 2: Получение ID токена...")
            val idTokenResult = firebaseUser.getIdToken(true).await()
            val idToken = idTokenResult.token
                ?: throw Exception("Failed to get ID token")
            Log.d(TAG, "ID Token получен: ${idToken.take(20)}...")

            Log.d(TAG, "Шаг 3: Получение профиля с бэкенда...")
            val userResponse = try {
                api.getCurrentUser("Bearer $idToken")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка получения профиля: ${e.message}")
                throw Exception("Пользователь не найден в базе данных. Сначала зарегистрируйтесь.")
            }

            Log.d(TAG, "Профиль получен успешно")
            Log.d(TAG, "User ID: ${userResponse.id}")
            Log.d(TAG, "Name: ${userResponse.name}")

            preferencesManager.saveUserData(
                idToken = idToken,
                firebaseUid = firebaseUid,
                userId = userResponse.id,
                email = userResponse.email,
                name = userResponse.name
            )

            Log.d(TAG, "Данные сохранены в SharedPreferences")
            Result.Success(Unit)

        }catch (e: Exception){
            val errorMessage = when (e) {
                is FirebaseAuthInvalidUserException -> {
                    "Пользователь с таким email не найден"
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    "Неверный пароль"
                }
                is IOException -> {
                    "Ошибка сети. Проверьте подключение к интернету"
                }
                else -> {
                    e.message ?: "Ошибка входа"
                }
            }
            Result.Error(errorMessage)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<Unit> {
        return try{
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("Firebase user creation failed")

            val firebaseUid = firebaseUser.uid
            Log.d(TAG, "Firebase UID: $firebaseUid Email: ${firebaseUser.email}")

            Log.d(TAG, "Получение токена из Firebase...")
            val idTokenResult = firebaseUser.getIdToken(true).await()
            val idToken = idTokenResult.token
                ?: throw Exception("Failed to get ID token")

            Log.d(TAG, "ID Token получен: ${idToken.take(20)}...")

            val userResponse = api.registerUser(
                authorization = "Bearer $idToken",
                request = UserRegisterRequest(
                    name = name,
                    email = email,
                    avatarUrl = null
                )
            )

            Log.d(TAG, "Успешная регистрации на бэке: User id: ${userResponse.id}, firebase UID: ${userResponse.firebaseUid}")

            preferencesManager.saveUserData(
                idToken = idToken,
                firebaseUid = firebaseUid,
                userId = userResponse.id,
                email = email,
                name = name
            )

            Result.Success(Unit)
        }catch(e:Exception){
            Result.Error(e.message?:"Ошибка регистрации")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        preferencesManager.clear()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return flow {
            delay(500)
            firebaseAuth.currentUser != null && preferencesManager.isLoggedIn()}
    }
    fun getCurrentUserId(): String? {
        return preferencesManager.getUserId()
    }
}