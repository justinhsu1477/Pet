package com.pet.web;

import com.pet.dto.PetPhotoDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.FileStorageService;
import com.pet.service.PetPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * 寵物照片 REST API
 */
@RestController
@RequestMapping("/api/pet-photos")
@RequiredArgsConstructor
@Slf4j
public class PetPhotoController {

    private final PetPhotoService petPhotoService;
    private final FileStorageService fileStorageService;

    /**
     * 查詢寵物的照片
     */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<ApiResponse<List<PetPhotoDto>>> getPhotosByPet(@PathVariable UUID petId) {
        List<PetPhotoDto> photos = petPhotoService.getPhotosByPet(petId);
        return ResponseEntity.ok(ApiResponse.success(photos));
    }

    /**
     * 查詢保母上傳的照片
     */
    @GetMapping("/sitter/{sitterId}")
    public ResponseEntity<ApiResponse<List<PetPhotoDto>>> getPhotosBySitter(@PathVariable UUID sitterId) {
        List<PetPhotoDto> photos = petPhotoService.getPhotosBySitter(sitterId);
        return ResponseEntity.ok(ApiResponse.success(photos));
    }

    /**
     * 取得圖片檔案（靜態資源，不需 JWT）
     */
    @GetMapping("/image/**")
    public ResponseEntity<Resource> getImage(jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 取得 /api/pet-photos/image/ 之後的完整路徑
            String fullPath = request.getRequestURI();
            String relativePath = fullPath.substring("/api/pet-photos/image/".length());

            Path filePath = fileStorageService.loadImage(relativePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(resource);
        } catch (SecurityException e) {
            log.warn("非法的圖片存取路徑");
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("圖片讀取失敗: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 手動關聯照片到寵物
     */
    @PutMapping("/{photoId}/associate")
    public ResponseEntity<ApiResponse<PetPhotoDto>> associateWithPet(
            @PathVariable UUID photoId,
            @RequestParam UUID petId) {
        PetPhotoDto updated = petPhotoService.associatePhotoWithPet(photoId, petId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
