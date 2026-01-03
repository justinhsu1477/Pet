package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.LoginRequest
import com.pet.android.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
}
