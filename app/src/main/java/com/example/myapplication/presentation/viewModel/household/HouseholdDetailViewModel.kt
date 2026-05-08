package com.example.myapplication.presentation.viewModel.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.user.UserResponse
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.Household
import com.example.myapplication.domain.usecase.DeleteHouseholdUseCase
import com.example.myapplication.domain.usecase.GetHouseholdMembersUseCase
import com.example.myapplication.domain.usecase.GetHouseholdUseCase
import com.example.myapplication.domain.usecase.LeaveHouseholdUseCase
import com.example.myapplication.domain.usecase.RemoveUserFromHouseholdUseCase
import com.example.myapplication.domain.usecase.UpdateHouseholdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HouseholdDetailUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val household: Household? = null,
    val members: List<UserResponse> = emptyList(),
    val name: String = "",
    val error: String? = null,
    val success: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showLeaveDialog: Boolean = false,
    val isCurrentUserCreator: Boolean = false
)
sealed class HouseholdDetailAction {
    data object LoadHousehold : HouseholdDetailAction()
    data object EditClicked : HouseholdDetailAction()
    data object SaveClicked : HouseholdDetailAction()
    data object CancelClicked : HouseholdDetailAction()
    data object DeleteClicked : HouseholdDetailAction()
    data object ConfirmDelete : HouseholdDetailAction()
    data object LeaveClicked : HouseholdDetailAction()
    data object ConfirmLeave : HouseholdDetailAction()
    data object DismissDeleteDialog : HouseholdDetailAction()
    data object DismissLeaveDialog : HouseholdDetailAction()
    data class NameChanged(val value: String) : HouseholdDetailAction()
    data object ErrorDismissed : HouseholdDetailAction()
    data object SuccessDismissed : HouseholdDetailAction()
    data class RemoveMember(val userId: String) : HouseholdDetailAction()
}
class HouseholdDetailViewModel(
    private val getHouseholdUseCase: GetHouseholdUseCase,
    private val updateHouseholdUseCase: UpdateHouseholdUseCase,
    private val deleteHouseholdUseCase: DeleteHouseholdUseCase,
    private val leaveHouseholdUseCase: LeaveHouseholdUseCase,
    private val getMembersUseCase: GetHouseholdMembersUseCase,
    private val removeUserUseCase: RemoveUserFromHouseholdUseCase,
    private val householdId: String,
    private val currentUserId: String
) : ViewModel(){
    private val _uiState = MutableStateFlow(HouseholdDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHousehold()
        loadMembers()
    }
    fun getCurrentUserId(): String = currentUserId

    fun handleAction(action: HouseholdDetailAction) {
        when (action) {
            HouseholdDetailAction.CancelClicked -> {
                _uiState.update {
                    it.copy(
                        isEditing = false,
                        name = ""
                    )
                }
                loadHousehold()
            }
            HouseholdDetailAction.ConfirmDelete -> deleteHousehold()
            HouseholdDetailAction.ConfirmLeave -> leaveHousehold()
            HouseholdDetailAction.DeleteClicked -> {_uiState.update { it.copy(showDeleteDialog = true) }}
            HouseholdDetailAction.DismissDeleteDialog -> {_uiState.update { it.copy(showDeleteDialog = false) }}
            HouseholdDetailAction.DismissLeaveDialog -> {_uiState.update { it.copy(showLeaveDialog = false) }}
            HouseholdDetailAction.EditClicked -> {
                _uiState.update {
                    it.copy(
                        isEditing = true,
                        name = it.household?.name ?: ""
                    )
                }
            }
            HouseholdDetailAction.ErrorDismissed -> {_uiState.update { it.copy(error = null) }}
            HouseholdDetailAction.LeaveClicked -> {_uiState.update { it.copy(showLeaveDialog = true) }}
            HouseholdDetailAction.LoadHousehold -> loadHousehold()
            is HouseholdDetailAction.NameChanged -> {_uiState.update { it.copy(name = action.value) }}
            HouseholdDetailAction.SaveClicked -> updateHousehold()
            HouseholdDetailAction.SuccessDismissed -> {_uiState.update { it.copy(success = false) }}
            is HouseholdDetailAction.RemoveMember -> removeMember(action.userId)
        }
    }
    private fun loadHousehold() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result = getHouseholdUseCase(householdId)){
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
                        household = result.data,
                        name = result.data.name,
                        isCurrentUserCreator = result.data.createdByUser == currentUserId
                    )
                }}
            }
        }

    }
    private fun loadMembers() {
        viewModelScope.launch {
            when(val result =getMembersUseCase(householdId)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {
                    _uiState.update { it.copy(members = result.data) }
                }
            }
        }
    }
    private fun updateHousehold() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result = updateHouseholdUseCase(householdId, uiState.value.name)){
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                Result.Loading -> {}
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = false,
                            household = result.data,
                            success = true
                        )
                    }
                }
            }
        }
    }
    private fun deleteHousehold() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            when(val result = deleteHouseholdUseCase(householdId)){
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
                        household = null
                    )
                }}
            }
        }
    }
    private fun leaveHousehold() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showLeaveDialog = false) }
            when(val result = leaveHouseholdUseCase(householdId)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            success = true,
                            household = null
                        )
                    }
                }
            }
        }
    }
    private fun removeMember(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result = removeUserUseCase(householdId, userId)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    loadMembers() // Обновляем список участников
                    _uiState.update { it.copy(success = true) }
                }
            }

        }
    }
}