package com.pet.web;

import com.pet.domain.Dog;
import com.pet.dto.DogDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.DogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dogs")
public class DogController {

    private final DogService dogService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<DogDto>>> getAllDogs() {
        List<DogDto> dogs = dogService.getAll();
        return ResponseEntity.ok(ApiResponse.success(dogs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DogDto>> getDog(@PathVariable UUID id) {
        DogDto dog = dogService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(dog));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DogDto>> createDog(
            @Valid @RequestBody DogDto dogDto,
            @RequestParam UUID userId) {
        DogDto createdDog = dogService.create(dogDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("狗狗創建成功", createdDog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DogDto>> updateDog(
            @PathVariable UUID id,
            @Valid @RequestBody DogDto dogDto) {
        DogDto updatedDog = dogService.update(id, dogDto);
        return ResponseEntity.ok(ApiResponse.success("狗狗更新成功", updatedDog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDog(@PathVariable UUID id) {
        dogService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("狗狗刪除成功", null));
    }

    // ========== 狗特有的 API ==========

    @GetMapping("/size/{size}")
    public ResponseEntity<ApiResponse<List<DogDto>>> getBySize(@PathVariable Dog.Size size) {
        List<DogDto> dogs = dogService.getBySize(size);
        return ResponseEntity.ok(ApiResponse.success(dogs));
    }

    @GetMapping("/need-walk")
    public ResponseEntity<ApiResponse<List<DogDto>>> getDogsNeedingWalk() {
        List<DogDto> dogs = dogService.getDogsNeedingWalk();
        return ResponseEntity.ok(ApiResponse.success(dogs));
    }

    @GetMapping("/training-level/{level}")
    public ResponseEntity<ApiResponse<List<DogDto>>> getByTrainingLevel(
            @PathVariable Dog.TrainingLevel level) {
        List<DogDto> dogs = dogService.getByTrainingLevel(level);
        return ResponseEntity.ok(ApiResponse.success(dogs));
    }

    @GetMapping("/dog-friendly")
    public ResponseEntity<ApiResponse<List<DogDto>>> getDogFriendlyDogs() {
        List<DogDto> dogs = dogService.getDogFriendlyDogs();
        return ResponseEntity.ok(ApiResponse.success(dogs));
    }

    @GetMapping("/child-friendly")
    public ResponseEntity<ApiResponse<List<DogDto>>> getChildFriendlyDogs() {
        List<DogDto> dogs = dogService.getChildFriendlyDogs();
        return ResponseEntity.ok(ApiResponse.success(dogs));
    }
}
