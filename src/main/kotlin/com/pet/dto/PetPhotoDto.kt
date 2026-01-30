package com.pet.dto

import java.time.LocalDateTime
import java.util.UUID

data class PetPhotoDto(
    val id: UUID,
    val petId: UUID?,
    val petName: String?,
    val sitterId: UUID,
    val sitterName: String,
    val photoUrl: String,
    val uploadSource: String,
    val caption: String?,
    val uploadedAt: LocalDateTime
)
