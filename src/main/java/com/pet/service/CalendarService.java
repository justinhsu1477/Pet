package com.pet.service;

import com.pet.domain.Booking;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 行事曆服務
 * 產生 iCalendar (.ics) 格式的行事曆事件
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

    private final BookingRepository bookingRepository;

    /**
     * 產生預約的 iCalendar (.ics) 內容
     *
     * @param bookingId 預約 ID
     * @return .ics 檔案內容
     */
    public String generateBookingCalendar(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("預約", "id", bookingId));

        return generateIcsContent(booking);
    }

    /**
     * 產生 iCalendar 內容
     * 格式遵循 RFC 5545 標準
     */
    private String generateIcsContent(Booking booking) {
        String petName = booking.getPet().getName();
        String sitterName = booking.getSitter().getName();
        String startTime = booking.getStartTime().format(ICS_DATE_FORMAT);
        String endTime = booking.getEndTime().format(ICS_DATE_FORMAT);
        String uid = "booking-" + booking.getId() + "@petcare.com";
        String createdAt = booking.getCreatedAt() != null
                ? booking.getCreatedAt().format(ICS_DATE_FORMAT)
                : startTime;

        // 事件摘要
        String summary = String.format("寵物保母預約 - %s", petName);

        // 事件描述
        StringBuilder description = new StringBuilder();
        description.append("寵物：").append(petName).append("\\n");
        description.append("保母：").append(sitterName).append("\\n");
        description.append("費用：$").append(String.format("%.0f", booking.getTotalPrice())).append("\\n");
        description.append("狀態：").append(getStatusText(booking.getStatus())).append("\\n");
        if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
            description.append("備註：").append(booking.getNotes().replace("\n", "\\n"));
        }

        // 組裝 iCalendar 內容
        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//Pet Care System//Booking Calendar//TW\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-TIMEZONE:Asia/Taipei\r\n");

        // 時區定義
        ics.append("BEGIN:VTIMEZONE\r\n");
        ics.append("TZID:Asia/Taipei\r\n");
        ics.append("X-LIC-LOCATION:Asia/Taipei\r\n");
        ics.append("BEGIN:STANDARD\r\n");
        ics.append("TZOFFSETFROM:+0800\r\n");
        ics.append("TZOFFSETTO:+0800\r\n");
        ics.append("TZNAME:CST\r\n");
        ics.append("DTSTART:19700101T000000\r\n");
        ics.append("END:STANDARD\r\n");
        ics.append("END:VTIMEZONE\r\n");

        // 事件
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(uid).append("\r\n");
        ics.append("DTSTAMP:").append(createdAt).append("\r\n");
        ics.append("DTSTART;TZID=Asia/Taipei:").append(startTime).append("\r\n");
        ics.append("DTEND;TZID=Asia/Taipei:").append(endTime).append("\r\n");
        ics.append("SUMMARY:").append(summary).append("\r\n");
        ics.append("DESCRIPTION:").append(description).append("\r\n");
        ics.append("STATUS:").append(getIcsStatus(booking.getStatus())).append("\r\n");

        // 提醒（預約前 1 小時）
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT1H\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:寵物保母預約提醒：").append(petName).append(" 的保母服務即將開始\r\n");
        ics.append("END:VALARM\r\n");

        // 提醒（預約前 1 天）
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-P1D\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:明天有寵物保母預約：").append(petName).append("\r\n");
        ics.append("END:VALARM\r\n");

        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        return ics.toString();
    }

    /**
     * 取得狀態的中文顯示文字
     */
    private String getStatusText(Booking.BookingStatus status) {
        return switch (status) {
            case PENDING -> "待確認";
            case CONFIRMED -> "已確認";
            case CANCELLED -> "已取消";
            case REJECTED -> "已拒絕";
            case COMPLETED -> "已完成";
        };
    }

    /**
     * 取得 iCalendar 標準的狀態值
     */
    private String getIcsStatus(Booking.BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "CONFIRMED";
            case CANCELLED, REJECTED -> "CANCELLED";
            default -> "TENTATIVE";
        };
    }
}
