package com.example.myapplication.data.api

import com.example.myapplication.data.model.user.UserResponse
import com.example.myapplication.data.model.privilege.PrivilegeRegisterDTO
import com.example.myapplication.data.model.privilege.PrivilegeResponseDTO
import com.example.myapplication.domain.model.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PrivilegeApi {
    @POST("/api/households/{householdId}/privileges")
    suspend fun createPrivilege(
        @Header("Authorization") authorization: String,  // Bearer токен из Firebase
        @Path("householdId") householdId: String,       // ID хозяйства в URL
        @Body request: PrivilegeRegisterDTO             // {title, description, cost}
    ): PrivilegeResponseDTO
    @GET("/api/households/{householdId}/privileges")
    suspend fun getPrivileges(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String,
        @Query("filter") filter: String  // ALL, AVAILABLE, MY
    ): List<PrivilegeResponseDTO>
    @GET("/api/privileges/{privilegeId}")
    suspend fun getPrivilegeById(
        @Header("Authorization") authorization: String,
        @Path("privilegeId") privilegeId: String
    ): PrivilegeResponseDTO
    @PUT("/api/privileges/{privilegeId}")
    suspend fun updatePrivilege(
        @Header("Authorization") authorization: String,
        @Path("privilegeId") privilegeId: String,
        @Body request: PrivilegeRegisterDTO
    ): PrivilegeResponseDTO
    @DELETE("/api/privileges/{privilegeId}")
    suspend fun deletePrivilege(
        @Header("Authorization") authorization: String,
        @Path("privilegeId") privilegeId: String
    ): retrofit2.Response<Unit>

    @POST("/api/privileges/{privilegeId}/buy")
    suspend fun buyPrivilege(
        @Header("Authorization") authorization: String,
        @Path("privilegeId") privilegeId: String
    ): PrivilegeResponseDTO
    @GET("/api/users/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String?
    ): User
    @GET("/api/households/{householdId}/members")
    suspend fun getHouseholdMembers(
        @Header("Authorization") authorization: String,
        @Path("householdId") householdId: String
    ): List<UserResponse>
}