package com.pet.scheduling

import com.pet.domain.Booking.BookingStatus
import com.pet.repository.BookingRepository
import com.pet.service.LineMessagingService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 預約自動過期排程任務
 * 每小時檢查一次，將超過24小時未回應的 PENDING 預約自動設為 EXPIRED
 */
@Component
class BookingExpiryScheduler(
    private val bookingRepository: BookingRepository,
    private val lineMessagingService: LineMessagingService
) {
    private val logger = LoggerFactory.getLogger(BookingExpiryScheduler::class.java)

    /**
     * 每小時執行一次，檢查並過期超時的預約
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    fun expireOverdueBookings() {
        val cutoffTime = LocalDateTime.now().minusHours(24)
        logger.info("開始檢查過期預約，截止時間: {}", cutoffTime)

        val overdueBookings = bookingRepository.findByStatusAndCreatedAtBefore(
            BookingStatus.PENDING, cutoffTime
        )

        if (overdueBookings.isEmpty()) {
            logger.info("沒有需要過期的預約")
            return
        }

        logger.info("找到 {} 筆需要過期的預約", overdueBookings.size)

        for (booking in overdueBookings) {
            try {
                booking.status = BookingStatus.EXPIRED
                bookingRepository.save(booking)
                logger.info("預約 {} 已自動過期（建立時間: {}）", booking.id, booking.createdAt)

                // 發送 LINE 通知給飼主
                lineMessagingService.sendBookingExpiredNotification(booking)
            } catch (e: Exception) {
                logger.error("處理預約 {} 過期時發生錯誤: {}", booking.id, e.message)
            }
        }

        logger.info("過期預約處理完成，共處理 {} 筆", overdueBookings.size)
    }
}
