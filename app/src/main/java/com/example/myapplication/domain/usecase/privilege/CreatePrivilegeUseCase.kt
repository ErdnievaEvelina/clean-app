package com.example.myapplication.domain.usecase.privilege
import com.example.myapplication.data.model.privilege.PrivilegeResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.PrivilegeRepository

class CreatePrivilegeUseCase(
    private val repository: PrivilegeRepository
) {
    suspend operator fun invoke(
        householdId: String,
        title: String,
        description: String?,
        cost: Int
    ): Result<PrivilegeResponseDTO> {
        if (title.isBlank()) {
            return Result.Error("Введите название привилегии")
        }
        if (cost < 5 || cost > 500) {
            return Result.Error("Стоимость должна быть от 5 до 500")
        }
        return repository.createPrivilege(householdId, title, description, cost)
    }
}