package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.CatRequest
import retrofit2.http.*

interface CatApi {
    @GET("api/cats")
    suspend fun getAllCats(): ApiResponse<List<CatRequest>>

    @GET("api/cats/{id}")
    suspend fun getCatById(@Path("id") id: String): ApiResponse<CatRequest>

    @POST("api/cats")
    suspend fun createCat(
        @Body cat: CatRequest,
        @Query("userId") userId: String
    ): ApiResponse<CatRequest>

    @PUT("api/cats/{id}")
    suspend fun updateCat(@Path("id") id: String, @Body cat: CatRequest): ApiResponse<CatRequest>

    @DELETE("api/cats/{id}")
    suspend fun deleteCat(@Path("id") id: String): ApiResponse<Void>
}
