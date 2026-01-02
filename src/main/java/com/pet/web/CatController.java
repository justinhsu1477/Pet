package com.pet.web;

import com.pet.domain.Cat;
import com.pet.repository.CatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/cats")
public class CatController {

    private final CatRepository catRepository;

    public CatController(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    @GetMapping
    public List<Cat> getAllCats() {
        return catRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Cat> createCat(@RequestBody Cat cat) throws URISyntaxException {
        if (cat.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Cat result = catRepository.save(cat);
        return ResponseEntity.created(new URI("/api/cats/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cat> updateCat(@PathVariable Long id, @RequestBody Cat cat) {
        if (!catRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cat.setId(id);
        Cat result = catRepository.save(cat);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCat(@PathVariable Long id) {
        catRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
