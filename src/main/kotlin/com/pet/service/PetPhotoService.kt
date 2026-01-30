package com.pet.service

import com.pet.domain.Booking.BookingStatus
import com.pet.domain.PetPhoto
import com.pet.dto.PetPhotoDto
import com.pet.repository.BookingRepository
import com.pet.repository.PetPhotoRepository
import com.pet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PetPhotoService(
    private val petPhotoRepository: PetPhotoRepository,
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService,
    private val lineContentService: LineContentService,
    private val webSocketNotificationService: WebSocketNotificationService,
    private val sitterPetSelectionCache: SitterPetSelectionCache
) {
    private val log = LoggerFactory.getLogger(PetPhotoService::class.java)

    /**
     * 處理 LINE 傳來的照片
     */
    @Transactional
    fun handlePhotoFromLine(messageId: String, lineUserId: String, replyToken: String?) {
        // 防止重複處理
        if (petPhotoRepository.findByMessageId(messageId).isPresent) {
            log.warn("重複的 LINE 訊息，跳過: messageId={}", messageId)
            return
        }

        // 1. 找保母
        val user = userRepository.findByLineUserId(lineUserId).orElse(null)
        if (user == null) {
            log.warn("找不到 LINE 用戶: lineUserId={}", lineUserId)
            replyToken?.let {
                lineContentService.sendReplyMessage(it, "⚠️ 您的 LINE 帳號尚未綁定系統帳號，請先在系統中使用 LINE 登入。")
            }
            return
        }

        val sitter = user.getSitter()
        if (sitter == null) {
            log.warn("此用戶不是保母: userId={}", user.getId())
            replyToken?.let {
                lineContentService.sendReplyMessage(it, "⚠️ 此功能僅限保母使用。")
            }
            return
        }

        // 2. 先檢查 Rich Menu 預選的寵物
        val confirmedBookings = bookingRepository.findBySitterIdAndStatus(sitter.getId(), BookingStatus.CONFIRMED)
        val preSelectedPetId = sitterPetSelectionCache.getAndClear(lineUserId)

        val pet = if (preSelectedPetId != null) {
            // 從 CONFIRMED 預約中找匹配的寵物（確保保母確實有該預約）
            val matched = confirmedBookings.map { it.getPet() }.find { it.getId() == preSelectedPetId }
            if (matched != null) {
                log.info("使用 Rich Menu 預選寵物: petId={}", preSelectedPetId)
                matched
            } else {
                log.warn("預選寵物不在 CONFIRMED 預約中，改用自動偵測: petId={}", preSelectedPetId)
                autoDetectPet(confirmedBookings)
            }
        } else {
            // 無預選，走原有自動偵測邏輯
            autoDetectPet(confirmedBookings)
        }

        // 3. 下載圖片
        val imageBytes: ByteArray
        try {
            imageBytes = lineContentService.downloadImage(messageId)
        } catch (e: Exception) {
            log.error("LINE 圖片下載失敗: messageId={}, error={}", messageId, e.message)
            replyToken?.let {
                lineContentService.sendReplyMessage(it, "❌ 照片下載失敗，請稍後再試。")
            }
            return
        }

        // 4. 儲存圖片
        val petId = pet?.getId()
        val relativePath = fileStorageService.saveImage(imageBytes, petId, messageId)

        // 5. 建立 PetPhoto 記錄
        val petPhoto = PetPhoto()
        petPhoto.setPet(pet)
        petPhoto.setSitter(sitter)
        petPhoto.setPhotoUrl(relativePath)
        petPhoto.setMessageId(messageId)
        petPhoto.setUploadSource(PetPhoto.UploadSource.LINE)
        val saved = petPhotoRepository.save(petPhoto)

        log.info("寵物照片已儲存: photoId={}, petId={}, sitterId={}", saved.getId(), petId, sitter.getId())

        // 6. LINE 回覆確認
        replyToken?.let {
            val petName = pet?.getName()
            val petInfo = if (petName != null) "🐾 寵物：${petName}\n" else "⚠️ 目前無進行中的預約，照片暫存未關聯。\n"
            val message = "📸 照片上傳成功！\n\n${petInfo}✅ 已儲存至系統\n\n飼主可以在系統中查看照片。"
            lineContentService.sendReplyMessage(it, message)
        }

        // 7. WebSocket 通知飼主
        val owner = pet?.getOwner()
        if (owner != null) {
            try {
                webSocketNotificationService.sendNotification(
                    owner.getId().toString(),
                    NotificationMessage(
                        type = "PET_PHOTO_UPLOADED",
                        title = "收到新照片",
                        message = "${sitter.getName()} 上傳了 ${pet!!.getName()} 的照片",
                        bookingId = null
                    )
                )
            } catch (e: Exception) {
                log.debug("WebSocket 通知失敗（不影響主流程）: {}", e.message)
            }
        }
    }

    /**
     * 查詢寵物的照片
     */
    @Transactional(readOnly = true)
    fun getPhotosByPet(petId: UUID): List<PetPhotoDto> {
        return petPhotoRepository.findByPetIdOrderByUploadedAtDesc(petId)
            .map { toDto(it) }
    }

    /**
     * 查詢保母上傳的照片
     */
    @Transactional(readOnly = true)
    fun getPhotosBySitter(sitterId: UUID): List<PetPhotoDto> {
        return petPhotoRepository.findBySitterIdOrderByUploadedAtDesc(sitterId)
            .map { toDto(it) }
    }

    /**
     * 手動關聯照片到寵物
     */
    @Transactional
    fun associatePhotoWithPet(photoId: UUID, petId: UUID): PetPhotoDto {
        val photo = petPhotoRepository.findById(photoId)
            .orElseThrow { IllegalArgumentException("找不到照片: $photoId") }

        val bookings = bookingRepository.findByPetIdOrderByCreatedAtDesc(petId)
        val pet = bookings.firstOrNull()?.getPet()
            ?: throw IllegalArgumentException("找不到寵物: $petId")

        photo.setPet(pet)
        val saved = petPhotoRepository.save(photo)
        log.info("照片已關聯到寵物: photoId={}, petId={}", photoId, petId)
        return toDto(saved)
    }

    /**
     * 自動偵測寵物（原有邏輯）
     */
    private fun autoDetectPet(confirmedBookings: List<com.pet.domain.Booking>): com.pet.domain.Pet? {
        return when {
            confirmedBookings.size == 1 -> confirmedBookings[0].getPet()
            confirmedBookings.isEmpty() -> null
            else -> confirmedBookings.minByOrNull { it.getStartTime() }?.getPet()
        }
    }

    private fun toDto(photo: PetPhoto): PetPhotoDto {
        return PetPhotoDto(
            id = photo.getId(),
            petId = photo.getPet()?.getId(),
            petName = photo.getPet()?.getName(),
            sitterId = photo.getSitter().getId(),
            sitterName = photo.getSitter().getName(),
            photoUrl = "/api/pet-photos/image/${photo.getPhotoUrl()}",
            uploadSource = photo.getUploadSource().name,
            caption = photo.getCaption(),
            uploadedAt = photo.getUploadedAt()
        )
    }
}
