package com.pet.service

import com.pet.domain.IdempotencyKey
import com.pet.repository.IdempotencyKeyRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class IdempotencyService(
    private val idempotencyKeyRepository: IdempotencyKeyRepository
) {
    private val log = LoggerFactory.getLogger(IdempotencyService::class.java)

    fun checkExisting(key: String): IdempotencyKey? {
        return idempotencyKeyRepository.findById(key).orElse(null)
    }

    @Transactional
    fun save(key: String, responseBody: String, httpStatus: Int) {
        val idempotencyKey = IdempotencyKey(
            key = key,
            responseBody = responseBody,
            httpStatus = httpStatus
        )
        idempotencyKeyRepository.save(idempotencyKey)
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpired() {
        val deleted = idempotencyKeyRepository.deleteByExpiresAtBefore(LocalDateTime.now())
        if (deleted > 0) {
            log.info("清理了 {} 筆過期的冪等性 key", deleted)
        }
    }
}
