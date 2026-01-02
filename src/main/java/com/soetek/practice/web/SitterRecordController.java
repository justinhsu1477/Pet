package com.soetek.practice.web;

import com.soetek.practice.domain.SitterRecord;
import com.soetek.practice.repository.SitterRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class SitterRecordController {

    private final SitterRecordRepository sitterRecordRepository;

    @GetMapping
    public List<SitterRecord> getAllRecords() {
        return sitterRecordRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SitterRecord> getRecord(@PathVariable Long id) {
        return sitterRecordRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pet/{petId}")
    public List<SitterRecord> getRecordsByPet(@PathVariable Long petId) {
        return sitterRecordRepository.findByPetId(petId);
    }

    @GetMapping("/sitter/{sitterId}")
    public List<SitterRecord> getRecordsBySitter(@PathVariable Long sitterId) {
        return sitterRecordRepository.findBySitterId(sitterId);
    }

    @PostMapping
    public ResponseEntity<SitterRecord> createRecord(@RequestBody SitterRecord record) throws URISyntaxException {
        if (record.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        SitterRecord result = sitterRecordRepository.save(record);
        return ResponseEntity.created(new URI("/api/records/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SitterRecord> updateRecord(@PathVariable Long id, @RequestBody SitterRecord record) {
        if (!sitterRecordRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        record.setId(id);
        SitterRecord result = sitterRecordRepository.save(record);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        sitterRecordRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
