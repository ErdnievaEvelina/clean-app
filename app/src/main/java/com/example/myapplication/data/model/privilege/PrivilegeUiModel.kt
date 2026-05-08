package com.example.myapplication.data.model.privilege

data class PrivilegeUiModel(
    val id: String,
    val title: String,
    val description: String?,
    val cost: Int,
    val isAvailable: Boolean,
    val createdBy: String,
    val createdByName: String?,
    val boughtBy: String?,
    val boughtByName: String?
)