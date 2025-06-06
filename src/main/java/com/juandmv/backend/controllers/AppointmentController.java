package com.juandmv.backend.controllers;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.models.dto.CancelAppointmentDto;
import com.juandmv.backend.models.dto.CreateAppointmentDto;
import com.juandmv.backend.models.dto.UpdateAppointmentDto;
import com.juandmv.backend.models.dto.UpdateAppointmentTypeDto;
import com.juandmv.backend.models.entities.Appointment;
import com.juandmv.backend.models.responses.CountResponse;
import com.juandmv.backend.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Citas", description = "Endpoints para gestionar citas")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/count")
    public ResponseEntity<CountResponse> countByPatientIdAndStatus(
            @RequestParam Long userId,
            @RequestParam AppointmentStatus status
            ) {
        return ResponseEntity.ok(this.appointmentService.countByPatientIdAndStatus(userId, status));
    }

    @Operation(summary = "Obtener las citas filtradas", description = "Devuelve las citas según los filtros aplicados")
    @ApiResponse(responseCode = "200", description = "Citas obtenidas correctamente")
    @GetMapping
    public ResponseEntity<Page<Appointment>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        // Crear objeto Pageable para paginación y ordenamiento
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Determinar qué método llamar según los parámetros proporcionados
        if (startDate == null && endDate == null && status == null) {
            return ResponseEntity.ok(this.appointmentService.findAll(pageable));
        } else {
            return ResponseEntity.ok(this.appointmentService.findAppointmentsByFilters(
                    startDate, endDate, status, pageable));
        }
    }

//    @Operation(summary = "Obtener las citas", description = "Devuelve todas las citas")
//    @ApiResponse(responseCode = "200", description = "Citas obtenidas correctamente")
//    @GetMapping
//    public ResponseEntity<List<Appointment>> findAll() {
//        return ResponseEntity.ok(this.appointmentService.findAll());
//    }

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

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> update(@PathVariable Long id, @Valid @RequestBody UpdateAppointmentDto updateAppointmentDto) {
        return ResponseEntity.ok(this.appointmentService.update(id, updateAppointmentDto));
    }

    @PatchMapping("/{appointmentId}/cancel/{userId}")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long appointmentId,
                                                      @PathVariable Long userId,
                                                      @RequestBody CancelAppointmentDto cancelAppointmentDto) {
        return ResponseEntity.ok(this.appointmentService.cancelAppointment(appointmentId, userId, cancelAppointmentDto.getCancellationReason()));
    }

    @PatchMapping("/{appointmentId}/status/{status}")
    public ResponseEntity<Appointment> changeStatus(@PathVariable Long appointmentId, @PathVariable AppointmentStatus status) {
        return ResponseEntity.ok(this.appointmentService.changeStatus(appointmentId, status));
    }

    @GetMapping("/closest-appointment/{userId}")
    public ResponseEntity<Appointment> getClosestAppointment(@PathVariable Long userId) {
        return ResponseEntity.ok(this.appointmentService.getNextAppointmentByPatientId(userId));
    }

//    @GetMapping("/available")
//    public ResponseEntity<?> findAvailableAppointments(
//            @RequestParam Long appointmentTypeId,
//            @RequestParam(required = false) LocalDateTime startRange,
//            @RequestParam(required = false) LocalDateTime endRange) {
//
//        // Si no se proporciona un rango de fechas, usar un rango predeterminado (por ejemplo, las próximas 4 semanas)
//        if (startRange == null) {
//            startRange = LocalDateTime.now();
//        }
//        if (endRange == null) {
//            endRange = startRange.plusWeeks(4);
//        }
//
//        // Llamar al servicio para buscar citas disponibles
//        List<Map<String, Object>> availableAppointments = appointmentService.findAvailableAppointments(
//                appointmentTypeId, startRange, endRange);
//
//        return ResponseEntity.ok(Map.of("availableAppointments", availableAppointments));
//    }
}