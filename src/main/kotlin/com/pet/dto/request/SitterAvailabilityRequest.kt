package com.pet.dto.request

import java.time.DayOfWeek
import java.time.LocalTime

data class SitterAvailabilityRequest(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val serviceArea: String? = null,
    val isActive: Boolean = true
)
