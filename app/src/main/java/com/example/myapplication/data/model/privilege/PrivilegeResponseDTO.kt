package com.example.myapplication.data.model.privilege

data class PrivilegeResponseDTO(
    val id: String,           // ID привилегии
    val householdId: String,  // ID хозяйства
    val createdBy: String,    // ID создателя
    val createdAt: String,    // дата создания
    val title: String,        // название
    val description: String?, // описание
    val cost: Int,            // стоимость
    val isAvailable: Boolean, // доступна для покупки (true - свободна, false - уже куплена)
    val boughtBy: String?     // ID покупателя (null если не куплена)
)