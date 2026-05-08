package com.example.myapplication.presentation.viewModel.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.transaction.TransactionResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.transaction.GetMyTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TransactionViewModel(
    private val getMyTransactionsUseCase: GetMyTransactionsUseCase,
    private val householdId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun handleAction(action: TransactionAction) {
        when (action) {
            TransactionAction.LoadTransactions -> loadTransactions()
            TransactionAction.Refresh -> loadTransactions()
            TransactionAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getMyTransactionsUseCase(householdId)) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                Result.Loading -> { /* Already loading */ }
                is Result.Success -> {
                    val transactions = result.data.sortedByDescending { it.createdAt }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            transactions = transactions,
                            isEmpty = transactions.isEmpty()
                        )
                    }
                }
            }
        }
    }
}

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionResponseDTO> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null
)

sealed class TransactionAction {
    data object LoadTransactions : TransactionAction()
    data object Refresh : TransactionAction()
    data object ErrorDismissed : TransactionAction()
}