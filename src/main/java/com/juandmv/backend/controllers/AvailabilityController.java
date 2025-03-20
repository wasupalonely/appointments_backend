package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateAvailabilityDto;
import com.juandmv.backend.models.entities.Availability;
import com.juandmv.backend.services.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/availabilities")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<Availability>> findAll() { return ResponseEntity.ok(this.availabilityService.findAll()); }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<Availability>> findByDoctorId(@PathVariable Long id) {
        return ResponseEntity.ok(this.availabilityService.findByDoctorId(id));
    }

    @PostMapping
    public ResponseEntity<Availability> save(@Valid @RequestBody CreateAvailabilityDto availability) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.availabilityService.save(availability));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.availabilityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
