package com.pet.android.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Pet(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("age")
    val age: Int,
    @SerializedName("breed")
    val breed: String?,
    @SerializedName("specialNeeds")
    val specialNeeds: String?
) : Serializable
