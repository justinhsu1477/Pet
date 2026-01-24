package com.pet.dto

import java.time.LocalDateTime
import java.util.UUID

/**
 * 一般使用者（飼主）DTO
 */
data class CustomerDto(
    val id: UUID?,
    val userId: UUID?,
    val username: String?,
    val email: String?,
    val phone: String?,
    val name: String,
    val address: String?,
    val emergencyContact: String?,
    val emergencyPhone: String?,
    val memberLevel: String?,
    val totalBookings: Int?,
    val totalSpent: Double?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
