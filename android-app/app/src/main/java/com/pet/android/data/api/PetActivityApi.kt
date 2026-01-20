package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.PetActivityResponse
import com.pet.android.data.model.RecordActivityRequest
import retrofit2.http.*

interface PetActivityApi {
    @GET("api/pets/{petId}/activities/today")
    suspend fun getTodayActivity(@Path("petId") petId: String): ApiResponse<PetActivityResponse>

    @POST("api/pets/{petId}/activities")
    suspend fun recordActivity(
        @Path("petId") petId: String,
        @Body request: RecordActivityRequest
    ): ApiResponse<PetActivityResponse>

    @GET("api/pets/{petId}/activities")
    suspend fun getActivityHistory(@Path("petId") petId: String): ApiResponse<List<PetActivityResponse>>
}
