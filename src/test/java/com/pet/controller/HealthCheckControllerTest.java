package com.pet.controller;

import com.pet.dto.HealthCheckDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckController 測試")
class HealthCheckControllerTest {

    @Mock
    private DataSource dataSource;

    private HealthCheckController healthCheckController;

    @BeforeEach
    void setUp() {
        healthCheckController = new HealthCheckController(dataSource);
        ReflectionTestUtils.setField(healthCheckController, "appVersion", "1.0.0-test");
    }

    @Nested
    @DisplayName("Liveness 端點測試")
    class LivenessTests {

        @Test
        @DisplayName("應該回傳 UP 狀態")
        void shouldReturnUpStatus() {
            // when
            ResponseEntity<Map<String, String>> response = healthCheckController.liveness();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status")).isEqualTo("UP");
            assertThat(response.getBody().get("timestamp")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Readiness 端點測試")
    class ReadinessTests {

        @Test
        @DisplayName("當資料庫連線正常時應該回傳 UP")
        void shouldReturnUpWhenDatabaseIsHealthy() throws SQLException {
            // given
            Connection mockConnection = mock(Connection.class);
            given(dataSource.getConnection()).willReturn(mockConnection);
            given(mockConnection.isValid(5)).willReturn(true);

            // when
            ResponseEntity<HealthCheckDto> response = healthCheckController.readiness();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo("UP");
            assertThat(response.getBody().version()).isEqualTo("1.0.0-test");
            assertThat(response.getBody().components().get("database").status()).isEqualTo("UP");
            assertThat(response.getBody().components().get("memory").status()).isEqualTo("UP");
        }

        @Test
        @DisplayName("當資料庫連線失敗時應該回傳 DOWN 和 503 狀態碼")
        void shouldReturnDownWhenDatabaseConnectionFails() throws SQLException {
            // given
            given(dataSource.getConnection()).willThrow(new SQLException("Connection refused"));

            // when
            ResponseEntity<HealthCheckDto> response = healthCheckController.readiness();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo("DOWN");
            assertThat(response.getBody().components().get("database").status()).isEqualTo("DOWN");
            assertThat(response.getBody().components().get("database").message()).contains("Connection refused");
        }

        @Test
        @DisplayName("當資料庫連線驗證失敗時應該回傳 DOWN")
        void shouldReturnDownWhenDatabaseValidationFails() throws SQLException {
            // given
            Connection mockConnection = mock(Connection.class);
            given(dataSource.getConnection()).willReturn(mockConnection);
            given(mockConnection.isValid(5)).willReturn(false);

            // when
            ResponseEntity<HealthCheckDto> response = healthCheckController.readiness();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo("DOWN");
            assertThat(response.getBody().components().get("database").status()).isEqualTo("DOWN");
        }
    }

    @Nested
    @DisplayName("完整健康檢查端點測試")
    class FullHealthCheckTests {

        @Test
        @DisplayName("應該包含所有組件狀態")
        void shouldIncludeAllComponents() throws SQLException {
            // given
            Connection mockConnection = mock(Connection.class);
            given(dataSource.getConnection()).willReturn(mockConnection);
            given(mockConnection.isValid(5)).willReturn(true);

            // when
            ResponseEntity<HealthCheckDto> response = healthCheckController.fullHealthCheck();

            // then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().components()).containsKeys("database", "memory");
            assertThat(response.getBody().timestamp()).isNotNull();
        }

        @Test
        @DisplayName("記憶體檢查應該包含使用量資訊")
        void shouldIncludeMemoryUsageInfo() throws SQLException {
            // given
            Connection mockConnection = mock(Connection.class);
            given(dataSource.getConnection()).willReturn(mockConnection);
            given(mockConnection.isValid(5)).willReturn(true);

            // when
            ResponseEntity<HealthCheckDto> response = healthCheckController.fullHealthCheck();

            // then
            assertThat(response.getBody()).isNotNull();
            String memoryMessage = response.getBody().components().get("memory").message();
            assertThat(memoryMessage).contains("Memory usage");
            assertThat(memoryMessage).contains("MB");
        }
    }
}
