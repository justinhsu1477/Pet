package com.pet.web;

import com.pet.dto.SitterRatingDto;
import com.pet.dto.SitterRatingStatsDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.SitterRatingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 保母評價 API
 *
 * 面試亮點：
 * - 防濫用設計（只有完成訂單才能評價）
 * - 分頁查詢
 * - 統計彙整 API
 */
@RestController
@RequestMapping("/api/ratings")
public class SitterRatingController {

    private final SitterRatingService ratingService;

    public SitterRatingController(SitterRatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * 建立評價
     * POST /api/ratings?userId={userId}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SitterRatingDto>> createRating(
            @Valid @RequestBody SitterRatingDto ratingDto,
            @RequestParam UUID userId) {
        SitterRatingDto created = ratingService.createRating(ratingDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("評價提交成功", created));
    }

    /**
     * 取得評價詳情
     * GET /api/ratings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SitterRatingDto>> getRating(@PathVariable UUID id) {
        SitterRatingDto rating = ratingService.getRatingById(id);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    /**
     * 取得預約的評價
     * GET /api/ratings/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<SitterRatingDto>> getRatingByBooking(@PathVariable UUID bookingId) {
        SitterRatingDto rating = ratingService.getRatingByBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    /**
     * 保母回覆評價
     * POST /api/ratings/{id}/reply?sitterId={sitterId}
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<SitterRatingDto>> replyToRating(
            @PathVariable UUID id,
            @RequestBody String reply,
            @RequestParam UUID sitterId) {
        SitterRatingDto updated = ratingService.replyToRating(id, reply, sitterId);
        return ResponseEntity.ok(ApiResponse.success("回覆成功", updated));
    }

    /**
     * 取得保母的所有評價（分頁）
     * GET /api/ratings/sitter/{sitterId}?page=0&size=10
     */
    @GetMapping("/sitter/{sitterId}")
    public ResponseEntity<ApiResponse<Page<SitterRatingDto>>> getSitterRatings(
            @PathVariable UUID sitterId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<SitterRatingDto> ratings = ratingService.getSitterRatings(sitterId, pageable);
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }

    /**
     * 取得保母的評價統計
     * GET /api/ratings/sitter/{sitterId}/stats
     */
    @GetMapping("/sitter/{sitterId}/stats")
    public ResponseEntity<ApiResponse<SitterRatingStatsDto>> getSitterRatingStats(
            @PathVariable UUID sitterId) {
        SitterRatingStatsDto stats = ratingService.getSitterRatingStats(sitterId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 取得使用者給出的所有評價
     * GET /api/ratings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SitterRatingDto>>> getUserRatings(@PathVariable UUID userId) {
        List<SitterRatingDto> ratings = ratingService.getUserRatings(userId);
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }
}
