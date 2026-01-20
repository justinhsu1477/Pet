package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.DogRequest
import retrofit2.http.*

interface DogApi {
    @GET("api/dogs")
    suspend fun getAllDogs(): ApiResponse<List<DogRequest>>

    @GET("api/dogs/{id}")
    suspend fun getDogById(@Path("id") id: String): ApiResponse<DogRequest>

    @POST("api/dogs")
    suspend fun createDog(@Body dog: DogRequest): ApiResponse<DogRequest>

    @PUT("api/dogs/{id}")
    suspend fun updateDog(@Path("id") id: String, @Body dog: DogRequest): ApiResponse<DogRequest>

    @DELETE("api/dogs/{id}")
    suspend fun deleteDog(@Path("id") id: String): ApiResponse<Void>
}
