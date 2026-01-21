package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.PageResponse
import com.pet.android.data.model.SitterRatingResponse
import com.pet.android.data.model.SitterRatingStatsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SitterRatingApi {
    @GET("api/ratings/sitter/{sitterId}")
    suspend fun getSitterRatings(
        @Path("sitterId") sitterId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<SitterRatingResponse>>

    @GET("api/ratings/sitter/{sitterId}/stats")
    suspend fun getSitterRatingStats(
        @Path("sitterId") sitterId: String
    ): ApiResponse<SitterRatingStatsResponse>
}