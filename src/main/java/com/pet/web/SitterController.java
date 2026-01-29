package com.pet.web;

import com.pet.dto.AvailableSitterDto;
import com.pet.dto.SitterDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.SitterService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sitters")
public class SitterController {

    private final SitterService sitterService;

    public SitterController(SitterService sitterService) {
        this.sitterService = sitterService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SitterDto>>> getAllSitters() {
        List<SitterDto> sitters = sitterService.getAllSitters();
        return ResponseEntity.ok(ApiResponse.success(sitters));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SitterDto>> getSitter(@PathVariable UUID id) {
        SitterDto sitter = sitterService.getSitterById(id);
        return ResponseEntity.ok(ApiResponse.success(sitter));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<SitterDto>> getSitterByUserId(@PathVariable UUID userId) {
        SitterDto sitter = sitterService.getSitterByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(sitter));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SitterDto>> createSitter(@Valid @RequestBody SitterDto sitterDto) {
        SitterDto createdSitter = sitterService.createSitter(sitterDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("保母創建成功", createdSitter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SitterDto>> updateSitter(
            @PathVariable UUID id,
            @Valid @RequestBody SitterDto sitterDto) {
        SitterDto updatedSitter = sitterService.updateSitter(id, sitterDto);
        return ResponseEntity.ok(ApiResponse.success("保母更新成功", updatedSitter));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSitter(@PathVariable UUID id) {
        sitterService.deleteSitter(id);
        return ResponseEntity.ok(ApiResponse.success("保母刪除成功", null));
    }

    /**
     * 取得指定日期可用的保母列表
     * GET /api/sitters/available?date=2026-01-22&startTime=2026-01-22T09:00&endTime=2026-01-22T17:00
     * startTime 和 endTime 為可選參數，如果提供則會排除在該時段已有預約的保母
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AvailableSitterDto>>> getAvailableSitters(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endTime) {
        List<AvailableSitterDto> sitters = sitterService.getAvailableSitters(date, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(sitters));
    }

    /**
     * 取得所有保母（含評分資訊）
     * GET /api/sitters/with-rating
     */
    @GetMapping("/with-rating")
    public ResponseEntity<ApiResponse<List<AvailableSitterDto>>> getAllSittersWithRating() {
        List<AvailableSitterDto> sitters = sitterService.getAllSittersWithRating();
        return ResponseEntity.ok(ApiResponse.success(sitters));
    }
}
