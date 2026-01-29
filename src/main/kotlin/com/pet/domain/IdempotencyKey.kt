package com.pet.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "idempotency_keys")
class IdempotencyKey(
    @Id
    @Column(name = "idempotency_key", length = 64)
    val key: String,

    @Column(name = "response_body", columnDefinition = "TEXT")
    var responseBody: String? = null,

    @Column(name = "http_status")
    var httpStatus: Int = 200,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusHours(24)
)
