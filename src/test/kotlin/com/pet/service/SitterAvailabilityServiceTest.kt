package com.pet.service

import com.pet.domain.Sitter
import com.pet.domain.SitterAvailability
import com.pet.dto.request.SitterAvailabilityRequest
import com.pet.exception.ResourceNotFoundException
import com.pet.repository.SitterAvailabilityRepository
import com.pet.repository.SitterRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

class SitterAvailabilityServiceTest {

    private lateinit var service: SitterAvailabilityService

    @Mock
    private lateinit var availabilityRepository: SitterAvailabilityRepository

    @Mock
    private lateinit var sitterRepository: SitterRepository

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = SitterAvailabilityService(availabilityRepository, sitterRepository)
    }

    private val sitterId: UUID = UUID.randomUUID()
    private val availabilityId: UUID = UUID.randomUUID()

    private fun createRequest() = SitterAvailabilityRequest(
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        serviceArea = "Taipei",
        isActive = true
    )

    private fun createSitter(): Sitter {
        val sitter = Sitter()
        sitter.id = sitterId
        return sitter
    }

    private fun createAvailability(sitter: Sitter): SitterAvailability {
        val a = SitterAvailability()
        a.setSitter(sitter)
        a.setDayOfWeek(DayOfWeek.MONDAY)
        a.setStartTime(LocalTime.of(9, 0))
        a.setEndTime(LocalTime.of(17, 0))
        a.setServiceArea("Taipei")
        a.setIsActive(true)
        return a
    }

    // --- getAvailabilityBySitter ---

    @Test
    fun `getAvailabilityBySitter should return list when sitter exists`() {
        `when`(sitterRepository.existsById(sitterId)).thenReturn(true)
        val expected = listOf(SitterAvailability())
        `when`(availabilityRepository.findBySitterIdAndIsActiveTrue(sitterId)).thenReturn(expected)

        val result = service.getAvailabilityBySitter(sitterId)

        assertEquals(expected, result)
        verify(sitterRepository).existsById(sitterId)
        verify(availabilityRepository).findBySitterIdAndIsActiveTrue(sitterId)
    }

    @Test
    fun `getAvailabilityBySitter should throw when sitter not found`() {
        `when`(sitterRepository.existsById(sitterId)).thenReturn(false)

        assertThrows(ResourceNotFoundException::class.java) {
            service.getAvailabilityBySitter(sitterId)
        }

        verify(availabilityRepository, never()).findBySitterIdAndIsActiveTrue(any())
    }

    // --- addAvailability ---

    @Test
    fun `addAvailability should save and return availability`() {
        val sitter = createSitter()
        val request = createRequest()
        `when`(sitterRepository.findById(sitterId)).thenReturn(Optional.of(sitter))
        `when`(availabilityRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.addAvailability(sitterId, request)

        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek())
        assertEquals(LocalTime.of(9, 0), result.getStartTime())
        assertEquals(LocalTime.of(17, 0), result.getEndTime())
        assertEquals("Taipei", result.getServiceArea())
        assertEquals(true, result.getIsActive())
        verify(availabilityRepository).save(any())
    }

    // --- updateAvailability ---

    @Test
    fun `updateAvailability should update and return availability`() {
        val sitter = createSitter()
        val existing = createAvailability(sitter)
        val request = SitterAvailabilityRequest(
            dayOfWeek = DayOfWeek.FRIDAY,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(18, 0),
            serviceArea = "Kaohsiung",
            isActive = false
        )
        `when`(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(existing))
        `when`(availabilityRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.updateAvailability(sitterId, availabilityId, request)

        assertEquals(DayOfWeek.FRIDAY, result.getDayOfWeek())
        assertEquals("Kaohsiung", result.getServiceArea())
        assertEquals(false, result.getIsActive())
    }

    @Test
    fun `updateAvailability should throw when sitter id does not match`() {
        val otherSitter = Sitter()
        otherSitter.id = UUID.randomUUID()
        val existing = createAvailability(otherSitter)
        `when`(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(existing))

        assertThrows(IllegalArgumentException::class.java) {
            service.updateAvailability(sitterId, availabilityId, createRequest())
        }
    }

    // --- deleteAvailability ---

    @Test
    fun `deleteAvailability should delete when sitter matches`() {
        val sitter = createSitter()
        val existing = createAvailability(sitter)
        `when`(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(existing))

        service.deleteAvailability(sitterId, availabilityId)

        verify(availabilityRepository).delete(existing)
    }

    @Test
    fun `deleteAvailability should throw when sitter id does not match`() {
        val otherSitter = Sitter()
        otherSitter.id = UUID.randomUUID()
        val existing = createAvailability(otherSitter)
        `when`(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(existing))

        assertThrows(IllegalArgumentException::class.java) {
            service.deleteAvailability(sitterId, availabilityId)
        }

        verify(availabilityRepository, never()).delete(any())
    }
}
