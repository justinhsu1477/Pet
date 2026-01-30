package com.pet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.dto.LineWebhookRequest;
import com.pet.service.LineRichMenuService;
import com.pet.service.LineWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * LINE Webhook Controller
 * 接收 LINE Platform 推送的事件（照片、文字等）
 *
 * 安全機制：
 * - 不需要 JWT（LINE 無法帶 JWT）
 * - 用 X-Line-Signature 做 HMAC-SHA256 簽名驗證
 */
@RestController
@RequestMapping("/api/line")
@RequiredArgsConstructor
@Slf4j
public class LineWebhookController {

    private final LineWebhookService lineWebhookService;
    private final LineRichMenuService lineRichMenuService;
    private final ObjectMapper objectMapper;

    /**
     * LINE Webhook 端點
     * LINE Platform 會將事件 POST 到此端點
     * 必須在 1 秒內回覆 200 OK，否則 LINE 會重試
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String body,
            @RequestHeader(value = "X-Line-Signature", required = false) String signature) {

        log.info("收到 LINE Webhook 請求");

        // 1. 驗證簽名
        if (signature == null || !lineWebhookService.verifySignature(body, signature)) {
            log.warn("LINE Webhook 簽名驗證失敗");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 2. 非同步處理事件（避免 LINE 超時）
        CompletableFuture.runAsync(() -> {
            try {
                LineWebhookRequest request = objectMapper.readValue(body, LineWebhookRequest.class);
                lineWebhookService.handleWebhookEvents(request.getEvents());
            } catch (Exception e) {
                log.error("處理 LINE Webhook 事件失敗: {}", e.getMessage(), e);
            }
        });

        // 3. 立即回覆 200 OK
        return ResponseEntity.ok().build();
    }

    /**
     * 重建所有 Rich Menu（URL 變更時使用）
     * 例如 ngrok 重啟後，需要更新 Rich Menu 內的連結
     *
     * 使用方式：POST /api/line/richmenu/recreate
     */
    @PostMapping("/richmenu/recreate")
    public ResponseEntity<String> recreateRichMenus() {
        try {
            lineRichMenuService.recreateAllMenus();
            return ResponseEntity.ok("Rich Menu 重建完成");
        } catch (Exception e) {
            log.error("Rich Menu 重建失敗: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Rich Menu 重建失敗: " + e.getMessage());
        }
    }
}
