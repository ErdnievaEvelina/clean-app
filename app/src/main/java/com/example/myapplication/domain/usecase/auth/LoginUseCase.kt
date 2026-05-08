package com.example.myapplication.domain.usecase.auth

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank()) return Result.Error("Введите эл. почту")
        if (password.isBlank()) return Result.Error("Введите пароль")
        return authRepository.login(email.trim(), password)
    }
}