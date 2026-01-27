package com.pet.controller;

import com.pet.service.BookingService;
import com.pet.service.CalendarService;
import com.pet.exception.ResourceNotFoundException;
import com.pet.web.BookingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingController 行事曆端點測試")
class BookingControllerCalendarTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private CalendarService calendarService;

    private BookingController bookingController;

    private UUID testBookingId;
    private static final String SAMPLE_ICS = """
            BEGIN:VCALENDAR\r
            VERSION:2.0\r
            BEGIN:VEVENT\r
            SUMMARY:寵物保母預約 - 喵喵\r
            END:VEVENT\r
            END:VCALENDAR\r
            """;

    @BeforeEach
    void setUp() {
        bookingController = new BookingController(bookingService, calendarService);
        testBookingId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /{id}/calendar - 行事曆頁面")
    class CalendarPageTests {

        @Test
        @DisplayName("應該回傳 HTML 頁面")
        void shouldReturnHtmlPage() {
            // given
            given(calendarService.generateBookingCalendar(testBookingId)).willReturn(SAMPLE_ICS);

            // when
            ResponseEntity<String> response = bookingController.calendarPage(testBookingId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_HTML);
        }

        @Test
        @DisplayName("HTML 頁面應包含下載連結")
        void shouldContainDownloadLink() {
            // given
            given(calendarService.generateBookingCalendar(testBookingId)).willReturn(SAMPLE_ICS);

            // when
            ResponseEntity<String> response = bookingController.calendarPage(testBookingId);

            // then
            String body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).contains("/api/bookings/" + testBookingId + "/calendar/download");
            assertThat(body).contains("加入行事曆");
        }

        @Test
        @DisplayName("HTML 頁面應包含完整結構")
        void shouldContainCompleteHtmlStructure() {
            // given
            given(calendarService.generateBookingCalendar(testBookingId)).willReturn(SAMPLE_ICS);

            // when
            ResponseEntity<String> response = bookingController.calendarPage(testBookingId);

            // then
            String body = response.getBody();
            assertThat(body).contains("<!DOCTYPE html>");
            assertThat(body).contains("寵物保母預約");
            assertThat(body).contains("📅");
        }

        @Test
        @DisplayName("預約不存在時應拋出例外")
        void shouldThrowWhenBookingNotFound() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            given(calendarService.generateBookingCalendar(nonExistentId))
                    .willThrow(new ResourceNotFoundException("預約", "id", nonExistentId));

            // when & then
            assertThatThrownBy(() -> bookingController.calendarPage(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("GET /{id}/calendar/download - 下載 .ics 檔案")
    class CalendarDownloadTests {

        @Test
        @DisplayName("應該回傳 200 狀態碼")
        void shouldReturn200() {
            // given
            given(calendarService.generateBookingCalendar(testBookingId)).willReturn(SAMPLE_ICS);

            // when
            ResponseEntity<byte[]> response = bookingController.downloadCalendar(testBookingId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("應該回傳 text/calendar Content-Type")
        void shouldReturnCalendarContentType() {
            // given
            given(calendarService.generateBookingCalendar(testBookingId)).willReturn(SAMPLE_ICS);

            // when
            ResponseEntity<byte[]> response = bookingController.downloadCalendar(testBookingId);

            // then
            assertThat(response.getHeaders().getContentType().toString())
                    .contains("text/calendar");
        }

        @Test
        @DisplayName("應該包含 Content-Disposition attachment header")
        void shouldContainContentDisposition() {
            // given
            given(calendarService.generateBookingCalendar(testBookingId)).willReturn(SAMPLE_ICS);

            // when
            ResponseEntity<byte[]> response = bookingController.downloadCalendar(testBookingId);

            // then
            String disposition = response.getHeaders().getContentDisposition().toString();
            assertThat(disposition).contains("booking-" + testBookingId + ".ics");
        }

        @Test
        @DisplayName("應該回傳正確的 .ics 內容")
        void shouldReturnCorrectIcsContent() {
            // given
            given(calendarService.generateBookingCalendar(testBookingId)).willReturn(SAMPLE_ICS);

            // when
            ResponseEntity<byte[]> response = bookingController.downloadCalendar(testBookingId);

            // then
            assertThat(response.getBody()).isNotNull();
            String content = new String(response.getBody());
            assertThat(content).contains("BEGIN:VCALENDAR");
            assertThat(content).contains("寵物保母預約 - 喵喵");
        }

        @Test
        @DisplayName("預約不存在時應拋出例外")
        void shouldThrowWhenBookingNotFound() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            given(calendarService.generateBookingCalendar(nonExistentId))
                    .willThrow(new ResourceNotFoundException("預約", "id", nonExistentId));

            // when & then
            assertThatThrownBy(() -> bookingController.downloadCalendar(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
