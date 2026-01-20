package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

data class PetActivityResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("petId")
    val petId: String,
    @SerializedName("activityDate")
    val activityDate: String,
    @SerializedName("walked")
    val walked: Boolean?,
    @SerializedName("walkTime")
    val walkTime: String?,
    @SerializedName("fed")
    val fed: Boolean?,
    @SerializedName("feedTime")
    val feedTime: String?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("createdAt")
    val createdAt: String?
)

data class RecordActivityRequest(
    @SerializedName("petId")
    val petId: String,
    @SerializedName("activityDate")
    val activityDate: String,
    @SerializedName("walked")
    val walked: Boolean? = null,
    @SerializedName("fed")
    val fed: Boolean? = null,
    @SerializedName("notes")
    val notes: String? = null
)
