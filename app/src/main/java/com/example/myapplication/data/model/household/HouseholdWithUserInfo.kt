package com.example.myapplication.data.model.household

import com.example.myapplication.data.model.household.UserHouseholdResponseDTO
import com.example.myapplication.domain.model.Household

data class HouseholdWithUserInfo(
    val household: Household,
    val userHousehold: UserHouseholdResponseDTO
)