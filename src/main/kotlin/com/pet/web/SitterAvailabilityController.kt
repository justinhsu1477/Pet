package com.pet.web

import com.pet.domain.SitterAvailability
import com.pet.dto.request.SitterAvailabilityRequest
import com.pet.dto.response.ApiResponse
import com.pet.service.SitterAvailabilityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/sitter")
class SitterAvailabilityController(
    private val sitterAvailabilityService: SitterAvailabilityService
) {

    /**
     * 取得保母的所有可用時段
     * GET /api/sitter/{sitterId}/availability
     */
    @GetMapping("/{sitterId}/availability")
    fun getAvailability(@PathVariable sitterId: UUID): ResponseEntity<ApiResponse<List<SitterAvailability>>> {
        val slots = sitterAvailabilityService.getAvailabilityBySitter(sitterId)
        return ResponseEntity.ok(ApiResponse.success(slots))
    }

    /**
     * 新增可用時段
     * POST /api/sitter/{sitterId}/availability
     */
    @PostMapping("/{sitterId}/availability")
    fun addAvailability(
        @PathVariable sitterId: UUID,
        @RequestBody request: SitterAvailabilityRequest
    ): ResponseEntity<ApiResponse<SitterAvailability>> {
        val slot = sitterAvailabilityService.addAvailability(sitterId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("可用時段新增成功", slot))
    }

    /**
     * 更新可用時段
     * PUT /api/sitter/{sitterId}/availability/{id}
     */
    @PutMapping("/{sitterId}/availability/{id}")
    fun updateAvailability(
        @PathVariable sitterId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: SitterAvailabilityRequest
    ): ResponseEntity<ApiResponse<SitterAvailability>> {
        val slot = sitterAvailabilityService.updateAvailability(sitterId, id, request)
        return ResponseEntity.ok(ApiResponse.success("可用時段更新成功", slot))
    }

    /**
     * 刪除可用時段
     * DELETE /api/sitter/{sitterId}/availability/{id}
     */
    @DeleteMapping("/{sitterId}/availability/{id}")
    fun deleteAvailability(
        @PathVariable sitterId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        sitterAvailabilityService.deleteAvailability(sitterId, id)
        return ResponseEntity.ok(ApiResponse.success("可用時段刪除成功", null))
    }
}
