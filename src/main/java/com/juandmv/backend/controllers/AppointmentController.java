package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateAppointmentDto;
import com.juandmv.backend.models.entities.Appointment;
import com.juandmv.backend.services.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> findAll() { return appointmentService.findAll(); }

    @GetMapping("/patient/{patientId}")
    public List<Appointment> findByPatientId(@PathVariable Long patientId) { return appointmentService.findByPatientId(patientId); }

    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> findByDoctorId(@PathVariable Long doctorId) { return appointmentService.findByDoctorId(doctorId); }

    @PostMapping
    public Appointment save(@Valid @RequestBody CreateAppointmentDto createAppointmentDto) { return appointmentService.save(createAppointmentDto); }
}
