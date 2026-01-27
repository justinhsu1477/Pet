package com.pet.service;

import com.pet.domain.Booking;
import com.pet.domain.Dog;
import com.pet.domain.Pet;
import com.pet.domain.Sitter;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarService 單元測試")
class CalendarServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CalendarService calendarService;

    private UUID testBookingId;
    private Booking testBooking;
    private Pet testPet;
    private Sitter testSitter;

    @BeforeEach
    void setUp() {
        testBookingId = UUID.randomUUID();

        testPet = new Dog();
        testPet.setName("喵喵");

        testSitter = new Sitter();
        testSitter.setName("張保母");

        testBooking = new Booking();
        testBooking.setId(testBookingId);
        testBooking.setPet(testPet);
        testBooking.setSitter(testSitter);
        testBooking.setStartTime(LocalDateTime.of(2026, 1, 30, 9, 0));
        testBooking.setEndTime(LocalDateTime.of(2026, 1, 30, 17, 0));
        testBooking.setTotalPrice(1628.0);
        testBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        testBooking.setNotes("測試備註");
        testBooking.setCreatedAt(LocalDateTime.of(2026, 1, 27, 10, 0));
    }

    @Nested
    @DisplayName("generateBookingCalendar - 產生行事曆")
    class GenerateBookingCalendarTests {

        @Test
        @DisplayName("應該產生有效的 iCalendar 內容")
        void shouldGenerateValidIcsContent() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).startsWith("BEGIN:VCALENDAR");
            assertThat(ics).endsWith("END:VCALENDAR\r\n");
            assertThat(ics).contains("VERSION:2.0");
            assertThat(ics).contains("METHOD:PUBLISH");
        }

        @Test
        @DisplayName("應該包含正確的時區定義")
        void shouldContainTimezoneDefinition() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("BEGIN:VTIMEZONE");
            assertThat(ics).contains("TZID:Asia/Taipei");
            assertThat(ics).contains("X-WR-TIMEZONE:Asia/Taipei");
            assertThat(ics).contains("TZOFFSETFROM:+0800");
            assertThat(ics).contains("TZOFFSETTO:+0800");
        }

        @Test
        @DisplayName("應該包含正確的事件資訊")
        void shouldContainCorrectEventInfo() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("BEGIN:VEVENT");
            assertThat(ics).contains("SUMMARY:寵物保母預約 - 喵喵");
            assertThat(ics).contains("DTSTART;TZID=Asia/Taipei:20260130T090000");
            assertThat(ics).contains("DTEND;TZID=Asia/Taipei:20260130T170000");
            assertThat(ics).contains("STATUS:CONFIRMED");
            assertThat(ics).contains("UID:booking-" + testBookingId + "@petcare.com");
        }

        @Test
        @DisplayName("應該包含寵物和保母資訊在描述中")
        void shouldContainDescriptionWithDetails() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("寵物：喵喵");
            assertThat(ics).contains("保母：張保母");
            assertThat(ics).contains("費用：$1628");
            assertThat(ics).contains("狀態：已確認");
        }

        @Test
        @DisplayName("應該包含備註資訊")
        void shouldContainNotes() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("備註：測試備註");
        }

        @Test
        @DisplayName("沒有備註時不應包含備註欄位")
        void shouldNotContainNotesWhenEmpty() {
            // given
            testBooking.setNotes(null);
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).doesNotContain("備註：");
        }

        @Test
        @DisplayName("應該包含兩個提醒")
        void shouldContainTwoAlarms() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            // 1 小時前提醒
            assertThat(ics).contains("TRIGGER:-PT1H");
            assertThat(ics).contains("寵物保母預約提醒：喵喵 的保母服務即將開始");
            // 1 天前提醒
            assertThat(ics).contains("TRIGGER:-P1D");
            assertThat(ics).contains("明天有寵物保母預約：喵喵");
        }

        @Test
        @DisplayName("應該使用 createdAt 作為 DTSTAMP")
        void shouldUseCreatedAtAsDtstamp() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("DTSTAMP:20260127T100000");
        }

        @Test
        @DisplayName("createdAt 為 null 時應使用 startTime 作為 DTSTAMP")
        void shouldUseStartTimeWhenCreatedAtIsNull() {
            // given
            testBooking.setCreatedAt(null);
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("DTSTAMP:20260130T090000");
        }
    }

    @Nested
    @DisplayName("狀態對應測試")
    class StatusMappingTests {

        @Test
        @DisplayName("PENDING 狀態應對應 TENTATIVE")
        void pendingShouldMapToTentative() {
            // given
            testBooking.setStatus(Booking.BookingStatus.PENDING);
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("STATUS:TENTATIVE");
            assertThat(ics).contains("狀態：待確認");
        }

        @Test
        @DisplayName("CANCELLED 狀態應對應 CANCELLED")
        void cancelledShouldMapToCancelled() {
            // given
            testBooking.setStatus(Booking.BookingStatus.CANCELLED);
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("STATUS:CANCELLED");
            assertThat(ics).contains("狀態：已取消");
        }

        @Test
        @DisplayName("REJECTED 狀態應對應 CANCELLED")
        void rejectedShouldMapToCancelled() {
            // given
            testBooking.setStatus(Booking.BookingStatus.REJECTED);
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("STATUS:CANCELLED");
            assertThat(ics).contains("狀態：已拒絕");
        }

        @Test
        @DisplayName("COMPLETED 狀態應對應 TENTATIVE")
        void completedShouldMapToTentative() {
            // given
            testBooking.setStatus(Booking.BookingStatus.COMPLETED);
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            String ics = calendarService.generateBookingCalendar(testBookingId);

            // then
            assertThat(ics).contains("STATUS:TENTATIVE");
            assertThat(ics).contains("狀態：已完成");
        }
    }

    @Nested
    @DisplayName("例外處理測試")
    class ExceptionTests {

        @Test
        @DisplayName("預約不存在時應拋出 ResourceNotFoundException")
        void shouldThrowWhenBookingNotFound() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            given(bookingRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> calendarService.generateBookingCalendar(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
