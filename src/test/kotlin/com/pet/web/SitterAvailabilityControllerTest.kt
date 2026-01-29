package com.pet.web

import com.pet.domain.SitterAvailability
import com.pet.dto.request.SitterAvailabilityRequest
import com.pet.service.SitterAvailabilityService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

class SitterAvailabilityControllerTest {

    private lateinit var controller: SitterAvailabilityController

    @Mock
    private lateinit var sitterAvailabilityService: SitterAvailabilityService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        controller = SitterAvailabilityController(sitterAvailabilityService)
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

    @Test
    fun `GET availability should return 200 with list`() {
        val slots = listOf(SitterAvailability(), SitterAvailability())
        `when`(sitterAvailabilityService.getAvailabilityBySitter(sitterId)).thenReturn(slots)

        val response = controller.getAvailability(sitterId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.success())
        assertEquals(2, response.body!!.data().size)
        verify(sitterAvailabilityService).getAvailabilityBySitter(sitterId)
    }

    @Test
    fun `POST availability should return 201 with created slot`() {
        val request = createRequest()
        val saved = SitterAvailability()
        `when`(sitterAvailabilityService.addAvailability(sitterId, request)).thenReturn(saved)

        val response = controller.addAvailability(sitterId, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.body!!.success())
        assertEquals(saved, response.body!!.data())
        verify(sitterAvailabilityService).addAvailability(sitterId, request)
    }

    @Test
    fun `PUT availability should return 200 with updated slot`() {
        val request = createRequest()
        val updated = SitterAvailability()
        `when`(sitterAvailabilityService.updateAvailability(sitterId, availabilityId, request)).thenReturn(updated)

        val response = controller.updateAvailability(sitterId, availabilityId, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.success())
        assertEquals(updated, response.body!!.data())
        verify(sitterAvailabilityService).updateAvailability(sitterId, availabilityId, request)
    }

    @Test
    fun `DELETE availability should return 200 with null data`() {
        doNothing().`when`(sitterAvailabilityService).deleteAvailability(sitterId, availabilityId)

        val response = controller.deleteAvailability(sitterId, availabilityId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.success())
        assertNull(response.body!!.data())
        verify(sitterAvailabilityService).deleteAvailability(sitterId, availabilityId)
    }
}
