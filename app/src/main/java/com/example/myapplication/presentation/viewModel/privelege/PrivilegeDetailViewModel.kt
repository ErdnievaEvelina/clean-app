package com.example.myapplication.presentation.viewModel.privelege

// PrivilegeDetailUiState.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.privilege.PrivilegeUiModel
import com.example.myapplication.data.model.privilege.map.toUiModel
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.GetHouseholdMembersUseCase
import com.example.myapplication.domain.usecase.privilege.BuyPrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.DeletePrivilegeUseCase
import com.example.myapplication.domain.usecase.privilege.GetPrivilegeByIdUseCase
import com.example.myapplication.domain.usecase.privilege.UpdatePrivilegeUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class PrivilegeDetailUiState(
    val isLoading: Boolean = false,
    val privilege: PrivilegeUiModel? = null,
    val error: String? = null,
    val isBuying: Boolean = false,
    val isDeleting: Boolean = false,
    val isEditing: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showEditDialog: Boolean = false,
    val editTitle: String = "",
    val editDescription: String = "",
    val editCost: String = ""
)

sealed class PrivilegeDetailAction {
    data object LoadPrivilege : PrivilegeDetailAction()
    data object BuyPrivilege : PrivilegeDetailAction()
    data object ShowDeleteConfirmation : PrivilegeDetailAction()
    data object DismissDeleteConfirmation : PrivilegeDetailAction()
    data object DeletePrivilege : PrivilegeDetailAction()
    data object ShowEditDialog : PrivilegeDetailAction()
    data object DismissEditDialog : PrivilegeDetailAction()
    data class UpdatePrivilege(
        val title: String,
        val description: String,
        val cost: Int
    ) : PrivilegeDetailAction()
    data class EditTitleChanged(val value: String) : PrivilegeDetailAction()
    data class EditDescriptionChanged(val value: String) : PrivilegeDetailAction()
    data class EditCostChanged(val value: String) : PrivilegeDetailAction()
    data object ErrorDismissed : PrivilegeDetailAction()
    data object NavigateBack : PrivilegeDetailAction()
}
class PrivilegeDetailViewModel(
    private val getPrivilegeByIdUseCase: GetPrivilegeByIdUseCase,
    private val buyPrivilegeUseCase: BuyPrivilegeUseCase,
    private val deletePrivilegeUseCase: DeletePrivilegeUseCase,
    private val updatePrivilegeUseCase: UpdatePrivilegeUseCase,
    private val getHouseholdMembersUseCase: GetHouseholdMembersUseCase,
    private val privilegeId: String,
    private val currentUserId: String,
    private val householdId: String,
    private val onNavigateBack: () -> Unit
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrivilegeDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPrivilege()
    }
    fun handleAction(action: PrivilegeDetailAction) {
        when (action) {
            PrivilegeDetailAction.LoadPrivilege -> loadPrivilege()
            PrivilegeDetailAction.BuyPrivilege -> buyPrivilege()
            PrivilegeDetailAction.ShowDeleteConfirmation -> {
                _uiState.update { it.copy(showDeleteConfirmation = true) }
            }
            PrivilegeDetailAction.DismissDeleteConfirmation -> {
                _uiState.update { it.copy(showDeleteConfirmation = false) }
            }
            PrivilegeDetailAction.DeletePrivilege -> deletePrivilege()
            PrivilegeDetailAction.ShowEditDialog -> {
                _uiState.value.privilege?.let { privilege ->
                    _uiState.update {
                        it.copy(
                            showEditDialog = true,
                            editTitle = privilege.title,
                            editDescription = privilege.description ?: "",
                            editCost = privilege.cost.toString()
                        )
                    }
                }
            }
            PrivilegeDetailAction.DismissEditDialog -> {
                _uiState.update { it.copy(showEditDialog = false) }
            }
            is PrivilegeDetailAction.UpdatePrivilege -> updatePrivilege(action)
            is PrivilegeDetailAction.EditTitleChanged -> {
                _uiState.update { it.copy(editTitle = action.value) }
            }
            is PrivilegeDetailAction.EditDescriptionChanged -> {
                _uiState.update { it.copy(editDescription = action.value) }
            }
            is PrivilegeDetailAction.EditCostChanged -> {
                val filtered = action.value.filter { it.isDigit() }
                if (filtered.length <= 3) {
                    _uiState.update { it.copy(editCost = filtered) }
                }
            }
            PrivilegeDetailAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
            PrivilegeDetailAction.NavigateBack -> onNavigateBack()
        }
    }
    private fun loadPrivilege() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }


                // Загружаем членов хозяйства и привилегию параллельно
                val membersDeferred = async { getHouseholdMembersUseCase(householdId) }
                val privilegeDeferred = async { getPrivilegeByIdUseCase(privilegeId) }

                val membersResult = membersDeferred.await()
                val privilegeResult = privilegeDeferred.await()

                // Создаем карту пользователей (ID -> имя)
                val userMap = when (membersResult) {
                    is Result.Success -> membersResult.data.associate { it.id to it.name }
                    else -> emptyMap()
                }

                // Преобразуем привилегию в UI модель с именами
                when (privilegeResult) {
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = privilegeResult.message)
                        }
                    }

                    Result.Loading -> { /* Already loading */
                    }

                    is Result.Success -> {
                        val uiModel = privilegeResult.data.toUiModel(userMap)
                        _uiState.update {
                            it.copy(isLoading = false, privilege = uiModel)
                        }
                    }
                }

        }
    }
    private fun buyPrivilege() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBuying = true, error = null) }

            when (val result = buyPrivilegeUseCase(privilegeId)) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isBuying = false, error = result.message)
                    }
                }
                Result.Loading -> { /* Already loading */ }
                is Result.Success -> {
                    _uiState.update { it.copy(isBuying = false) }
                    loadPrivilege()
                }
            }
        }
    }
    private fun deletePrivilege() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isDeleting = true, error = null, showDeleteConfirmation = false)
            }

            when (val result = deletePrivilegeUseCase(privilegeId)) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isDeleting = false, error = result.message)
                    }
                }
                Result.Loading -> { /* Already loading */ }
                is Result.Success -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    onNavigateBack()
                }
            }
        }
    }
    private fun updatePrivilege(action: PrivilegeDetailAction.UpdatePrivilege) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isEditing = true, error = null, showEditDialog = false)
            }

            when (val result = updatePrivilegeUseCase(
                privilegeId,
                action.title,
                action.description.takeIf { it.isNotBlank() },
                action.cost
            )) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isEditing = false, error = result.message)
                    }
                }
                Result.Loading -> { /* Already loading */ }
                is Result.Success -> {
                    _uiState.update { it.copy(isEditing = false) }
                    loadPrivilege() // Перезагружаем обновленные данные
                }
            }
        }
    }

    fun getCurrentUserId(): String = currentUserId
    fun getHouseholdId(): String = householdId

}