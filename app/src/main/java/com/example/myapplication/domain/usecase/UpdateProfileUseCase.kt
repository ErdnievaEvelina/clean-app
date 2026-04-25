package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository

class UpdateProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        name: String
    ): Result<User> = repository.updateUser(email,name)
}