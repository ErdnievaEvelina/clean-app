package com.example.myapplication.presentation.screen

import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.usecase.DeleteUserUseCase
import com.example.myapplication.domain.usecase.GetProfileUseCase
import com.example.myapplication.domain.usecase.UpdateProfileUseCase

data class ProfileUiState(
    val isLoading:Boolean=false,
    val error:String?= null,
    val success:Boolean = false
)

sealed class ProfileAction{

}
class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
): ViewModel() {

}