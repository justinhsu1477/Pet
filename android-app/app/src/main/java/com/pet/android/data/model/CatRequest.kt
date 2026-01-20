package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

data class CatRequest(
    val id: String? = null,
    val name: String,
    val age: Int?,
    val breed: String?,
    val gender: Gender?,
    @SerializedName("ownerName")
    val ownerName: String,
    @SerializedName("ownerPhone")
    val ownerPhone: String,
    @SerializedName("specialNeeds")
    val specialNeeds: String?,
    @SerializedName("isNeutered")
    val isNeutered: Boolean?,
    @SerializedName("vaccineStatus")
    val vaccineStatus: String?,
    // Cat specific fields
    @SerializedName("isIndoor")
    val isIndoor: Boolean?,
    @SerializedName("litterBoxType")
    val litterBoxType: LitterBoxType?,
    @SerializedName("scratchingHabit")
    val scratchingHabit: ScratchingHabit?
)

enum class LitterBoxType {
    OPEN, COVERED, AUTOMATIC, TOP_ENTRY
}

enum class ScratchingHabit {
    LOW, MODERATE, HIGH
}
