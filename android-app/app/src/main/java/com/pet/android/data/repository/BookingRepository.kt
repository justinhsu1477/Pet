package com.pet.android.data.repository

import com.pet.android.data.api.BookingApi
import com.pet.android.data.model.BookingRequest
import javax.inject.Inject

class BookingRepository @Inject constructor(
    private val api: BookingApi
) {
    suspend fun getAvailableSitters(date: String) = api.getAvailableSitters(date)

    suspend fun getAllSittersWithRating() = api.getAllSittersWithRating()

    suspend fun createBooking(userId: String, booking: BookingRequest) =
        api.createBooking(userId, booking)
}
