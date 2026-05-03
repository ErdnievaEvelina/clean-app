package com.example.myapplication.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.HouseholdWithUserInfo
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.CreateHouseholdUseCase
import com.example.myapplication.domain.usecase.GetUserHouseholdsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HouseholdUiState(
    val isLoading: Boolean = false,
    val households: List<HouseholdWithUserInfo> = emptyList(),
    val error: String? = null,
    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val newHouseholdName: String = ""
)
sealed class HouseholdsAction {
    data object LoadHouseholds : HouseholdsAction()
    data object ShowCreateDialog : HouseholdsAction()
    data object DismissCreateDialog : HouseholdsAction()
    data class CreateHousehold(val name: String) : HouseholdsAction()
    data class NewNameChanged(val name: String) : HouseholdsAction()
    data object ErrorDismissed : HouseholdsAction()
}
class HouseholdViewModel(
    private val getUserHouseholdsUseCase: GetUserHouseholdsUseCase,
    private val createHouseholdUseCase: CreateHouseholdUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(HouseholdUiState())
    val uiState = _uiState.asStateFlow()
    init{
        loadHouseholds()
    }
    fun handleAction(action: HouseholdsAction) {
        when (action) {
            HouseholdsAction.LoadHouseholds -> loadHouseholds()
            HouseholdsAction.ShowCreateDialog -> {
                _uiState.update { it.copy(showCreateDialog = true, newHouseholdName = "") }
            }
            HouseholdsAction.DismissCreateDialog -> {
                _uiState.update { it.copy(showCreateDialog = false) }
            }
            is HouseholdsAction.CreateHousehold -> createHousehold(action.name)
            is HouseholdsAction.NewNameChanged -> {
                _uiState.update { it.copy(newHouseholdName = action.name) }
            }
            HouseholdsAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
    private fun loadHouseholds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result = getUserHouseholdsUseCase()){
                is Result.Error -> { _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        households = result.data
                    )
                }}
            }
        }
    }
    private fun createHousehold(name: String){
        viewModelScope.launch {
            _uiState.update {it.copy(isCreating = true, error = null, showCreateDialog = false) }
            when(val result = createHouseholdUseCase(name)){
                is Result.Error -> {_uiState.update { it.copy(isCreating = false, error = result.message) }}
                Result.Loading -> {}
                is Result.Success -> { _uiState.update { it.copy(isCreating = false) }
                    loadHouseholds()
                }
            }
        }
    }


}