package com.pet.service

import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

/**
 * WebSocket 即時通知服務
 *
 * 透過 STOMP 將通知推送給特定使用者
 * 前端訂閱 /user/queue/notifications 接收
 *
 * 與 LINE 通知互補：
 * - WebSocket：使用者在網站上時即時顯示 toast
 * - LINE：使用者不在網站上時推播通知
 */
@Service
class WebSocketNotificationService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val log = LoggerFactory.getLogger(WebSocketNotificationService::class.java)

    /**
     * 發送通知給指定使用者
     * @param userId 目標使用者 ID（對應 Principal.getName()）
     * @param notification 通知內容
     */
    fun sendNotification(userId: String, notification: NotificationMessage) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                notification
            )
            log.debug("WebSocket 通知已發送給使用者: {}", userId)
        } catch (e: Exception) {
            // WebSocket 發送失敗不影響主流程
            log.warn("WebSocket 通知發送失敗 (userId={}): {}", userId, e.message)
        }
    }
}


/**
 * 通知訊息 DTO
 */
data class NotificationMessage(
    val type: String,       // BOOKING_CONFIRMED, BOOKING_CANCELLED, etc.
    val title: String,      // 通知標題
    val message: String,    // 通知內容
    val bookingId: String?  // 相關預約 ID（可選）
)
