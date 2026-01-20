package com.pet.web;

import com.pet.dto.PetDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 寵物控制器 - 提供多態查詢所有寵物
 * 新增/更新請使用 /api/cats 或 /api/dogs
 */
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    /**
     * 取得所有寵物（包含貓和狗）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PetDto>>> getAllPets() {
        List<PetDto> pets = petService.getAllPets();
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    /**
     * 根據 ID 取得寵物
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetDto>> getPet(@PathVariable UUID id) {
        PetDto pet = petService.getPetById(id);
        return ResponseEntity.ok(ApiResponse.success(pet));
    }

    /**
     * 根據類型取得寵物 (CAT/DOG)
     */
    @GetMapping("/type/{petType}")
    public ResponseEntity<ApiResponse<List<PetDto>>> getPetsByType(@PathVariable String petType) {
        List<PetDto> pets = petService.getPetsByType(petType);
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    /**
     * 刪除寵物（通用刪除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePet(@PathVariable UUID id) {
        petService.deletePet(id);
        return ResponseEntity.ok(ApiResponse.success("寵物刪除成功", null));
    }
}
