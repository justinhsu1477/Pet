package com.pet.scheduling

import com.pet.domain.Booking
import com.pet.domain.Booking.BookingStatus
import com.pet.domain.Dog
import com.pet.domain.Sitter
import com.pet.domain.Users
import com.pet.repository.BookingRepository
import com.pet.service.LineMessagingService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime
import java.util.*

class BookingExpirySchedulerTest {

    private lateinit var scheduler: BookingExpiryScheduler

    @Mock
    private lateinit var bookingRepository: BookingRepository

    @Mock
    private lateinit var lineMessagingService: LineMessagingService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        scheduler = BookingExpiryScheduler(bookingRepository, lineMessagingService)
    }

    private fun createTestBooking(): Booking {
        val booking = Booking()
        booking.id = UUID.randomUUID()
        booking.status = BookingStatus.PENDING
        booking.createdAt = LocalDateTime.now().minusHours(25)
        booking.startTime = LocalDateTime.of(2026, 2, 1, 10, 0)
        booking.endTime = LocalDateTime.of(2026, 2, 1, 14, 0)
        booking.totalPrice = 800.0

        val pet = Dog()
        pet.name = "小黑"
        booking.pet = pet

        val sitter = Sitter()
        sitter.name = "王保母"
        booking.sitter = sitter

        val user = Users()
        user.id = UUID.randomUUID()
        booking.user = user

        return booking
    }

    @Test
    fun `should expire pending bookings older than 24 hours`() {
        val booking = createTestBooking()
        `when`(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING), any()))
            .thenReturn(listOf(booking))

        scheduler.expireOverdueBookings()

        verify(bookingRepository).save(argThat<Booking> { it.status == BookingStatus.EXPIRED })
    }

    @Test
    fun `should not expire recent pending bookings`() {
        `when`(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING), any()))
            .thenReturn(emptyList())

        scheduler.expireOverdueBookings()

        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should send LINE notification for expired bookings`() {
        val booking = createTestBooking()
        `when`(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING), any()))
            .thenReturn(listOf(booking))

        scheduler.expireOverdueBookings()

        verify(lineMessagingService).sendBookingExpiredNotification(booking)
    }

    @Test
    fun `should continue processing other bookings when one fails`() {
        val booking1 = createTestBooking()
        val booking2 = createTestBooking()

        `when`(bookingRepository.findByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING), any()))
            .thenReturn(listOf(booking1, booking2))
        `when`(bookingRepository.save(booking1)).thenThrow(RuntimeException("DB error"))

        scheduler.expireOverdueBookings()

        // booking2 should still be processed
        verify(bookingRepository).save(booking2)
    }
}
