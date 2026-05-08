package com.example.myapplication.presentation.viewModel.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ActivityActorScope
import com.example.myapplication.data.model.ActivityResponseDTO
import com.example.myapplication.data.model.ActivityType
import com.example.myapplication.domain.usecase.activity.GetHouseholdActivityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.myapplication.domain.common.Result

class ActivityViewModel(
    private val getHouseholdActivityUseCase: GetHouseholdActivityUseCase,
    private val householdId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadActivity()
    }

    fun handleAction(action: ActivityAction) {
        when (action) {
            is ActivityAction.FilterByType -> {
                _uiState.update { it.copy(selectedType = action.activityType) }
                loadActivity()
            }
            is ActivityAction.FilterByScope -> {
                _uiState.update { it.copy(selectedScope = action.scope) }
                loadActivity()
            }
            ActivityAction.Refresh -> loadActivity()
            ActivityAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
            ActivityAction.ClearTypeFilter -> {
                _uiState.update { it.copy(selectedType = null) }
                loadActivity()
            }
        }
    }

    private fun loadActivity() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getHouseholdActivityUseCase(
                householdId,
                _uiState.value.selectedType,
                _uiState.value.selectedScope
            )) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                Result.Loading -> { /* Already loading */ }
                is Result.Success -> {
                    val activities = result.data.sortedByDescending { it.createdAt }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activities = activities,
                            isEmpty = activities.isEmpty()
                        )
                    }
                }
            }
        }
    }
}

data class ActivityUiState(
    val isLoading: Boolean = false,
    val activities: List<ActivityResponseDTO> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
    val selectedType: ActivityType? = null,
    val selectedScope: ActivityActorScope = ActivityActorScope.ALL
)

sealed class ActivityAction {
    data class FilterByType(val activityType: ActivityType?) : ActivityAction()
    data class FilterByScope(val scope: ActivityActorScope) : ActivityAction()
    data object Refresh : ActivityAction()
    data object ErrorDismissed : ActivityAction()
    data object ClearTypeFilter : ActivityAction()
}