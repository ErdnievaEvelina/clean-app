package com.example.myapplication.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

sealed class LoginUserAction {
    data class EmailChanged(val value: String) : LoginUserAction()
    data class PasswordChanged(val value: String) : LoginUserAction()
    data object LoginClicked : LoginUserAction()
    data object ErrorDismissed : LoginUserAction()
}
class LoginViewModel(
    private val loginUseCase: LoginUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun handleEvent(action: LoginUserAction) {
        when (action) {
            is LoginUserAction.EmailChanged -> _uiState.update { it.copy(email = action.value) }
            is LoginUserAction.PasswordChanged -> _uiState.update { it.copy(password = action.value) }
            is LoginUserAction.LoginClicked -> login()
            is LoginUserAction.ErrorDismissed -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(uiState.value.email, uiState.value.password)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }
}
