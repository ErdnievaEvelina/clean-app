package com.example.myapplication.domain.usecase.privilege
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.PrivilegeRepository

class DeletePrivilegeUseCase(
    private val repository: PrivilegeRepository
) {
    suspend operator fun invoke(privilegeId: String): Result<Unit> {
        return repository.deletePrivilege(privilegeId)
    }
}