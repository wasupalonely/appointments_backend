package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateUnavailabilityDto;
import com.juandmv.backend.models.entities.Unavailability;
import com.juandmv.backend.services.UnavailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unavailabilities")
public class UnavailabilityController {

    @Autowired
    private UnavailabilityService unavailabilityService;

    @GetMapping
    public ResponseEntity<List<Unavailability>> findAll() {
        return ResponseEntity.ok(this.unavailabilityService.findAll());
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<Unavailability>> findByDoctorId(@PathVariable Long id) {
        return ResponseEntity.ok(this.unavailabilityService.findByDoctorId(id));
    }

    @PostMapping
    public ResponseEntity<Unavailability> save(@Valid @RequestBody CreateUnavailabilityDto unavailability) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.unavailabilityService.save(unavailability));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.unavailabilityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
