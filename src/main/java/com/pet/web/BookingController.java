package com.pet.web;

import com.pet.dto.BookingDto;
import com.pet.dto.BookingStatusUpdateDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.BookingService;
import com.pet.service.CalendarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 預約管理 API
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final CalendarService calendarService;

    public BookingController(BookingService bookingService, CalendarService calendarService) {
        this.bookingService = bookingService;
        this.calendarService = calendarService;
    }

    /**
     * 取得所有預約（管理員用）
     * GET /api/bookings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingDto>>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * 建立預約
     * POST /api/bookings?userId={userId}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingDto>> createBooking(
            @Valid @RequestBody BookingDto bookingDto,
            @RequestParam UUID userId) {
        BookingDto created = bookingService.createBooking(bookingDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("預約建立成功，等待保母確認", created));
    }

    /**
     * 取得預約詳情
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDto>> getBooking(@PathVariable UUID id) {
        BookingDto booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * 下載預約行事曆 (.ics 檔案)
     * GET /api/bookings/{id}/calendar
     *
     * 用途：
     * - 讓用戶將預約加入 iPhone/Google/Outlook 行事曆
     * - LINE 通知中附帶此連結
     *
     * 回傳：
     * - Content-Type: text/calendar
     * - Content-Disposition: attachment; filename="booking-{id}.ics"
     */
    @GetMapping("/{id}/calendar")
    public ResponseEntity<String> calendarPage(@PathVariable UUID id) {
        // 驗證預約存在
        calendarService.generateBookingCalendar(id);

        String downloadUrl = "/api/bookings/" + id + "/calendar/download";
        String html = """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>加入行事曆 - 寵物保母預約</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { font-family: -apple-system, BlinkMacSystemFont, sans-serif;
                               background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                               min-height: 100vh; display: flex; align-items: center; justify-content: center; }
                        .card { background: white; border-radius: 20px; padding: 40px 30px;
                                max-width: 360px; width: 90%%; text-align: center;
                                box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
                        .icon { font-size: 64px; margin-bottom: 16px; }
                        h1 { font-size: 20px; color: #333; margin-bottom: 8px; }
                        p { font-size: 14px; color: #666; margin-bottom: 24px; line-height: 1.6; }
                        .btn { display: inline-block; background: #4CAF50; color: white;
                               padding: 14px 32px; border-radius: 12px; text-decoration: none;
                               font-size: 16px; font-weight: 600; width: 100%%;
                               transition: background 0.2s; }
                        .btn:active { background: #388E3C; }
                        .hint { font-size: 12px; color: #999; margin-top: 16px; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="icon">📅</div>
                        <h1>寵物保母預約</h1>
                        <p>點擊下方按鈕將預約加入您的行事曆</p>
                        <a class="btn" href="%s">加入行事曆</a>
                        <p class="hint">支援 iPhone 行事曆 / Google 日曆 / Outlook</p>
                    </div>
                </body>
                </html>
                """.formatted(downloadUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/{id}/calendar/download")
    public ResponseEntity<byte[]> downloadCalendar(@PathVariable UUID id) {
        String icsContent = calendarService.generateBookingCalendar(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", "booking-" + id + ".ics");
        headers.set("Content-Description", "Pet Care Booking Calendar");

        return ResponseEntity.ok()
                .headers(headers)
                .body(icsContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 更新預約狀態（使用樂觀鎖）
     * PUT /api/bookings/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookingDto>> updateBookingStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BookingStatusUpdateDto updateDto) {
        BookingDto updated = bookingService.updateBookingStatus(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("預約狀態已更新（樂觀鎖）", updated));
    }

    /**
     * 更新預約狀態（使用悲觀鎖）
     * PUT /api/bookings/{id}/status/pessimistic
     * 使用場景：
     * - 高併發環境下的訂單確認
     * - 需要強一致性保證的業務場景
     * 與樂觀鎖的差異：
     * - 樂觀鎖 (/api/bookings/{id}/status): 適合低衝突場景，失敗時需要重試
     * - 悲觀鎖 (/api/bookings/{id}/status/pessimistic): 適合高衝突場景，直接阻塞等待
     */
    @PutMapping("/{id}/status/pessimistic")
    public ResponseEntity<ApiResponse<BookingDto>> updateBookingStatusWithPessimisticLock(
            @PathVariable UUID id,
            @Valid @RequestBody BookingStatusUpdateDto updateDto) {
        BookingDto updated = bookingService.updateBookingStatusWithPessimisticLock(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("預約狀態已更新（悲觀鎖）", updated));
    }

    /**
     * 保母確認預約
     * POST /api/bookings/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingDto>> confirmBooking(
            @PathVariable UUID id,
            @RequestBody(required = false) String response) {
        BookingDto confirmed = bookingService.confirmBooking(id, response);
        return ResponseEntity.ok(ApiResponse.success("預約已確認", confirmed));
    }

    /**
     * 保母拒絕預約
     * POST /api/bookings/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<BookingDto>> rejectBooking(
            @PathVariable UUID id,
            @RequestBody(required = false) String reason) {
        BookingDto rejected = bookingService.rejectBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.success("預約已拒絕", rejected));
    }

    /**
     * 取消預約
     * POST /api/bookings/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingDto>> cancelBooking(
            @PathVariable UUID id,
            @RequestBody(required = false) String reason) {
        BookingDto cancelled = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.success("預約已取消", cancelled));
    }

    /**
     * 完成預約
     * POST /api/bookings/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<BookingDto>> completeBooking(@PathVariable UUID id) {
        BookingDto completed = bookingService.completeBooking(id);
        return ResponseEntity.ok(ApiResponse.success("預約已完成", completed));
    }

    /**
     * 取得使用者的所有預約
     * GET /api/bookings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getBookingsByUser(@PathVariable UUID userId) {
        List<BookingDto> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * 取得保母的所有預約
     * GET /api/bookings/sitter/{sitterId}
     */
    @GetMapping("/sitter/{sitterId}")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getBookingsBySitter(@PathVariable UUID sitterId) {
        List<BookingDto> bookings = bookingService.getBookingsBySitter(sitterId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * 取得保母待處理的預約
     * GET /api/bookings/sitter/{sitterId}/pending
     */
    @GetMapping("/sitter/{sitterId}/pending")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getPendingBookingsForSitter(
            @PathVariable UUID sitterId) {
        List<BookingDto> bookings = bookingService.getPendingBookingsForSitter(sitterId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * 取得寵物的預約歷史
     * GET /api/bookings/pet/{petId}
     */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getBookingsByPet(@PathVariable UUID petId) {
        List<BookingDto> bookings = bookingService.getBookingsByPet(petId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
}
