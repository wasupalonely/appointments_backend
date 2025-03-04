package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateAppointmentDto;
import com.juandmv.backend.models.entities.Appointment;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        // appointment.setDoctor(doctor);

        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        // Se puede hacer un update a status cancelado
        this.findById(id);
        appointmentRepository.deleteById(id);
    }

    // TODO: Implementar algoritmo una vez hecho el availability
//    private User getAppointmentDoctor(Appointment appointment) {
//        // TODO: Validar especialidad (sería buscar por especialidad si se implementa)
//        if (appointment.getStartTime().after())
//    }
}
