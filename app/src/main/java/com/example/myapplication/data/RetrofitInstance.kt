package com.example.myapplication.data

import com.example.myapplication.data.api.AuthApi
import com.example.myapplication.data.api.HouseholdApi
import com.example.myapplication.data.api.UserApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api= Retrofit.Builder()
        .baseUrl(AuthApi.URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
    val userApi = Retrofit.Builder()
        .baseUrl(UserApi.URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UserApi::class.java)
    val householdApi = Retrofit.Builder()
            .baseUrl(HouseholdApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HouseholdApi::class.java)

}