package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
    ): Result<Unit> {
        if (email.isBlank()) return Result.Error("Введите эл. почту")
        if (password.length < 8) return Result.Error("Пароль должен содержать не менее 6 символов")
        if (name.isBlank()) return Result.Error("Введите ФИО")
        return authRepository.register(email.trim(), password, name.trim())
    }
}