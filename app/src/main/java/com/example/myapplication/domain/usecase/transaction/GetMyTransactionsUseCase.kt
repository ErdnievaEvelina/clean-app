package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.data.model.transaction.TransactionResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.TransactionRepository

class GetMyTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(householdId: String): Result<List<TransactionResponseDTO>> {
        return repository.getMyTransactions(householdId)
    }
}