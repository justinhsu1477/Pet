package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

data class SitterRatingStatsResponse(
    val sitterId: String?,
    val sitterName: String?,

    @SerializedName("averageRating")
    val average: Double,

    val averageProfessionalism: Double?,
    val averageCommunication: Double?,
    val averagePunctuality: Double?,

    @SerializedName("totalRatings")
    val count: Long,

    val completedBookings: Int?,

    @SerializedName("fiveStarCount")
    val star5: Long,

    @SerializedName("fourStarCount")
    val star4: Long,

    @SerializedName("threeStarCount")
    val star3: Long,

    @SerializedName("twoStarCount")
    val star2: Long,

    @SerializedName("oneStarCount")
    val star1: Long
)
