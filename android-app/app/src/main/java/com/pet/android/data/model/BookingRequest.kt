package com.pet.android.data.model

data class BookingRequest(
    val petId: String,
    val sitterId: String,
    val startTime: String,  // ISO 8601 format: "2026-01-22T09:00:00"
    val endTime: String,
    val notes: String?
)
