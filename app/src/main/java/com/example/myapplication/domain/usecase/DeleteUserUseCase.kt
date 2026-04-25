package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.UserRepository

class DeleteUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.deleteUser()
}