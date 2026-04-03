package com.example.myapplication.data

import com.example.myapplication.data.api.AuthApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api= Retrofit.Builder()
        .baseUrl(AuthApi.URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
}