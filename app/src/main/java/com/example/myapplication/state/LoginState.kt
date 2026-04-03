package com.example.myapplication.state

import com.example.myapplication.data.model.UserResponse

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}