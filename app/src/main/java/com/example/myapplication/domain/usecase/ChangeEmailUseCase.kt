package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.tasks.await

class ChangeEmailUseCase(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(newEmail: String, password: String): Result<User> {
        return try {
            // 1. Проверяем, что пользователь авторизован
            val currentUser = firebaseAuth.currentUser
                ?: return Result.Error("Пользователь не авторизован")

            // 2. Re-authentication для безопасности
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            currentUser.reauthenticate(credential).await()

            // 3. Меняем email в Firebase (источник истины)
            currentUser.verifyBeforeUpdateEmail(newEmail).await()

            userRepository.syncEmailFromFirebase()

        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthRecentLoginRequiredException ->
                    "Требуется повторный вход. Выйдите и войдите снова."
                is FirebaseAuthInvalidCredentialsException ->
                    "Неверный пароль"
                is FirebaseAuthUserCollisionException ->
                    "Этот email уже используется"
                else -> e.message ?: "Ошибка при смене email"
            }
            Result.Error(errorMessage)
        }
    }
}