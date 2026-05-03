package com.example.myapplication.data.model

import com.example.myapplication.domain.model.Household

data class HouseholdWithUserInfo(
    val household: Household,
    val userHousehold: UserHouseholdResponseDTO
)
