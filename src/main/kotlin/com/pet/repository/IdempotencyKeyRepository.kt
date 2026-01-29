package com.pet.repository

import com.pet.domain.IdempotencyKey
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface IdempotencyKeyRepository : JpaRepository<IdempotencyKey, String> {
    fun deleteByExpiresAtBefore(time: LocalDateTime): Long
}
