package com.pet.web;

import com.pet.domain.Pet;
import com.pet.repository.PetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetRepository petRepository;

    public PetController(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @GetMapping
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPet(@PathVariable Long id) {
        return petRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) throws URISyntaxException {
        if (pet.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Pet result = petRepository.save(pet);
        return ResponseEntity.created(new URI("/api/pets/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pet> updatePet(@PathVariable Long id, @RequestBody Pet pet) {
        if (!petRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pet.setId(id);
        Pet result = petRepository.save(pet);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
