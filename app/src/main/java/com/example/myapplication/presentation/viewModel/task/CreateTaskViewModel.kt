package com.example.myapplication.presentation.viewModel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.task.CreateTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateTaskUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val description: String = "",
    val reward: String = "",
    val error: String? = null,
    val success: Boolean = false,
    val createdTaskId: String? = null
)
sealed class CreateTaskAction {
    data class TitleChanged(val value: String) : CreateTaskAction()
    data class DescriptionChanged(val value: String) : CreateTaskAction()
    data class RewardChanged(val value: String) : CreateTaskAction()
    data object CreateClicked : CreateTaskAction()
    data object ErrorDismissed : CreateTaskAction()
    data object SuccessDismissed : CreateTaskAction()
}
class CreateTaskViewModel(
    private val createTaskUseCase: CreateTaskUseCase,
    private val householdId: String
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState = _uiState.asStateFlow()
    fun handleAction(action: CreateTaskAction) {
        when (action) {
            is CreateTaskAction.TitleChanged -> {
                _uiState.update { it.copy(title = action.value) }
            }
            is CreateTaskAction.DescriptionChanged -> {
                _uiState.update { it.copy(description = action.value) }
            }
            is CreateTaskAction.RewardChanged -> {
                _uiState.update { it.copy(reward = action.value) }
            }
            CreateTaskAction.CreateClicked -> createTask()
            CreateTaskAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
            CreateTaskAction.SuccessDismissed -> {
                _uiState.update { it.copy(success = false, createdTaskId = null) }
            }
        }
    }
    private fun createTask() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }
            val reward = _uiState.value.reward.toIntOrNull() ?: 0
            when (val result = createTaskUseCase(householdId,uiState.value.title,uiState.value.description.takeIf { it.isNotBlank() },reward)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        success = true,
                        createdTaskId = result.data.id
                    )
                }}
            }

        }
    }
}