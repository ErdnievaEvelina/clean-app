package com.example.myapplication.data.api

import com.example.myapplication.data.model.task.TaskFilterType
import com.example.myapplication.data.model.task.TaskRegisterDTO
import com.example.myapplication.data.model.task.TaskResponseDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApi {
    @POST("/api/households/{householdId}/tasks")
    suspend fun createTask(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String,
        @Body request: TaskRegisterDTO
    ): TaskResponseDTO
    @GET("/api/households/{householdId}/tasks")
    suspend fun getHouseholdTasks(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String,
        @Query("filter") filter: String
    ): List<TaskResponseDTO>
    @GET("/api/tasks/{taskId}")
    suspend fun getTaskById(
        @Header("Authorization") authorization: String,
        @Path("taskId") taskId: String
    ): TaskResponseDTO
    @PUT("/api/tasks/{taskId}")
    suspend fun updateTask(
        @Header("Authorization") authorization: String,
        @Path("taskId") taskId: String,
        @Body request: TaskRegisterDTO
    ): TaskResponseDTO
    @DELETE("/api/tasks/{taskId}")
    suspend fun deleteTask(
        @Header("Authorization") authorization: String,
        @Path("taskId") taskId: String
    ): retrofit2.Response<Unit>
    @POST("/api/tasks/{taskId}/assign")
    suspend fun assignTask(
        @Header("Authorization") authorization: String,
        @Path("taskId") taskId: String
    ): TaskResponseDTO
    @POST("/api/tasks/{taskId}/unassign")
    suspend fun unassignTask(
        @Header("Authorization") authorization: String,
        @Path("taskId") taskId: String
    ): TaskResponseDTO
    @POST("/api/tasks/{taskId}/complete")
    suspend fun completeTask(
        @Header("Authorization") authorization: String,
        @Path("taskId") taskId: String
    ): TaskResponseDTO

}