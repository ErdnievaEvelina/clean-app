package com.example.myapplication.domain.usecase.privilege
import com.example.myapplication.data.model.privilege.PrivilegeResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.PrivilegeRepository

class BuyPrivilegeUseCase(
    private val repository: PrivilegeRepository
) {
    suspend operator fun invoke(privilegeId: String): Result<PrivilegeResponseDTO>{
        return repository.buyPrivilege(privilegeId)
    }
}