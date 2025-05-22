package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateAppointmentTypeDto;
import com.juandmv.backend.models.dto.UpdateAppointmentTypeDto;
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
    public ResponseEntity<List<AppointmentType>> findAll(@RequestParam(required = false) Boolean isGeneral) {
        return ResponseEntity.ok(this.appointmentTypeService.findAll(isGeneral));
    }

    @GetMapping("/specialty/{specialtyId}")
    public ResponseEntity<List<AppointmentType>> findBySpecialty(@PathVariable Long specialtyId, @RequestParam(required = false) Boolean isGeneral) {
        return ResponseEntity.ok(this.appointmentTypeService.findBySpecialty(specialtyId, isGeneral));
    }

    @PostMapping
    public ResponseEntity<AppointmentType> save(@Valid @RequestBody CreateAppointmentTypeDto appointmentType) {
        return ResponseEntity.ok(this.appointmentTypeService.save(appointmentType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentType> update(@PathVariable Long id, @Valid @RequestBody UpdateAppointmentTypeDto appointmentType) {
        return ResponseEntity.ok(this.appointmentTypeService.update(id, appointmentType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.appointmentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
