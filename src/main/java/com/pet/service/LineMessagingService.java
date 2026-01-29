package com.pet.service;

import com.pet.config.LineMessagingConfig;
import com.pet.domain.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LineMessagingService {

    private static final String LINE_API_URL = "https://api.line.me/v2/bot/message/push";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private final LineMessagingConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 取得預約飼主的 LINE userId，若無則回退到 demo user
     */
    private String resolveRecipient(Booking booking) {
        if (booking.getUser() != null && booking.getUser().getLineUserId() != null
                && !booking.getUser().getLineUserId().isBlank()) {
            return booking.getUser().getLineUserId();
        }
        log.info("飼主無 LINE userId，使用 demo user 發送通知");
        return config.getDemoUserId();
    }

    /**
     * 發送預約確認通知
     */
    public void sendBookingConfirmedNotification(Booking booking) {
        StringBuilder message = new StringBuilder();
        message.append(String.format(
            "✅ 您的預約已確認！\n\n" +
            "🐾 寵物：%s\n" +
            "👤 保母：%s\n" +
            "📅 時間：%s ~ %s\n" +
            "💰 費用：$%.0f\n",
            booking.getPet().getName(),
            booking.getSitter().getName(),
            booking.getStartTime().format(DATE_FORMATTER),
            booking.getEndTime().format(DATE_FORMATTER),
            booking.getTotalPrice()
        ));

        // 如果有設定 baseUrl，加入行事曆連結
        if (config.hasBaseUrl()) {
            String calendarUrl = String.format("%s/api/bookings/%s/calendar",
                    config.getBaseUrl(), booking.getId());
            message.append("\n📅 加入行事曆：\n").append(calendarUrl).append("\n");
        }

        message.append("\n感謝您使用寵物保母系統！");
        sendNotification(resolveRecipient(booking), message.toString());
    }

    /**
     * 發送預約取消通知
     */
    public void sendBookingCancelledNotification(Booking booking, String reason) {
        String message = String.format(
            "⚠️ 預約已取消\n\n" +
            "🐾 寵物：%s\n" +
            "👤 保母：%s\n" +
            "📅 原訂時間：%s ~ %s\n" +
            (reason != null && !reason.isEmpty() ? "📝 原因：" + reason + "\n" : "") +
            "\n如有需要，歡迎重新預約！",
            booking.getPet().getName(),
            booking.getSitter().getName(),
            booking.getStartTime().format(DATE_FORMATTER),
            booking.getEndTime().format(DATE_FORMATTER)
        );
        sendNotification(resolveRecipient(booking), message);
    }

    /**
     * 發送預約被拒絕通知
     */
    public void sendBookingRejectedNotification(Booking booking, String reason) {
        String message = String.format(
            "❌ 您的預約被婉拒\n\n" +
            "🐾 寵物：%s\n" +
            "👤 保母：%s\n" +
            "📅 申請時間：%s ~ %s\n" +
            (reason != null && !reason.isEmpty() ? "📝 原因：" + reason + "\n" : "") +
            "\n建議您選擇其他保母或時段。",
            booking.getPet().getName(),
            booking.getSitter().getName(),
            booking.getStartTime().format(DATE_FORMATTER),
            booking.getEndTime().format(DATE_FORMATTER)
        );
        sendNotification(resolveRecipient(booking), message);
    }

    /**
     * 發送預約完成通知
     */
    public void sendBookingCompletedNotification(Booking booking) {
        String message = String.format(
            "🎉 服務已完成！\n\n" +
            "🐾 寵物：%s\n" +
            "👤 保母：%s\n" +
            "📅 服務時間：%s ~ %s\n" +
            "💰 費用：$%.0f\n\n" +
            "感謝您的使用，期待下次服務！",
            booking.getPet().getName(),
            booking.getSitter().getName(),
            booking.getStartTime().format(DATE_FORMATTER),
            booking.getEndTime().format(DATE_FORMATTER),
            booking.getTotalPrice()
        );
        sendNotification(resolveRecipient(booking), message);
    }

    /**
     * 發送預約過期通知
     */
    public void sendBookingExpiredNotification(Booking booking) {
        String message = String.format(
            "⏰ 您的預約已過期\n\n" +
            "🐾 寵物：%s\n" +
            "👤 保母：%s\n" +
            "📅 申請時間：%s ~ %s\n\n" +
            "由於保母超過24小時未回應，此預約已自動過期。\n" +
            "建議您選擇其他保母或重新預約。",
            booking.getPet().getName(),
            booking.getSitter().getName(),
            booking.getStartTime().format(DATE_FORMATTER),
            booking.getEndTime().format(DATE_FORMATTER)
        );
        sendNotification(resolveRecipient(booking), message);
    }

    /**
     * 發送通知到 LINE
     * 優先發給飼主的 LINE userId，若無則回退到 demo user
     */
    private void sendNotification(String recipientId, String message) {
        if (!config.isEnabled()) {
            log.info("LINE 通知已停用，跳過發送");
            return;
        }

        if (!config.isConfigured()) {
            log.warn("LINE 設定不完整，無法發送通知。請檢查 .env 檔案");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getChannelToken());

            Map<String, Object> body = new HashMap<>();
            body.put("to", recipientId);
            body.put("messages", List.of(Map.of("type", "text", "text", message)));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(LINE_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("LINE 通知發送成功");
            } else {
                log.error("LINE 通知發送失敗: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("LINE 通知發送異常: {}", e.getMessage());
        }
    }
}
