package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.AvailableSitterResponse
import com.pet.android.data.model.BookingRequest
import com.pet.android.data.model.BookingResponse
import retrofit2.http.*

interface BookingApi {

    @GET("api/sitters/available")
    suspend fun getAvailableSitters(
        @Query("date") date: String,
        @Query("startTime") startTime: String? = null,
        @Query("endTime") endTime: String? = null
    ): ApiResponse<List<AvailableSitterResponse>>

    @GET("api/sitters/with-rating")
    suspend fun getAllSittersWithRating(): ApiResponse<List<AvailableSitterResponse>>

    @POST("api/bookings")
    suspend fun createBooking(
        @Query("userId") userId: String,
        @Body booking: BookingRequest
    ): ApiResponse<BookingResponse>
}
