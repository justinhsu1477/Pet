package com.pet.android.data.repository

import com.pet.android.data.api.SitterBookingApi
import com.pet.android.data.model.ConfirmBookingRequest
import com.pet.android.data.model.RejectBookingRequest
import javax.inject.Inject

/**
 * 保母預約 Repository
 */
class SitterBookingRepository @Inject constructor(
    private val api: SitterBookingApi
) {
    suspend fun getSitterBookings(sitterId: String) =
        api.getSitterBookings(sitterId)

    suspend fun getPendingBookings(sitterId: String) =
        api.getPendingBookings(sitterId)

    suspend fun getBookingDetail(sitterId: String, bookingId: String) =
        api.getBookingDetail(sitterId, bookingId)

    suspend fun confirmBooking(sitterId: String, bookingId: String, response: String?) =
        api.confirmBooking(sitterId, bookingId, ConfirmBookingRequest(response))

    suspend fun rejectBooking(sitterId: String, bookingId: String, reason: String?) =
        api.rejectBooking(sitterId, bookingId, RejectBookingRequest(reason))

    suspend fun completeBooking(sitterId: String, bookingId: String) =
        api.completeBooking(sitterId, bookingId)

    suspend fun cancelBooking(sitterId: String, bookingId: String, reason: String?) =
        api.cancelBooking(sitterId, bookingId, reason)
}
