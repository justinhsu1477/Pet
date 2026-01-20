package com.pet.android.data.model

import com.google.gson.annotations.SerializedName

data class DogRequest(
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
    // Dog specific fields
    val size: DogSize?,
    @SerializedName("isWalkRequired")
    val isWalkRequired: Boolean?,
    @SerializedName("walkFrequencyPerDay")
    val walkFrequencyPerDay: Int?,
    @SerializedName("trainingLevel")
    val trainingLevel: TrainingLevel?,
    @SerializedName("isFriendlyWithDogs")
    val isFriendlyWithDogs: Boolean?,
    @SerializedName("isFriendlyWithPeople")
    val isFriendlyWithPeople: Boolean?,
    @SerializedName("isFriendlyWithChildren")
    val isFriendlyWithChildren: Boolean?
)

enum class Gender {
    MALE, FEMALE
}

enum class DogSize {
    SMALL, MEDIUM, LARGE, GIANT
}

enum class TrainingLevel {
    NONE, BASIC, INTERMEDIATE, ADVANCED
}
