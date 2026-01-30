package com.pet.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 保母寵物選擇快取
 * 用於 Rich Menu 流程：保母選擇寵物後，暫存選擇結果
 * 等保母傳照片時，自動關聯到已選擇的寵物
 *
 * TTL: 10 分鐘，用完即清除
 */
@Component
class SitterPetSelectionCache {
    private val log = LoggerFactory.getLogger(SitterPetSelectionCache::class.java)

    // lineUserId -> (petId, expiryTimestamp)
    private val selectionMap = ConcurrentHashMap<String, Pair<UUID, Long>>()

    companion object {
        private const val TTL_MS = 10 * 60 * 1000L // 10 分鐘
    }

    /**
     * 記錄保母選擇的寵物
     */
    fun select(lineUserId: String, petId: UUID) {
        selectionMap[lineUserId] = Pair(petId, System.currentTimeMillis() + TTL_MS)
        log.info("保母寵物選擇已記錄: lineUserId={}, petId={}", lineUserId, petId)
    }

    /**
     * 取出並清除選擇（用完即丟）
     * 若已過期則返回 null
     */
    fun getAndClear(lineUserId: String): UUID? {
        val entry = selectionMap.remove(lineUserId) ?: return null
        return if (System.currentTimeMillis() < entry.second) {
            log.info("取出保母寵物選擇: lineUserId={}, petId={}", lineUserId, entry.first)
            entry.first
        } else {
            log.debug("保母寵物選擇已過期: lineUserId={}", lineUserId)
            null
        }
    }

    /**
     * 查看目前選擇（不清除）
     */
    fun peek(lineUserId: String): UUID? {
        val entry = selectionMap[lineUserId] ?: return null
        return if (System.currentTimeMillis() < entry.second) entry.first else null
    }

    /**
     * 每 5 分鐘清理過期條目
     */
    @Scheduled(fixedRate = 300000)
    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        val expiredCount = selectionMap.entries.count { it.value.second < now }
        if (expiredCount > 0) {
            selectionMap.entries.removeIf { it.value.second < now }
            log.debug("清理過期的寵物選擇快取: {} 筆", expiredCount)
        }
    }
}
