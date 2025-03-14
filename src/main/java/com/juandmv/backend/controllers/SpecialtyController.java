package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateSpecialtyDto;
import com.juandmv.backend.models.dto.UpdateSpecialtyDto;
import com.juandmv.backend.models.entities.Specialty;
import com.juandmv.backend.services.SpecialtyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/specialties")
public class SpecialtyController {

    @Autowired
    private SpecialtyService specialtyService;

    @GetMapping
    public ResponseEntity<List<Specialty>> findAll() {
        return ResponseEntity.ok(specialtyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Specialty> findById(@PathVariable Long id) {
        return ResponseEntity.ok(specialtyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Specialty> save(@Valid @RequestBody CreateSpecialtyDto specialty) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtyService.save(specialty));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Specialty> update(@PathVariable Long id, @Valid @RequestBody UpdateSpecialtyDto specialty) {
        return ResponseEntity.ok(specialtyService.update(id, specialty));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        specialtyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
