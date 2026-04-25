package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository

class GetProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<User> = repository.getProfile()
}