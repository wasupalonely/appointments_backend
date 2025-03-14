package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreatePhysicalLocationDto;
import com.juandmv.backend.models.dto.UpdatePhysicalLocationDto;
import com.juandmv.backend.models.entities.PhysicalLocation;
import com.juandmv.backend.services.PhysicalLocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/physical-locations")
public class PhysicalLocationController {

    @Autowired
    private PhysicalLocationService physicalLocationService;

    @GetMapping
    public ResponseEntity<List<PhysicalLocation>> findAll() {
        return ResponseEntity.ok(this.physicalLocationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhysicalLocation> findById(@PathVariable Long id) {
        return ResponseEntity.ok(this.physicalLocationService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PhysicalLocation> save(@Valid @RequestBody CreatePhysicalLocationDto physicalLocation) {
        return ResponseEntity.ok(this.physicalLocationService.save(physicalLocation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhysicalLocation> update(@PathVariable Long id, @Valid @RequestBody UpdatePhysicalLocationDto physicalLocation) {
        return ResponseEntity.ok(this.physicalLocationService.update(id, physicalLocation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
