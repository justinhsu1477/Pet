package com.pet.web;

import com.pet.dto.CreateSitterRecordDto;
import com.pet.dto.SitterRecordDto;
import com.pet.dto.UpdateSitterRecordDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.SitterRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class SitterRecordController {

    private final SitterRecordService sitterRecordService;

    public SitterRecordController(SitterRecordService sitterRecordService) {
        this.sitterRecordService = sitterRecordService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SitterRecordDto>>> getAllRecords() {
        List<SitterRecordDto> records = sitterRecordService.getAllRecords();
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SitterRecordDto>> getRecord(@PathVariable Long id) {
        SitterRecordDto record = sitterRecordService.getRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<ApiResponse<List<SitterRecordDto>>> getRecordsByPet(@PathVariable Long petId) {
        List<SitterRecordDto> records = sitterRecordService.getRecordsByPetId(petId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/sitter/{sitterId}")
    public ResponseEntity<ApiResponse<List<SitterRecordDto>>> getRecordsBySitter(@PathVariable Long sitterId) {
        List<SitterRecordDto> records = sitterRecordService.getRecordsBySitterId(sitterId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SitterRecordDto>> createRecord(
            @Valid @RequestBody CreateSitterRecordDto createDto) {
        SitterRecordDto createdRecord = sitterRecordService.createRecord(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("記錄創建成功", createdRecord));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SitterRecordDto>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSitterRecordDto updateDto) {
        SitterRecordDto updatedRecord = sitterRecordService.updateRecord(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("記錄更新成功", updatedRecord));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        sitterRecordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success("記錄刪除成功", null));
    }
}
