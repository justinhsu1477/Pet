package com.pet.controller;

import com.pet.dto.SitterDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.SitterService;
import com.pet.web.SitterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SitterController 測試")
class SitterControllerTest {

    @Mock
    private SitterService sitterService;

    private SitterController sitterController;

    @BeforeEach
    void setUp() {
        sitterController = new SitterController(sitterService);
    }

    @Test
    @DisplayName("getSitterByUserId 應該根據 userId 回傳保母資料")
    void shouldReturnSitterByUserId() {
        // given
        UUID userId = UUID.randomUUID();
        UUID sitterId = UUID.randomUUID();
        SitterDto sitterDto = new SitterDto(sitterId, "王保母", "5年經驗");
        given(sitterService.getSitterByUserId(userId)).willReturn(sitterDto);

        // when
        ResponseEntity<ApiResponse<SitterDto>> response = sitterController.getSitterByUserId(userId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(sitterId);
        assertThat(response.getBody().data().name()).isEqualTo("王保母");
    }

    @Test
    @DisplayName("getSitter 應該根據 id 回傳保母資料")
    void shouldReturnSitterById() {
        // given
        UUID sitterId = UUID.randomUUID();
        SitterDto sitterDto = new SitterDto(sitterId, "李保母", "3年經驗");
        given(sitterService.getSitterById(sitterId)).willReturn(sitterDto);

        // when
        ResponseEntity<ApiResponse<SitterDto>> response = sitterController.getSitter(sitterId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().name()).isEqualTo("李保母");
    }
}
