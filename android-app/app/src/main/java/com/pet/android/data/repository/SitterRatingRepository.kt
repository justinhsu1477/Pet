package com.pet.android.data.repository

import com.pet.android.data.api.SitterRatingApi
import javax.inject.Inject

class SitterRatingRepository @Inject constructor(
    private val api: SitterRatingApi
) {
    suspend fun getRatings(sitterId: String, page: Int = 0, size: Int = 10) =
        api.getSitterRatings(sitterId, page, size)

    suspend fun getStats(sitterId: String) =
        api.getSitterRatingStats(sitterId)
}