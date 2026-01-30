package com.pet.service

import com.pet.config.LineMessagingConfig
import com.pet.domain.Booking.BookingStatus
import com.pet.dto.LineWebhookEvent
import com.pet.repository.BookingRepository
import com.pet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * LINE Webhook 事件處理服務
 * - 簽名驗證（HMAC-SHA256）
 * - 事件路由分發
 * - Postback 事件處理（Rich Menu / Quick Reply）
 * - 文字指令處理
 */
@Service
class LineWebhookService(
    private val config: LineMessagingConfig,
    private val petPhotoService: PetPhotoService,
    private val lineContentService: LineContentService,
    private val sitterPetSelectionCache: SitterPetSelectionCache,
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository,
    private val lineRichMenuService: LineRichMenuService
) {
    private val log = LoggerFactory.getLogger(LineWebhookService::class.java)

    /**
     * 驗證 LINE Webhook 簽名
     */
    fun verifySignature(body: String, signature: String): Boolean {
        return try {
            val secretKey = SecretKeySpec(
                config.getChannelSecret().toByteArray(StandardCharsets.UTF_8),
                "HmacSHA256"
            )
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(secretKey)
            val expectedSignature = Base64.getEncoder().encodeToString(
                mac.doFinal(body.toByteArray(StandardCharsets.UTF_8))
            )
            expectedSignature == signature
        } catch (e: Exception) {
            log.error("簽名驗證異常: {}", e.message)
            false
        }
    }

    /**
     * 處理 Webhook 事件列表
     */
    fun handleWebhookEvents(events: List<LineWebhookEvent>) {
        events.forEach { event ->
            try {
                when (event.type) {
                    "message" -> handleMessageEvent(event)
                    "postback" -> handlePostbackEvent(event)
                    "follow" -> handleFollowEvent(event)
                    "unfollow" -> log.info("用戶封鎖: {}", event.source.userId)
                    else -> log.debug("未處理的事件類型: {}", event.type)
                }
            } catch (e: Exception) {
                log.error("處理 Webhook 事件失敗: type={}, error={}", event.type, e.message, e)
            }
        }
    }

    private fun handleMessageEvent(event: LineWebhookEvent) {
        val message = event.message ?: return

        when (message.type) {
            "image" -> {
                log.info("收到圖片訊息: messageId={}, userId={}", message.id, event.source.userId)
                petPhotoService.handlePhotoFromLine(
                    messageId = message.id,
                    lineUserId = event.source.userId,
                    replyToken = event.replyToken
                )
            }
            "text" -> {
                log.info("收到文字訊息: userId={}, text={}", event.source.userId, message.text)
                val text = message.text?.trim() ?: return
                when {
                    text == "上傳照片" || text == "📷 上傳照片" -> {
                        handleUploadPhotoRequest(event.source.userId, event.replyToken)
                    }
                    else -> log.debug("未處理的文字指令: {}", text)
                }
            }
            else -> log.debug("未處理的訊息類型: {}", message.type)
        }
    }

    /**
     * 處理 Postback 事件（Rich Menu 按鈕 / Quick Reply 選擇）
     */
    private fun handlePostbackEvent(event: LineWebhookEvent) {
        val data = event.postback?.data ?: return
        val params = parsePostbackData(data)
        val lineUserId = event.source.userId
        val replyToken = event.replyToken

        log.info("收到 Postback: userId={}, data={}", lineUserId, data)

        when (params["action"]) {
            "upload_photo" -> {
                handleUploadPhotoRequest(lineUserId, replyToken)
            }
            "select_pet" -> {
                val petIdStr = params["petId"] ?: return
                handleSelectPet(lineUserId, replyToken, petIdStr)
            }
            "my_bookings" -> {
                val frontendUrl = config.getFrontendUrl() ?: "http://localhost:3000"
                lineContentService.sendReplyMessage(replyToken ?: return,
                    "📋 我的預約\n\n$frontendUrl/bookings")
            }
            "my_pets" -> {
                val frontendUrl = config.getFrontendUrl() ?: "http://localhost:3000"
                lineContentService.sendReplyMessage(replyToken ?: return,
                    "🐾 我的寵物\n\n$frontendUrl/pets")
            }
            else -> log.debug("未處理的 Postback action: {}", params["action"])
        }
    }

    /**
     * 處理「上傳照片」請求
     * 查詢保母的 CONFIRMED 預約，列出可選寵物
     */
    private fun handleUploadPhotoRequest(lineUserId: String, replyToken: String?) {
        if (replyToken == null) return

        // 1. 找用戶和保母身份
        val user = userRepository.findByLineUserId(lineUserId).orElse(null)
        if (user == null) {
            lineContentService.sendReplyMessage(replyToken, "⚠️ 您的 LINE 帳號尚未綁定系統帳號，請先在系統中使用 LINE 登入。")
            return
        }

        val sitter = user.getSitter()
        if (sitter == null) {
            lineContentService.sendReplyMessage(replyToken, "⚠️ 此功能僅限保母使用。")
            return
        }

        // 2. 查 CONFIRMED 預約
        val confirmedBookings = bookingRepository.findBySitterIdAndStatus(sitter.getId(), BookingStatus.CONFIRMED)

        // 取出不重複的寵物
        val distinctPets = confirmedBookings
            .map { it.getPet() }
            .distinctBy { it.getId() }

        when {
            distinctPets.isEmpty() -> {
                lineContentService.sendReplyMessage(replyToken,
                    "📭 目前沒有進行中的預約。\n\n您可以直接傳送照片，系統會暫時儲存。")
            }
            distinctPets.size == 1 -> {
                // 只有一隻寵物，自動選擇
                val pet = distinctPets[0]
                sitterPetSelectionCache.select(lineUserId, pet.getId())
                lineContentService.sendReplyMessage(replyToken,
                    "🐾 已自動選擇寵物：${pet.getName()}\n\n請傳送照片，將自動關聯到 ${pet.getName()}。")
            }
            else -> {
                // 多隻寵物，顯示 Quick Reply 讓保母選擇
                val petList = distinctPets.map { Pair(it.getId(), it.getName()) }
                lineContentService.sendPetSelectionReply(
                    replyToken,
                    "📸 請選擇要上傳照片的寵物：",
                    petList
                )
            }
        }
    }

    /**
     * 處理保母選擇寵物
     */
    private fun handleSelectPet(lineUserId: String, replyToken: String?, petIdStr: String) {
        if (replyToken == null) return

        try {
            val petId = UUID.fromString(petIdStr)
            sitterPetSelectionCache.select(lineUserId, petId)

            // 找寵物名稱來回覆
            val user = userRepository.findByLineUserId(lineUserId).orElse(null)
            val sitter = user?.getSitter()
            var petName = "寵物"

            if (sitter != null) {
                val bookings = bookingRepository.findBySitterIdAndStatus(sitter.getId(), BookingStatus.CONFIRMED)
                val pet = bookings.map { it.getPet() }.find { it.getId() == petId }
                if (pet != null) {
                    petName = pet.getName()
                }
            }

            lineContentService.sendReplyMessage(replyToken,
                "✅ 已選擇：${petName}\n\n請傳送照片，將自動關聯到 ${petName}。\n⏰ 選擇有效期 10 分鐘。")
        } catch (e: Exception) {
            log.error("選擇寵物失敗: {}", e.message)
            lineContentService.sendReplyMessage(replyToken, "❌ 選擇失敗，請重試。")
        }
    }

    /**
     * 處理 follow 事件（用戶加入好友）
     * 若已綁定帳號 → 依角色綁定 Rich Menu
     * 若未綁定 → 顯示 default Rich Menu + 歡迎訊息
     */
    private fun handleFollowEvent(event: LineWebhookEvent) {
        val lineUserId = event.source.userId
        log.info("用戶加入好友: {}", lineUserId)

        val user = userRepository.findByLineUserId(lineUserId).orElse(null)
        if (user != null) {
            // 已綁定帳號 → 依角色綁定 Rich Menu
            lineRichMenuService.assignMenuToUser(lineUserId, user.getRole())
            lineContentService.sendReplyMessage(event.replyToken ?: return,
                "🎉 歡迎回來！已為您載入${if (user.getRole().name == "SITTER") "保母" else "飼主"}專屬選單。")
        } else {
            // 未綁定 → 歡迎訊息引導註冊
            lineContentService.sendReplyMessage(event.replyToken ?: return,
                "🐾 歡迎使用寵物保母媒合系統！\n\n請點選下方選單「前往登入」綁定您的帳號，即可使用完整功能。")
        }
    }

    /**
     * 解析 Postback data（格式：action=xxx&petId=yyy）
     */
    private fun parsePostbackData(data: String): Map<String, String> {
        return data.split("&").mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }
}
