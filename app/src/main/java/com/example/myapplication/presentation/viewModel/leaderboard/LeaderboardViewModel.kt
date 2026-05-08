package com.example.myapplication.presentation.viewModel.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.leaderbord.LeaderboardResponseDTO
import com.example.myapplication.domain.usecase.leaderboard.GetLeaderboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.myapplication.domain.common.Result


class LeaderboardViewModel(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val householdId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun handleAction(action: LeaderboardAction) {
        when (action) {
            LeaderboardAction.Refresh -> loadLeaderboard()
            LeaderboardAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getLeaderboardUseCase(householdId)) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                Result.Loading -> {  }
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            leaderboard = result.data,
                            isEmpty = result.data.items.isEmpty()
                        )
                    }
                }
            }
        }
    }
}

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val leaderboard: LeaderboardResponseDTO? = null,
    val isEmpty: Boolean = false,
    val error: String? = null
)

sealed class LeaderboardAction {
    data object Refresh : LeaderboardAction()
    data object ErrorDismissed : LeaderboardAction()
}