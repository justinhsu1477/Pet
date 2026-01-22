package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.Pet
import retrofit2.http.*

interface PetApi {
    @GET("api/pets")
    suspend fun getAllPets(): ApiResponse<List<Pet>>

    @GET("api/pets/{id}")
    suspend fun getPetById(@Path("id") id: String): ApiResponse<Pet>

    @GET("api/pets/user/{userId}")
    suspend fun getPetsByUser(@Path("userId") userId: String): ApiResponse<List<Pet>>

    @POST("api/pets")
    suspend fun createPet(@Body pet: Pet): ApiResponse<Pet>

    @PUT("api/pets/{id}")
    suspend fun updatePet(@Path("id") id: String, @Body pet: Pet): ApiResponse<Pet>

    @DELETE("api/pets/{id}")
    suspend fun deletePet(@Path("id") id: String): ApiResponse<Void>
}
