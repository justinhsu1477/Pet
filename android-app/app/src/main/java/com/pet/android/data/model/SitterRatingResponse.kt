package com.pet.android.data.model

data class SitterRatingResponse(
    val id: String,
    val bookingId: String?,
    val sitterId: String,
    val userId: String?,
    val overallRating: Int,
    val comment: String?,
    val reply: String?,
    val createdAt: String
)