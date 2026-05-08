package com.example.myapplication.domain.usecase.privilege
import com.example.myapplication.data.model.privilege.PrivilegeUiModel
import com.example.myapplication.data.model.privilege.map.PrivilegeFilterType
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.PrivilegeRepository

class GetPrivilegesUseCase(
    private val repository: PrivilegeRepository
) {
    suspend operator fun invoke(
        householdId: String,
        filter: PrivilegeFilterType = PrivilegeFilterType.ALL
    ): Result<List<PrivilegeUiModel>> {
        return repository.getPrivileges(householdId, filter)
    }
}