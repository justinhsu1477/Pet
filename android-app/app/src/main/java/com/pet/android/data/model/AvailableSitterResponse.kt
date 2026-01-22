package com.pet.android.data.model

import java.io.Serializable

data class AvailableSitterResponse(
    val id: String,
    val name: String,
    val phone: String?,
    val email: String?,
    val experience: String?,
    val averageRating: Double?,
    val ratingCount: Int?,
    val completedBookings: Int?
) : Serializable
