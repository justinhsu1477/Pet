package com.pet.android.data.api

import com.pet.android.data.model.ApiResponse
import com.pet.android.data.model.ConfirmBookingRequest
import com.pet.android.data.model.RejectBookingRequest
import com.pet.android.data.model.SitterBookingResponse
import retrofit2.http.*

/**
 * 保母預約管理 API
 * 對應後端 SitterBookingController
 */
interface SitterBookingApi {

    /**
     * 取得保母的所有預約
     * GET /api/sitter/{sitterId}/bookings
     */
    @GET("api/sitter/{sitterId}/bookings")
    suspend fun getSitterBookings(
        @Path("sitterId") sitterId: String
    ): ApiResponse<List<SitterBookingResponse>>

    /**
     * 取得保母待處理的預約
     * GET /api/sitter/{sitterId}/bookings/pending
     */
    @GET("api/sitter/{sitterId}/bookings/pending")
    suspend fun getPendingBookings(
        @Path("sitterId") sitterId: String
    ): ApiResponse<List<SitterBookingResponse>>

    /**
     * 取得預約詳情
     * GET /api/sitter/{sitterId}/bookings/{bookingId}
     */
    @GET("api/sitter/{sitterId}/bookings/{bookingId}")
    suspend fun getBookingDetail(
        @Path("sitterId") sitterId: String,
        @Path("bookingId") bookingId: String
    ): ApiResponse<SitterBookingResponse>

    /**
     * 保母確認預約
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/confirm
     */
    @POST("api/sitter/{sitterId}/bookings/{bookingId}/confirm")
    suspend fun confirmBooking(
        @Path("sitterId") sitterId: String,
        @Path("bookingId") bookingId: String,
        @Body request: ConfirmBookingRequest
    ): ApiResponse<SitterBookingResponse>

    /**
     * 保母拒絕預約
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/reject
     */
    @POST("api/sitter/{sitterId}/bookings/{bookingId}/reject")
    suspend fun rejectBooking(
        @Path("sitterId") sitterId: String,
        @Path("bookingId") bookingId: String,
        @Body request: RejectBookingRequest
    ): ApiResponse<SitterBookingResponse>

    /**
     * 完成預約
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/complete
     */
    @POST("api/sitter/{sitterId}/bookings/{bookingId}/complete")
    suspend fun completeBooking(
        @Path("sitterId") sitterId: String,
        @Path("bookingId") bookingId: String
    ): ApiResponse<SitterBookingResponse>

    /**
     * 取消預約
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/cancel
     */
    @POST("api/sitter/{sitterId}/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Path("sitterId") sitterId: String,
        @Path("bookingId") bookingId: String,
        @Body reason: String?
    ): ApiResponse<SitterBookingResponse>
}
