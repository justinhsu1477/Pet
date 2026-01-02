package com.soetek.practice.web;

import com.soetek.practice.domain.Sitter;
import com.soetek.practice.repository.SitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/sitters")
@RequiredArgsConstructor
public class SitterController {

    private final SitterRepository sitterRepository;

    @GetMapping
    public List<Sitter> getAllSitters() {
        return sitterRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sitter> getSitter(@PathVariable Long id) {
        return sitterRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Sitter> createSitter(@RequestBody Sitter sitter) throws URISyntaxException {
        if (sitter.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Sitter result = sitterRepository.save(sitter);
        return ResponseEntity.created(new URI("/api/sitters/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sitter> updateSitter(@PathVariable Long id, @RequestBody Sitter sitter) {
        if (!sitterRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sitter.setId(id);
        Sitter result = sitterRepository.save(sitter);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSitter(@PathVariable Long id) {
        sitterRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
