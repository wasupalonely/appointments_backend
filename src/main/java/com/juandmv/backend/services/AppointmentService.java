package com.juandmv.backend.services;

import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateAppointmentDto;
import com.juandmv.backend.models.entities.Appointment;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    public List<Appointment> findAll() { return appointmentRepository.findAll(); }

    public List<Appointment> findByPatientId(Long patientId) { return appointmentRepository.findByPatientId(patientId); }

    public List<Appointment> findByDoctorId(Long doctorId) { return appointmentRepository.findByDoctorId(doctorId); }

    public Appointment save(CreateAppointmentDto createAppointmentDto) {
        // TODO: Validar si se va a tener en cuenta la especialidad para asignar un doctor con horario disponible
        User patient = userService.findById(createAppointmentDto.getPatientId());

        // Algoritmo para asignar un doctor con horario disponible

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartTime(createAppointmentDto.getStartTime());
        appointment.setEndTime(createAppointmentDto.getEndTime());
        appointment.setNotes(createAppointmentDto.getNotes());

        User doctor = getAppointmentDoctor(appointment);
        appointment.setDoctor(doctor);

        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        // Se puede hacer un update a status cancelado
        this.findById(id);
        appointmentRepository.deleteById(id);
    }

    private User getAppointmentDoctor(Appointment appointment) {
        // TODO: Validar especialidad (sería buscar por especialidad si se implementa)
        // Primero, se buscan a los doctores con horario disponible en la hora del
        // appointment con availability y unavailability (vAlidar espeicialida si se implementa)

        List<Map<String, Object>> availableDoctors = new ArrayList<>();
        List<User> doctors = userService.findByRole("ROLE_DOCTOR");

        if (doctors.isEmpty()) {
            throw new ResourceNotFoundException("No hay doctores disponibles");
        }

        doctors.forEach(doctor -> {
            List<Appointment> doctorAppointments = findByDoctorId(doctor.getId());
            boolean isAvailable = true;
            for (Appointment doctorAppointment : doctorAppointments) {
                if (appointment.getStartTime().after(doctorAppointment.getStartTime())
                        && appointment.getStartTime().before(doctorAppointment.getEndTime())) {
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable) {
                Map<String, Object> availableDoctor = new HashMap<>();
                availableDoctor.put("numOfAppointments", doctorAppointments.size());
                availableDoctor.put("doctor", doctor);
                availableDoctors.add(availableDoctor);
            }
        });

        // Luego se valida entre los doctores el que tenga menos citas y se le asigna la cita
        User doctor = null;
        int minAppointments = Integer.MAX_VALUE;
        for (Map<String, Object> availableDoctor : availableDoctors) {
            int numOfAppointments = (int) availableDoctor.get("numOfAppointments");
            if (numOfAppointments < minAppointments) {
                minAppointments = numOfAppointments;
                doctor = (User) availableDoctor.get("doctor");
            }
        }

        return doctor;
    }
}
