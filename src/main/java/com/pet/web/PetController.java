package com.pet.web;

import com.pet.dto.PetDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<PetDto>>> getAllPets() {
        List<PetDto> pets = petService.getAllPets();
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetDto>> getPet(@PathVariable UUID id) {
        PetDto pet = petService.getPetById(id);
        return ResponseEntity.ok(ApiResponse.success(pet));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PetDto>> createPet(@Valid @RequestBody PetDto petDto) {
        PetDto createdPet = petService.createPet(petDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("寵物創建成功", createdPet));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PetDto>> updatePet(
            @PathVariable UUID id,
            @Valid @RequestBody PetDto petDto) {
        PetDto updatedPet = petService.updatePet(id, petDto);
        return ResponseEntity.ok(ApiResponse.success("寵物更新成功", updatedPet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePet(@PathVariable UUID id) {
        petService.deletePet(id);
        return ResponseEntity.ok(ApiResponse.success("寵物刪除成功", null));
    }
}
