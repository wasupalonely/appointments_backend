package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.AvailableSlotDto;
import com.juandmv.backend.models.dto.CreateAvailabilityDto;
import com.juandmv.backend.models.entities.AppointmentType;
import com.juandmv.backend.models.entities.Availability;
import com.juandmv.backend.repositories.AppointmentRepository;
import com.juandmv.backend.repositories.AppointmentTypeRepository;
import com.juandmv.backend.services.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/availabilities")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @GetMapping
    public ResponseEntity<List<Availability>> findAll() { return ResponseEntity.ok(this.availabilityService.findAll()); }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<Availability>> findByDoctorId(@PathVariable Long id) {
        return ResponseEntity.ok(this.availabilityService.findByDoctorId(id));
    }

    /**
     * Endpoint para obtener slots disponibles para un doctor en un período específico
     * considerando un tipo de cita específico.
     *
     * @param doctorId ID del médico
     * @param appointmentTypeId ID del tipo de cita
     * @param startDate Fecha inicial (opcional, por defecto es hoy)
     * @param endDate Fecha final (opcional, por defecto es 7 días después de la fecha inicial)
     * @return Lista de slots disponibles
     */
    @GetMapping("/slots")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam Long appointmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Si no se proporciona fecha inicial, usar la fecha actual
        if (startDate == null) {
            startDate = LocalDate.now();
        }

        // Si no se proporciona fecha final, usar 7 días después de la fecha inicial
        if (endDate == null) {
            endDate = startDate.plusDays(7);
        }

        // Obtener el tipo de cita
        Optional<AppointmentType> appointmentTypeOpt = appointmentTypeRepository.findById(appointmentTypeId);
        if (appointmentTypeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Tipo de cita no encontrado");
        }
        AppointmentType appointmentType = appointmentTypeOpt.get();

        // Obtener los slots disponibles
        List<AvailableSlotDto> availableSlots = availabilityService.findAvailableSlots(
                doctorId, appointmentType, startDate, endDate);

        return ResponseEntity.ok(availableSlots);
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
