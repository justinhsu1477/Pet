package com.pet.service;

import com.pet.config.LineMessagingConfig;
import com.pet.domain.Booking;
import com.pet.domain.Dog;
import com.pet.domain.Pet;
import com.pet.domain.Sitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LineMessagingService 單元測試")
class LineMessagingServiceTest {

    @Mock
    private LineMessagingConfig config;

    @Mock
    private RestTemplate restTemplate;

    private LineMessagingService lineMessagingService;

    private Booking testBooking;
    private Pet testPet;
    private Sitter testSitter;

    @BeforeEach
    void setUp() {
        lineMessagingService = new LineMessagingService(config);
        // 使用反射注入 mock RestTemplate
        ReflectionTestUtils.setField(lineMessagingService, "restTemplate", restTemplate);

        // 設定測試資料
        testPet = new Dog();
        testPet.setId(UUID.randomUUID());
        testPet.setName("小黑");

        testSitter = new Sitter();
        testSitter.setId(UUID.randomUUID());
        testSitter.setName("王保母");

        testBooking = new Booking();
        testBooking.setId(UUID.randomUUID());
        testBooking.setPet(testPet);
        testBooking.setSitter(testSitter);
        testBooking.setStartTime(LocalDateTime.of(2026, 2, 1, 10, 0));
        testBooking.setEndTime(LocalDateTime.of(2026, 2, 1, 14, 0));
        testBooking.setTotalPrice(800.0);
    }

    @Nested
    @DisplayName("設定檢查測試")
    class ConfigurationTests {

        @Test
        @DisplayName("當 LINE 通知停用時應該跳過發送")
        void shouldSkipWhenDisabled() {
            // given
            given(config.isEnabled()).willReturn(false);

            // when
            lineMessagingService.sendBookingConfirmedNotification(testBooking);

            // then
            verify(restTemplate, never()).postForEntity(anyString(), any(), any());
        }

        @Test
        @DisplayName("當設定不完整時應該跳過發送")
        void shouldSkipWhenNotConfigured() {
            // given
            given(config.isEnabled()).willReturn(true);
            given(config.isConfigured()).willReturn(false);

            // when
            lineMessagingService.sendBookingConfirmedNotification(testBooking);

            // then
            verify(restTemplate, never()).postForEntity(anyString(), any(), any());
        }
    }

    @Nested
    @DisplayName("預約確認通知測試")
    class ConfirmedNotificationTests {

        @BeforeEach
        void setUpConfig() {
            given(config.isEnabled()).willReturn(true);
            given(config.isConfigured()).willReturn(true);
            given(config.getChannelToken()).willReturn("test-token");
            given(config.getDemoUserId()).willReturn("U123456789");
        }

        @Test
        @DisplayName("應該成功發送預約確認通知")
        void shouldSendConfirmedNotification() {
            // given
            ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willReturn(successResponse);

            // when
            lineMessagingService.sendBookingConfirmedNotification(testBooking);

            // then
            ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForEntity(
                    eq("https://api.line.me/v2/bot/message/push"),
                    captor.capture(),
                    eq(String.class)
            );

            HttpEntity<Map<String, Object>> request = captor.getValue();
            assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThat(request.getHeaders().getFirst("Authorization")).isEqualTo("Bearer test-token");

            Map<String, Object> body = request.getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("to")).isEqualTo("U123456789");
        }

