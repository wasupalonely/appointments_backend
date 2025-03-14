package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateAppointmentDto;
import com.juandmv.backend.models.entities.Appointment;
import com.juandmv.backend.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Citas", description = "Endpoints para gestionar citas")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Operation(summary = "Obtener las citas", description = "Devuelve todas las citas")
    @ApiResponse(responseCode = "200", description = "Citas obtenidas correctamente")
    @GetMapping
    public ResponseEntity<List<Appointment>> findAll() {
        return ResponseEntity.ok(this.appointmentService.findAll());
    }

    @Operation(summary = "Obtener las citas por paciente", description = "Devuelve todas las citas de un paciente")
    @ApiResponse(responseCode = "200", description = "Citas obtenidas correctamente")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> findByPatientId(@Parameter(description = "Id del paciente", example = "1")
                                                 @PathVariable Long patientId) {
        return ResponseEntity.ok(this.appointmentService.findByPatientId(patientId));
    }

    @Operation(summary = "Obtener las citas por doctor asignado", description = "Devuelve todas las citas asignadas a un doctor")
    @ApiResponse(responseCode = "200", description = "Citas obtenidas correctamente")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> findByDoctorId(@Parameter(description = "Id del paciente", example = "1")
                                                @PathVariable Long doctorId) {
        return ResponseEntity.ok(this.appointmentService.findByDoctorId(doctorId));
    }

    @Operation(summary = "Crear una cita", description = "Devuelve la cita creada")
    @ApiResponse(responseCode = "201", description = "Cita creada correctamente")
    @PostMapping
    public ResponseEntity<Appointment> save(@Parameter(description = "Cita a crear", required = true)
                                @Valid @RequestBody CreateAppointmentDto createAppointmentDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.appointmentService.save(createAppointmentDto));
    }
}
