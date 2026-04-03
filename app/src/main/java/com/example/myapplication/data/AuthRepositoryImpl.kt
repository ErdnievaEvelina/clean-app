package com.example.myapplication.data

import android.util.Log
import com.example.myapplication.PreferenceManager
import com.example.myapplication.data.api.AuthApi
import com.example.myapplication.state.RegistrationState
import com.example.myapplication.data.model.UserRegisterRequest
import com.example.myapplication.state.LoginState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.io.IOException

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val preferencesManager: PreferenceManager
) {
    private val firebaseAuth: FirebaseAuth = Firebase.auth

    companion object {
        private const val TAG = "AuthRepository"
    }
    fun registerUser(
        email: String,
        password: String,
        name: String
    ): Flow<RegistrationState> = flow {
        try {
            // Шаг 1: Начало регистрации
            emit(RegistrationState.Loading)
            Log.d(TAG, "Starting Firebase registration for: $email")

            // ШАГ 1: Регистрация в Firebase через SDK
            emit(RegistrationState.FirebaseCreating)
            Log.d(TAG, "Step 1: Creating user in Firebase...")

            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
                .await() // await() превращает Task в suspend функцию

            val firebaseUser = authResult.user
                ?: throw Exception("Firebase user creation failed")

            val firebaseUid = firebaseUser.uid
            Log.d(TAG, "Firebase user created successfully")
            Log.d(TAG, "Firebase UID: $firebaseUid")
            Log.d(TAG, "Email: ${firebaseUser.email}")

            // ШАГ 2: Получение idToken от Firebase
            Log.d(TAG, "Step 2: Getting ID token from Firebase...")
            val idTokenResult = firebaseUser.getIdToken(true).await()
            val idToken = idTokenResult.token
                ?: throw Exception("Failed to get ID token")

            Log.d(TAG, "ID Token получен: ${idToken.take(20)}...")
            // ШАГ 3: Регистрация в вашем бэкенде
            emit(RegistrationState.BackendRegistering)
            Log.d(TAG, "Step 3: Registering in backend...")

            val userResponse = api.registerUser(
                authorization = "Bearer $idToken",  // Точно как в вашем Postman
                request = UserRegisterRequest(
                    name = name,
                    email = email,
                    avatarUrl = null
                )
            )

            Log.d(TAG, "Backend registration successful")
            Log.d(TAG, "User ID from backend: ${userResponse.id}")
            Log.d(TAG, "Firebase UID from backend: ${userResponse.firebaseUid}")

            // ШАГ 4: Сохраняем данные в SharedPreferences
            preferencesManager.saveUserData(
                idToken = idToken,
                firebaseUid = firebaseUid,
                userId = userResponse.id,
                email = email,
                name = name
            )

            Log.d(TAG, "User data saved to SharedPreferences")

            // Успешная регистрация
            emit(RegistrationState.Success(userResponse))
        } catch (e: Exception) {
            Log.e(TAG, "Registration error", e)

            val errorMessage = when (e) {
                is IOException -> "Ошибка сети. Проверьте подключение к интернету"
                is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                    "Этот email уже зарегистрирован в Firebase"
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                    "Неверный формат email или пароля"
                is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
                    "Слишком простой пароль. Минимум 6 символов"
                else -> e.message ?: "Неизвестная ошибка"
            }

            emit(RegistrationState.Error(errorMessage))
        }
    }.flowOn(Dispatchers.IO)

    fun loginUser(
        email: String,
        password: String
    ): Flow<LoginState> = flow {
        try {
            Log.d(TAG, "=== НАЧАЛО ВХОДА ===")
            Log.d(TAG, "Email: $email")

            emit(LoginState.Loading)

            // ШАГ 1: Вход в Firebase
            Log.d(TAG, "Шаг 1: Аутентификация в Firebase...")
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("Failed to get user from Firebase")

            val firebaseUid = firebaseUser.uid
            Log.d(TAG, "Firebase аутентификация успешна")
            Log.d(TAG, "Firebase UID: $firebaseUid")

            // ШАГ 2: Получение свежего ID токена
            Log.d(TAG, "Шаг 2: Получение ID токена...")
            val idTokenResult = firebaseUser.getIdToken(true).await()
            val idToken = idTokenResult.token
                ?: throw Exception("Failed to get ID token")
            Log.d(TAG, "ID Token получен: ${idToken.take(20)}...")

            // ШАГ 3: Получение профиля пользователя с бэкенда
            Log.d(TAG, "Шаг 3: Получение профиля с бэкенда...")

            // Сначала пробуем получить профиль через /api/users/me
            val userResponse = try {
                api.getCurrentUser("Bearer $idToken")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка получения профиля: ${e.message}")
                // Если профиль не найден, возможно пользователь не зарегистрирован в бэкенде
                throw Exception("Пользователь не найден в базе данных. Сначала зарегистрируйтесь.")
            }

            Log.d(TAG, "Профиль получен успешно")
            Log.d(TAG, "User ID: ${userResponse.id}")
            Log.d(TAG, "Name: ${userResponse.name}")

            // ШАГ 4: Сохранение данных в SharedPreferences
            preferencesManager.saveUserData(
                idToken = idToken,
                firebaseUid = firebaseUid,
                userId = userResponse.id,
                email = userResponse.email,
                name = userResponse.name
            )

            Log.d(TAG, "Данные сохранены в SharedPreferences")
            Log.d(TAG, "=== ВХОД УСПЕШНО ЗАВЕРШЕН ===")

            emit(LoginState.Success(userResponse))

        } catch (e: Exception) {
            Log.e(TAG, "=== ОШИБКА ВХОДА ===")
            Log.e(TAG, "Тип ошибки: ${e.javaClass.simpleName}")
            Log.e(TAG, "Сообщение: ${e.message}")
            e.printStackTrace()

            val errorMessage = when (e) {
                is FirebaseAuthInvalidUserException -> {
                    Log.e(TAG, "FirebaseAuthInvalidUserException - пользователь не найден")
                    "Пользователь с таким email не найден"
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    Log.e(TAG, "FirebaseAuthInvalidCredentialsException - неверный пароль")
                    "Неверный пароль"
                }
                is IOException -> {
                    Log.e(TAG, "IOException - проблема с сетью")
                    "Ошибка сети. Проверьте подключение к интернету"
                }
                else -> {
                    Log.e(TAG, "Неизвестная ошибка")
                    e.message ?: "Ошибка входа"
                }
            }

            emit(LoginState.Error(errorMessage))
        }
    }.flowOn(Dispatchers.IO)


    fun signOut() {
        firebaseAuth.signOut()
        preferencesManager.clear()
        Log.d(TAG, "User signed out")
    }

    /**
     * Получение текущего пользователя из Firebase
     */
    fun getCurrentFirebaseUser() = firebaseAuth.currentUser

    /**
     * Проверка, залогинен ли пользователь
     */
    fun isUserLoggedIn(): Boolean =
        firebaseAuth.currentUser != null && preferencesManager.isLoggedIn()
}