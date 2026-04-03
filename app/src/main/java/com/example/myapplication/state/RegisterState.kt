package com.example.myapplication.state

import com.example.myapplication.data.model.UserResponse

sealed class RegistrationState {
    object Initial : RegistrationState()
    object Loading : RegistrationState()
    object FirebaseCreating : RegistrationState()  // Шаг 1: создание в Firebase
    object BackendRegistering : RegistrationState() // Шаг 2: регистрация в бэкенде
    data class Success(val user: UserResponse) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}