package com.pet.android.data.model

data class SitterRatingStatsResponse(
    val average: Double,
    val count: Long,
    val star1: Long,
    val star2: Long,
    val star3: Long,
    val star4: Long,
    val star5: Long
)