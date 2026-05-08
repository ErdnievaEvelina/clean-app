package com.example.myapplication.presentation.viewModel.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.JoinHouseholdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinHouseholdUiState(
    val isLoading: Boolean = false,
    val inviteCode: String = "",
    val error: String? = null,
    val success: Boolean = false,
    val joinedHouseholdId: String? = null
)
sealed class JoinHouseholdAction {
    data class InviteCodeChanged(val value: String) : JoinHouseholdAction()
    data object JoinClicked : JoinHouseholdAction()
    data object ErrorDismissed : JoinHouseholdAction()
    data object SuccessDismissed : JoinHouseholdAction()
}
class JoinHouseholdViewModel(
    private val joinHouseholdUseCase: JoinHouseholdUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(JoinHouseholdUiState())
    val uiState = _uiState.asStateFlow()
    fun handleAction(action: JoinHouseholdAction) {
        when (action) {
            is JoinHouseholdAction.InviteCodeChanged -> {
                _uiState.update { it.copy(inviteCode = action.value) }
            }
            JoinHouseholdAction.JoinClicked -> joinHousehold()
            JoinHouseholdAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
            JoinHouseholdAction.SuccessDismissed -> {
                _uiState.update { it.copy(success = false) }
            }
        }
    }
    private fun joinHousehold() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result = joinHouseholdUseCase(uiState.value.inviteCode)){
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
                            success = true,
                            joinedHouseholdId = result.data.householdId
                        )
                    }
                }
            }
        }
    }


}