package com.pet.web;

import com.pet.dto.PetActivityDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.PetActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 寵物活動控制器 - 記錄散步/餵食等日常活動
 */
@RestController
@RequestMapping("/api/pets/{petId}/activities")
public class PetActivityController {

    private final PetActivityService petActivityService;

    public PetActivityController(PetActivityService petActivityService) {
        this.petActivityService = petActivityService;
    }

    /**
     * 取得今天的活動紀錄
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<PetActivityDto>> getTodayActivity(@PathVariable UUID petId) {
        PetActivityDto activity = petActivityService.getTodayActivity(petId);
        return ResponseEntity.ok(ApiResponse.success(activity));
    }

    /**
     * 記錄活動（新增或更新今天的紀錄）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PetActivityDto>> recordActivity(
            @PathVariable UUID petId,
            @Valid @RequestBody PetActivityDto dto) {
        PetActivityDto activity = petActivityService.recordActivity(petId, dto);
        return ResponseEntity.ok(ApiResponse.success("活動記錄成功", activity));
    }

    /**
     * 取得活動歷史紀錄
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PetActivityDto>>> getActivityHistory(@PathVariable UUID petId) {
        List<PetActivityDto> activities = petActivityService.getActivityHistory(petId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
