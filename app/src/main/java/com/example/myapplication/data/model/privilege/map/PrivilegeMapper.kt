package com.example.myapplication.data.model.privilege.map

import com.example.myapplication.data.model.privilege.PrivilegeResponseDTO
import com.example.myapplication.data.model.privilege.PrivilegeUiModel

enum class PrivilegeFilterType {
    ALL,
    AVAILABLE,
    MY
}

fun PrivilegeFilterType.toApiString(): String {
    return when (this) {
        PrivilegeFilterType.ALL -> "ALL"
        PrivilegeFilterType.AVAILABLE -> "AVAILABLE"
        PrivilegeFilterType.MY -> "MY"
    }
}
fun PrivilegeResponseDTO.toUiModel(userNameMap: Map<String, String> = emptyMap()): PrivilegeUiModel {
    return PrivilegeUiModel(
        id = id,
        title = title,
        description = description,
        cost = cost,
        isAvailable = isAvailable,
        createdBy = createdBy,
        createdByName = userNameMap[createdBy],
        boughtBy = boughtBy,
        boughtByName = boughtBy?.let { userNameMap[it] }
    )
}