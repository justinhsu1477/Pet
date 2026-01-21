package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

data class Sitter(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("experience")
    val experience: String?,
    @SerializedName("averageRating")
    val averageRating: Double? = null,
    @SerializedName("ratingCount")
    val ratingCount: Int? = null,
    @SerializedName("completedBookings")
    val completedBookings: Int? = null
)
