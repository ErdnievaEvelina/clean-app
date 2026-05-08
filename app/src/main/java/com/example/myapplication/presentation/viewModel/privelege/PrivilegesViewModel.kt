package com.example.myapplication.presentation.viewModel.privelege

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.privilege.PrivilegeUiModel
import com.example.myapplication.data.model.privilege.map.PrivilegeFilterType
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.privilege.BuyPrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.CreatePrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.DeletePrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.GetPrivilegesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrivilegesUiState(
    val isLoading: Boolean = false,
    val privileges: List<PrivilegeUiModel> = emptyList(),
    val filter: PrivilegeFilterType = PrivilegeFilterType.ALL,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false,
    val newPrivilegeTitle: String = "",
    val newPrivilegeDescription: String = "",
    val newPrivilegeCost: String = ""
)
sealed class PrivilegesAction {
    data object LoadPrivileges : PrivilegesAction()
    data class FilterChanged(val filter: PrivilegeFilterType) : PrivilegesAction()
    data object ShowCreateDialog : PrivilegesAction()
    data object DismissCreateDialog : PrivilegesAction()
    data class CreatePrivilege(val title: String, val description: String, val cost: Int) : PrivilegesAction()
    data class NewTitleChanged(val value: String) : PrivilegesAction()
    data class NewDescriptionChanged(val value: String) : PrivilegesAction()
    data class NewCostChanged(val value: String) : PrivilegesAction()
    data class BuyPrivilege(val privilegeId: String) : PrivilegesAction()
    data class DeletePrivilege(val privilegeId: String) : PrivilegesAction()
    data object ErrorDismissed : PrivilegesAction()
}
class PrivilegesViewModel(
    private val getPrivilegesUseCase: GetPrivilegesUseCase,
    private val createPrivilegeUseCase: CreatePrivilegeUseCase,
    private val buyPrivilegeUseCase: BuyPrivilegeUseCase,
    private val deletePrivilegeUseCase: DeletePrivilegeUseCase,
    private val householdId: String,
    private val currentUserId: String
) : ViewModel(){
    private val _uiState = MutableStateFlow(PrivilegesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPrivileges()
    }

    fun getCurrentUserId(): String = currentUserId
    fun handleAction(action: PrivilegesAction) {
        when (action) {
            PrivilegesAction.LoadPrivileges -> loadPrivileges()
            is PrivilegesAction.FilterChanged -> {
                _uiState.update { it.copy(filter = action.filter) }
                loadPrivileges()
            }
            PrivilegesAction.ShowCreateDialog -> {
                _uiState.update {
                    it.copy(showCreateDialog = true, newPrivilegeTitle = "", newPrivilegeDescription = "", newPrivilegeCost = "")
                }
            }
            PrivilegesAction.DismissCreateDialog -> {
                _uiState.update { it.copy(showCreateDialog = false) }
            }
            is PrivilegesAction.CreatePrivilege -> createPrivilege(action)
            is PrivilegesAction.NewTitleChanged -> {
                _uiState.update { it.copy(newPrivilegeTitle = action.value) }
            }
            is PrivilegesAction.NewDescriptionChanged -> {
                _uiState.update { it.copy(newPrivilegeDescription = action.value) }
            }
            is PrivilegesAction.NewCostChanged -> {
                val filtered = action.value.filter { it.isDigit() }
                _uiState.update { it.copy(newPrivilegeCost = filtered) }
            }
            is PrivilegesAction.BuyPrivilege -> buyPrivilege(action.privilegeId)
            is PrivilegesAction.DeletePrivilege -> deletePrivilege(action.privilegeId)
            PrivilegesAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
    private fun loadPrivileges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result =getPrivilegesUseCase(householdId,uiState.value.filter)){
                is Result.Error -> {_uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update {
                    it.copy(isLoading = false, privileges = result.data)
                }}
            }

        }
    }
    private fun createPrivilege(action: PrivilegesAction.CreatePrivilege){
        viewModelScope.launch {
            _uiState.update {
                it.copy(isCreating = true, error = null, showCreateDialog = false)
            }
            when(val result = createPrivilegeUseCase(householdId,
                action.title,
                action.description.takeIf { it.isNotBlank() },
                action.cost)){
                is Result.Error -> {_uiState.update {
                    it.copy(isCreating = false, error = result.message)
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isCreating = false) }
                    loadPrivileges() }
            }
        }
    }
    private fun buyPrivilege(privilegeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when(val result = buyPrivilegeUseCase(privilegeId)){
                is Result.Error -> {_uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isLoading = false) }
                    loadPrivileges()}
            }

        }
    }
    private fun deletePrivilege(privilegeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when(val result = deletePrivilegeUseCase(privilegeId)){
                is Result.Error -> {_uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isLoading = false) }
                    loadPrivileges()}
            }

        }
    }


}