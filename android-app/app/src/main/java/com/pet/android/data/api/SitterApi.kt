package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.Sitter
import retrofit2.http.*

interface SitterApi {
    @GET("api/sitters")
    suspend fun getAllSitters(): ApiResponse<List<Sitter>>

    @GET("api/sitters/{id}")
    suspend fun getSitterById(@Path("id") id: String): ApiResponse<Sitter>

    @POST("api/sitters")
    suspend fun createSitter(@Body sitter: Sitter): ApiResponse<Sitter>

    @PUT("api/sitters/{id}")
    suspend fun updateSitter(@Path("id") id: String, @Body sitter: Sitter): ApiResponse<Sitter>

    @DELETE("api/sitters/{id}")
    suspend fun deleteSitter(@Path("id") id: String): ApiResponse<Void>
}
