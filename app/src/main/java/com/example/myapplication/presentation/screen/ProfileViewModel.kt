package com.example.myapplication.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.usecase.DeleteUserUseCase
import com.example.myapplication.domain.usecase.GetProfileUseCase
import com.example.myapplication.domain.usecase.SyncEmailUseCase
import com.example.myapplication.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading:Boolean=false,
    val error:String?= null,
    val success:Boolean = false,
    val isEditing: Boolean = false,
    val isSyncing: Boolean = false,
    val user: User? = null,
    val name: String = "",
    val email: String = ""
)

sealed class ProfileAction{
    data class NameChanged(val value: String) : ProfileAction()
    data class EmailChanged(val value: String) : ProfileAction()
    data object EditClicked : ProfileAction()
    data object SaveClicked : ProfileAction()
    data object DeleteClicked : ProfileAction()
    data object ErrorDismissed : ProfileAction()
    data object LoadProfile : ProfileAction()
    data object SyncEmail : ProfileAction()
}
class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val syncEmailUseCase: SyncEmailUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState= _uiState.asStateFlow()
    init{
        loadProfile()
    }
    fun handleAction(action: ProfileAction) {
        when(action){
            ProfileAction.DeleteClicked -> deleteAccount()
            ProfileAction.EditClicked -> {
                val currentUser = _uiState.value.user
                _uiState.update {
                    it.copy(
                        isEditing = true,
                        name = currentUser?.name ?: "",
                        email = currentUser?.email ?: ""
                    )
                }
            }
            is ProfileAction.EmailChanged -> {
                _uiState.update { it.copy(email = action.value) }
            }
            ProfileAction.ErrorDismissed -> {_uiState.update { it.copy(error = null) }}
            ProfileAction.LoadProfile -> loadProfile()
            is ProfileAction.NameChanged -> {
                _uiState.update { it.copy(name= action.value) }
            }
            ProfileAction.SaveClicked -> saveProfile()
            ProfileAction.SyncEmail -> syncEmail()
        }
    }

    private fun loadProfile(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
             when(val result = getProfileUseCase()){
                 is Result.Error -> {_uiState.update { it.copy(isLoading = false, error = result.message) }}
                 Result.Loading -> {}
                 is Result.Success -> {_uiState.update {it.copy(
                     isLoading = false,
                     user = result.data,
                     name = result.data.name,
                     email = result.data.email,
                     success = true
                 ) }}
             }
        }
    }
    private fun saveProfile(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result= updateProfileUseCase(uiState.value.name,null)){
                is Result.Error -> {_uiState.update { it.copy(isLoading = false, error = result.message) }}
                Result.Loading -> {}
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = false,
                            user = result.data,
                            success = true
                        )
                    }
                }
            }
        }
    }
    private fun syncEmail(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result= syncEmailUseCase()){
                is Result.Error -> {_uiState.update { it.copy(
                        isSyncing = false,
                        error = result.message) }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update {
                    it.copy(
                        isSyncing = false,
                        user = result.data,
                        email = result.data.email,
                        success = true
                    )}
                }
            }
        }
    }
    private fun deleteAccount(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = deleteUserUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

}