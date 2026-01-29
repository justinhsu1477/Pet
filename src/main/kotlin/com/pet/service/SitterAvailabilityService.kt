package com.pet.service

import com.pet.domain.SitterAvailability
import com.pet.dto.request.SitterAvailabilityRequest
import com.pet.exception.ResourceNotFoundException
import com.pet.repository.SitterAvailabilityRepository
import com.pet.repository.SitterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class SitterAvailabilityService(
    private val availabilityRepository: SitterAvailabilityRepository,
    private val sitterRepository: SitterRepository
) {

    @Transactional(readOnly = true)
    fun getAvailabilityBySitter(sitterId: UUID): List<SitterAvailability> {
        // Verify sitter exists
        if (!sitterRepository.existsById(sitterId)) {
            throw ResourceNotFoundException("保母", "id", sitterId)
        }
        return availabilityRepository.findBySitterIdAndIsActiveTrue(sitterId)
    }

    fun addAvailability(sitterId: UUID, request: SitterAvailabilityRequest): SitterAvailability {
        val sitter = sitterRepository.findById(sitterId)
            .orElseThrow { ResourceNotFoundException("保母", "id", sitterId) }

        val availability = SitterAvailability()
        availability.setSitter(sitter)
        availability.setDayOfWeek(request.dayOfWeek)
        availability.setStartTime(request.startTime)
        availability.setEndTime(request.endTime)
        availability.setServiceArea(request.serviceArea)
        availability.setIsActive(request.isActive)

        return availabilityRepository.save(availability)
    }

    fun updateAvailability(sitterId: UUID, id: UUID, request: SitterAvailabilityRequest): SitterAvailability {
        val availability = availabilityRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("可用時段", "id", id) }

        if (availability.getSitter().getId() != sitterId) {
            throw IllegalArgumentException("此時段不屬於該保母")
        }

        availability.setDayOfWeek(request.dayOfWeek)
        availability.setStartTime(request.startTime)
        availability.setEndTime(request.endTime)
        availability.setServiceArea(request.serviceArea)
        availability.setIsActive(request.isActive)

        return availabilityRepository.save(availability)
    }

    fun deleteAvailability(sitterId: UUID, id: UUID) {
        val availability = availabilityRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("可用時段", "id", id) }

        if (availability.getSitter().getId() != sitterId) {
            throw IllegalArgumentException("此時段不屬於該保母")
        }

        availabilityRepository.delete(availability)
    }
}
