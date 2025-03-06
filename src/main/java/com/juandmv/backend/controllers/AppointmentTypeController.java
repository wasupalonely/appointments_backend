package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateAppointmentTypeDto;
import com.juandmv.backend.models.entities.AppointmentType;
import com.juandmv.backend.services.AppointmentTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointment-types")
public class AppointmentTypeController {

    @Autowired
    private AppointmentTypeService appointmentTypeService;

    @GetMapping
    public ResponseEntity<List<AppointmentType>> findAll() {
        return ResponseEntity.ok(this.appointmentTypeService.findAll());
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<AppointmentType>> findBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(this.appointmentTypeService.findBySpecialty(specialty));
    }

    @PostMapping
    public ResponseEntity<AppointmentType> save(@Valid @RequestBody CreateAppointmentTypeDto appointmentType) {
        return ResponseEntity.ok(this.appointmentTypeService.save(appointmentType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.appointmentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