        @Test
        @DisplayName("通知內容應該包含正確的預約資訊")
        void shouldContainCorrectBookingInfo() {
            // given
            ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willReturn(successResponse);

            // when
            lineMessagingService.sendBookingConfirmedNotification(testBooking);

            // then
            ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));

            Map<String, Object> body = captor.getValue().getBody();
            @SuppressWarnings("unchecked")
            var messages = (java.util.List<Map<String, String>>) body.get("messages");
            String messageText = messages.get(0).get("text");

            assertThat(messageText).contains("小黑");
            assertThat(messageText).contains("王保母");
            assertThat(messageText).contains("$800");
            assertThat(messageText).contains("已確認");
        }
    }

    @Nested
    @DisplayName("預約取消通知測試")
    class CancelledNotificationTests {

        @BeforeEach
        void setUpConfig() {
            given(config.isEnabled()).willReturn(true);
            given(config.isConfigured()).willReturn(true);
            given(config.getChannelToken()).willReturn("test-token");
            given(config.getDemoUserId()).willReturn("U123456789");
        }

        @Test
        @DisplayName("應該成功發送預約取消通知（含原因）")
        void shouldSendCancelledNotificationWithReason() {
            // given
            ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willReturn(successResponse);

            // when
            lineMessagingService.sendBookingCancelledNotification(testBooking, "臨時有事");

            // then
            ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));

            Map<String, Object> body = captor.getValue().getBody();
            @SuppressWarnings("unchecked")
            var messages = (java.util.List<Map<String, String>>) body.get("messages");
            String messageText = messages.get(0).get("text");

            assertThat(messageText).contains("取消");
            assertThat(messageText).contains("臨時有事");
        }

        @Test
        @DisplayName("應該成功發送預約取消通知（無原因）")
        void shouldSendCancelledNotificationWithoutReason() {
            // given
            ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willReturn(successResponse);

            // when
            lineMessagingService.sendBookingCancelledNotification(testBooking, null);

            // then
            verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
        }
    }

    @Nested
    @DisplayName("預約拒絕通知測試")
    class RejectedNotificationTests {

        @BeforeEach
        void setUpConfig() {
            given(config.isEnabled()).willReturn(true);
            given(config.isConfigured()).willReturn(true);
            given(config.getChannelToken()).willReturn("test-token");
            given(config.getDemoUserId()).willReturn("U123456789");
        }

        @Test
        @DisplayName("應該成功發送預約拒絕通知")
        void shouldSendRejectedNotification() {
            // given
            ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willReturn(successResponse);

            // when
            lineMessagingService.sendBookingRejectedNotification(testBooking, "時間不合");

            // then
            ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));

            Map<String, Object> body = captor.getValue().getBody();
            @SuppressWarnings("unchecked")
            var messages = (java.util.List<Map<String, String>>) body.get("messages");
            String messageText = messages.get(0).get("text");

            assertThat(messageText).contains("婉拒");
            assertThat(messageText).contains("時間不合");
        }
    }

    @Nested
    @DisplayName("預約完成通知測試")
    class CompletedNotificationTests {

        @BeforeEach
        void setUpConfig() {
            given(config.isEnabled()).willReturn(true);
            given(config.isConfigured()).willReturn(true);
            given(config.getChannelToken()).willReturn("test-token");
            given(config.getDemoUserId()).willReturn("U123456789");
        }

        @Test
        @DisplayName("應該成功發送預約完成通知")
        void shouldSendCompletedNotification() {
            // given
            ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willReturn(successResponse);

            // when
            lineMessagingService.sendBookingCompletedNotification(testBooking);

            // then
            ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));

            Map<String, Object> body = captor.getValue().getBody();
            @SuppressWarnings("unchecked")
            var messages = (java.util.List<Map<String, String>>) body.get("messages");
            String messageText = messages.get(0).get("text");

            assertThat(messageText).contains("完成");
            assertThat(messageText).contains("$800");
        }
    }

    @Nested
    @DisplayName("錯誤處理測試")
    class ErrorHandlingTests {

        @BeforeEach
        void setUpConfig() {
            given(config.isEnabled()).willReturn(true);
            given(config.isConfigured()).willReturn(true);
            given(config.getChannelToken()).willReturn("test-token");
            given(config.getDemoUserId()).willReturn("U123456789");
        }

        @Test
        @DisplayName("當 API 回傳非 2xx 狀態碼時應該記錄錯誤")
        void shouldLogErrorWhenApiReturnsNon2xx() {
            // given
            ResponseEntity<String> errorResponse = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willReturn(errorResponse);

            // when - 不應該拋出例外
            lineMessagingService.sendBookingConfirmedNotification(testBooking);

            // then
            verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
        }

        @Test
        @DisplayName("當發生網路異常時應該捕獲並記錄")
        void shouldCatchNetworkException() {
            // given
            given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                    .willThrow(new RestClientException("Connection refused"));

            // when - 不應該拋出例外
            lineMessagingService.sendBookingConfirmedNotification(testBooking);

            // then
            verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
        }
    }
}
