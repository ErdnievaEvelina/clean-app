package com.example.myapplication.data

import com.example.myapplication.data.api.ActivityApi
import com.example.myapplication.data.api.AuthApi
import com.example.myapplication.data.api.HouseholdApi
import com.example.myapplication.data.api.LeaderboardApi
import com.example.myapplication.data.api.PrivilegeApi
import com.example.myapplication.data.api.TaskApi
import com.example.myapplication.data.api.TransactionApi
import com.example.myapplication.data.api.UserApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    //private const val BASE_URL = "http://10.0.2.2:8080"
    private const val BASE_URL = "https://cleaning-app-backend-m1aa.onrender.com/"
    //private const val BASE_URL = "http://192.168.0.20:8080/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val activityApi: ActivityApi = retrofit.create(ActivityApi::class.java)
    val leaderboardApi: LeaderboardApi = retrofit.create(LeaderboardApi::class.java)
    val api = retrofit.create(AuthApi::class.java)
    val userApi = retrofit.create(UserApi::class.java)
    val householdApi = retrofit.create(HouseholdApi::class.java)
    val taskApi =  retrofit.create(TaskApi::class.java)
    val privilegeApi = retrofit.create(PrivilegeApi::class.java)
    val transactioApi = retrofit.create(TransactionApi::class.java)
}

