package com.pet.web;

import com.pet.dto.SitterDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.SitterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
