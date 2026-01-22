package com.pet.android.data.model

data class BookingResponse(
    val id: String,
    val petId: String,
    val sitterId: String,
    val userId: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val notes: String?,
    val totalPrice: Double?,
    val createdAt: String?
)
