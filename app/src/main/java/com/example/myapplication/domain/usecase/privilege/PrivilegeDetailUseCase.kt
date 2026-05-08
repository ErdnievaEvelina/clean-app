package com.example.myapplication.domain.usecase.privilege

import com.example.myapplication.data.model.privilege.PrivilegeResponseDTO
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.PrivilegeRepository

class GetPrivilegeByIdUseCase(
    private val repository: PrivilegeRepository
) {
    suspend operator fun invoke(privilegeId: String): Result<PrivilegeResponseDTO> {
        return repository.getPrivilegeById(privilegeId)
    }
}

class UpdatePrivilegeUseCase(
    private val repository: PrivilegeRepository
) {
    suspend operator fun invoke(
        privilegeId: String,
        title: String,
        description: String?,
        cost: Int
    ): Result<PrivilegeResponseDTO> {
        return repository.updatePrivilege(privilegeId, title, description, cost)
    }
}