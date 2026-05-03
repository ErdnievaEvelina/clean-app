package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.model.Household
import com.example.myapplication.domain.repository.HouseholdRepository

class CreateHouseholdUseCase(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(name: String): Result<Household>{
       if (name.isBlank()){
           Result.Error("Название хозяйства не может быть пустым")
       }
        return repository.createHousehold(name)
    }
}