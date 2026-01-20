package com.pet.web;

import com.pet.domain.Cat;
import com.pet.dto.CatDto;
import com.pet.dto.response.ApiResponse;
import com.pet.service.CatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cats")
public class CatController {

    private final CatService catService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<CatDto>>> getAllCats() {
        List<CatDto> cats = catService.getAll();
        return ResponseEntity.ok(ApiResponse.success(cats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatDto>> getCat(@PathVariable UUID id) {
        CatDto cat = catService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(cat));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CatDto>> createCat(@Valid @RequestBody CatDto catDto) {
        CatDto createdCat = catService.create(catDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("貓咪創建成功", createdCat));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CatDto>> updateCat(
            @PathVariable UUID id,
            @Valid @RequestBody CatDto catDto) {
        CatDto updatedCat = catService.update(id, catDto);
        return ResponseEntity.ok(ApiResponse.success("貓咪更新成功", updatedCat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCat(@PathVariable UUID id) {
        catService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("貓咪刪除成功", null));
    }

    // ========== 貓特有的 API ==========

    @GetMapping("/indoor")
    public ResponseEntity<ApiResponse<List<CatDto>>> getIndoorCats() {
        List<CatDto> cats = catService.getIndoorCats();
        return ResponseEntity.ok(ApiResponse.success(cats));
    }

    @GetMapping("/outdoor")
    public ResponseEntity<ApiResponse<List<CatDto>>> getOutdoorCats() {
        List<CatDto> cats = catService.getOutdoorCats();
        return ResponseEntity.ok(ApiResponse.success(cats));
    }

    @GetMapping("/litter-box-type/{type}")
    public ResponseEntity<ApiResponse<List<CatDto>>> getByLitterBoxType(
            @PathVariable Cat.LitterBoxType type) {
        List<CatDto> cats = catService.getByLitterBoxType(type);
        return ResponseEntity.ok(ApiResponse.success(cats));
    }

    @GetMapping("/scratching-habit/{habit}")
    public ResponseEntity<ApiResponse<List<CatDto>>> getByScratchingHabit(
            @PathVariable Cat.ScratchingHabit habit) {
        List<CatDto> cats = catService.getByScratchingHabit(habit);
        return ResponseEntity.ok(ApiResponse.success(cats));
    }
}
