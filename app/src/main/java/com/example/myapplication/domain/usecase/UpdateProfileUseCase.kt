package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository
import com.google.android.play.integrity.internal.a

class UpdateProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        avatarUrl:String?
    ): Result<User> = repository.updateUser(name,avatarUrl)
}