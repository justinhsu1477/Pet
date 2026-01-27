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
     * 發送預約確認通知
     */
    public void sendBookingConfirmedNotification(Booking booking) {
        String message = String.format(
            "✅ 您的預約已確認！\n\n" +
            "🐾 寵物：%s\n" +
            "👤 保母：%s\n" +
            "📅 時間：%s ~ %s\n" +
            "💰 費用：$%.0f\n\n" +
            "感謝您使用寵物保母系統！",
            booking.getPet().getName(),
            booking.getSitter().getName(),
            booking.getStartTime().format(DATE_FORMATTER),
            booking.getEndTime().format(DATE_FORMATTER),
            booking.getTotalPrice()
        );
        sendNotification(message);
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
        sendNotification(message);
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
        sendNotification(message);
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
        sendNotification(message);
    }

    /**
     * 發送通知到 LINE（Demo 模式：都發到設定的 user）
     */
    private void sendNotification(String message) {
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
            body.put("to", config.getDemoUserId());
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
