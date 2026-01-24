package com.pet.web

import com.pet.dto.BookingDto
import com.pet.dto.request.ConfirmBookingRequest
import com.pet.dto.request.RejectBookingRequest
import com.pet.dto.response.ApiResponse
import com.pet.dto.response.BookingStatisticsResponse
import com.pet.service.BookingService
import com.pet.service.BookingStatisticsService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 保母預約管理 Controller
 *
 * 與 BookingController 的區別：
 * - BookingController: 通用預約管理（含用戶、保母、系統管理視角）
 */
@RestController
@RequestMapping("/api/sitter")
class SitterBookingController(
    private val bookingService: BookingService,
    private val bookingStatisticsService: BookingStatisticsService
) {

    /***
     * 取得保母的所有預約
     * GET /api/sitter/{sitterId}/bookings
     */
    @GetMapping("/{sitterId}/bookings")
    fun getMyBookings(@PathVariable sitterId: UUID): ResponseEntity<ApiResponse<List<BookingDto>>> {
        val bookings: List<BookingDto> = bookingService.getBookingsBySitter(sitterId)
        return ResponseEntity.ok(ApiResponse.success(bookings))
    }

    /**
     * 取得保母待處理的預約
     * GET /api/sitter/{sitterId}/bookings/pending
     */
    @GetMapping("/{sitterId}/bookings/pending")
    fun getPendingBookings(@PathVariable sitterId: UUID): ResponseEntity<ApiResponse<List<BookingDto>>> {
        val bookings = bookingService.getPendingBookingsForSitter(sitterId)
        return ResponseEntity.ok(
            ApiResponse.success(
                if (bookings.isEmpty()) "目前沒有待處理的預約" else "找到 ${bookings.size} 筆待處理預約",
                bookings
            )
        )
    }

    /**
     * 保母確認預約
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/confirm
     */
    @PostMapping("/{sitterId}/bookings/{bookingId}/confirm")
    fun confirmBooking(
        @PathVariable sitterId: UUID,
        @PathVariable bookingId: UUID,
        @Valid @RequestBody request: ConfirmBookingRequest?
    ): ResponseEntity<ApiResponse<BookingDto>> {
        // Kotlin 的空安全語法：request?.response 等同於 request != null ? request.getResponse() : null
        val confirmed = bookingService.confirmBooking(bookingId, request?.response)

        return ResponseEntity.ok(
            ApiResponse.success(
                "您已成功確認此預約！飼主將收到通知。",
                confirmed
            )
        )
    }

    /**
     * 保母拒絕預約
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/reject
     *
     */
    @PostMapping("/{sitterId}/bookings/{bookingId}/reject")
    fun rejectBooking(
        @PathVariable sitterId: UUID,
        @PathVariable bookingId: UUID,
        @Valid @RequestBody request: RejectBookingRequest?
    ): ResponseEntity<ApiResponse<BookingDto>> {
        val reason = request?.reason ?: "保母未提供原因"
        val rejected = bookingService.rejectBooking(bookingId, reason)

        return ResponseEntity.ok(
            ApiResponse.success(
                "已拒絕此預約，飼主將收到通知。",
                rejected
            )
        )
    }

    /**
     * 取得預約詳情（保母視角）
     * GET /api/sitter/{sitterId}/bookings/{bookingId}
     *
     * Kotlin 表達式函數體：單行函數可以用 = 簡化
     */
    @GetMapping("/{sitterId}/bookings/{bookingId}")
    fun getBookingDetail(
        @PathVariable sitterId: UUID,
        @PathVariable bookingId: UUID
    ): ResponseEntity<ApiResponse<BookingDto>> =
        ResponseEntity.ok(
            ApiResponse.success(bookingService.getBookingById(bookingId))
        )

    /**
     * 完成預約（保母標記服務完成）
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/complete
     */
    @PostMapping("/{sitterId}/bookings/{bookingId}/complete")
    fun completeBooking(
        @PathVariable sitterId: UUID,
        @PathVariable bookingId: UUID
    ): ResponseEntity<ApiResponse<BookingDto>> {
        val completed = bookingService.completeBooking(bookingId)

        return ResponseEntity.ok(
            ApiResponse.success(
                "預約已完成！感謝您的服務。",
                completed
            )
        )
    }

    /**
     * 保母取消預約
     * POST /api/sitter/{sitterId}/bookings/{bookingId}/cancel
     */
    @PostMapping("/{sitterId}/bookings/{bookingId}/cancel")
    fun cancelBooking(
        @PathVariable sitterId: UUID,
        @PathVariable bookingId: UUID,
        @RequestBody(required = false) reason: String?
    ): ResponseEntity<ApiResponse<BookingDto>> {
        val cancelled = bookingService.cancelBooking(bookingId, reason)

        return ResponseEntity.ok(
            ApiResponse.success(
                "預約已取消。",
                cancelled
            )
        )
    }

    /**
     * 取得保母的統計資料
     * GET /api/sitter/{sitterId}/statistics
     *
     * 包含：
     * - 預約統計（本月總數、待確認、已完成、拒絕/取消）
     * - 收入統計（本月收入、本週收入、每日趨勢）
     * - 評價統計（平均評分、五星比例、星級分布、最新評價）
     */
    @GetMapping("/{sitterId}/statistics")
    fun getStatistics(
        @PathVariable sitterId: UUID
    ): ResponseEntity<ApiResponse<BookingStatisticsResponse>> {
        val statistics = bookingStatisticsService.getStatistics(sitterId)

        return ResponseEntity.ok(
            ApiResponse.success(
                "成功取得統計資料",
                statistics
            )
        )
    }
}
