package com.example.myapplication.presentation.viewModel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUIState(
    val email:String="",
    val password:String="",
    val name:String="",
    val isLoading:Boolean=false,
    val error:String?= null,
    val success:Boolean = false
)

sealed class RegisterUserAction{
    data class EmailChanged(val value:String): RegisterUserAction()
    data class PasswordChanged(val value:String): RegisterUserAction()
    data class NameChanged(val value:String): RegisterUserAction()
    data object RegisterClicked: RegisterUserAction()
    data object ErrorDismissed: RegisterUserAction()

}
class RegisterViewModelNew(
    private val registerUseCase: RegisterUseCase
): ViewModel() {
    private val _uiState= MutableStateFlow(RegisterUIState())
    val uiState = _uiState.asStateFlow()
    fun handleEvent(action: RegisterUserAction){
        when(action){
            is RegisterUserAction.EmailChanged -> {
                _uiState.update { it.copy(email = action.value) }
            }
            RegisterUserAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
            is RegisterUserAction.NameChanged -> {
                _uiState.update { it.copy(name= action.value) }
            }
            is RegisterUserAction.PasswordChanged -> {
                _uiState.update { it.copy(password = action.value) }
            }
            RegisterUserAction.RegisterClicked -> register()
        }
    }

    private fun register(){
        viewModelScope.launch {
            _uiState.update{it.copy(isLoading = true,error=null)}
            when(val result = registerUseCase(uiState.value.email, uiState.value.password,uiState.value.name)){
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                Result.Loading -> {}
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = true) }
            }
        }

    }

}