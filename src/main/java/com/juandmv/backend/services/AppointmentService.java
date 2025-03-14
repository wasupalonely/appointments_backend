package com.juandmv.backend.services;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.exceptions.ScheduleConflictException;
import com.juandmv.backend.models.dto.CreateAppointmentDto;
import com.juandmv.backend.models.entities.*;
import com.juandmv.backend.repositories.AppointmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentTypeService appointmentTypeService;

    @Autowired
    private UnavailabilityService unavailabilityService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private UserService userService;

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    public List<Appointment> findAll() { return appointmentRepository.findAll(); }

    public List<Appointment> findByPatientId(Long patientId) { return appointmentRepository.findByPatientId(patientId); }

    public List<Appointment> findByDoctorId(Long doctorId) { return appointmentRepository.findByDoctorId(doctorId); }

    @Transactional
    public Appointment save(CreateAppointmentDto createAppointmentDto) {
        AppointmentType appointmentType = appointmentTypeService.findById(createAppointmentDto.getAppointmentTypeId());
        User patient = userService.findById(createAppointmentDto.getPatientId());

        // Validar que el usuario no tenga citas activas
        this.validateActiveAppointmentsByUser(patient);

        int procedureDuration = appointmentType.getDurationInMinutes(); // Duración del procedimiento en minutos
        int bufferMinutes = 20; // Tiempo adicional de descanso o margen

        // Calcular la fecha final
        LocalDateTime endTime = createAppointmentDto.getStartTime().plusMinutes(procedureDuration + bufferMinutes);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartTime(createAppointmentDto.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setNotes(createAppointmentDto.getNotes());
        appointment.setAppointmentType(appointmentType);
        appointment.setStatus(AppointmentStatus.PENDING);

        // Asignar un doctor disponible
        User doctor = getAppointmentDoctor(appointment);
        appointment.setDoctor(doctor);

        // Manejar citas derivadas
        if (createAppointmentDto.getParentAppointmentId() != null) {
            Appointment parentAppointment = this.findById(createAppointmentDto.getParentAppointmentId());
            appointment.setParentAppointment(parentAppointment);
            parentAppointment.getDerivedAppointments().add(appointment);
            appointmentRepository.save(parentAppointment);
        }

        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        // Se puede hacer un update a status cancelado
        this.findById(id);
        appointmentRepository.deleteById(id);
    }

    private User getAppointmentDoctor(Appointment appointment) {
        Long specialtyId = appointment.getAppointmentType().getSpecialty().getId();
        List<User> doctors = userService.findBySpecialtyId(specialtyId);

        if (doctors.isEmpty()) {
            throw new ResourceNotFoundException("No hay doctores disponibles para la especialidad seleccionada");
        }

        User selectedDoctor = null;
        int minAppointments = Integer.MAX_VALUE;

        for (User doctor : doctors) {
            if (isDoctorAvailable(doctor, appointment.getStartTime(), appointment.getEndTime())) {
                int numOfAppointments = appointmentRepository.countByDoctorId(doctor.getId());
                if (numOfAppointments < minAppointments) {
                    minAppointments = numOfAppointments;
                    selectedDoctor = doctor;
                }
            }
        }

        if (selectedDoctor == null) {
            throw new ResourceNotFoundException("No hay doctores disponibles en el horario seleccionado");
        }

        return selectedDoctor;
    }

    private boolean isDoctorAvailable(User doctor, LocalDateTime startTime, LocalDateTime endTime) {
        // Validar disponibilidad por availability
        List<Availability> availabilities = availabilityService.findByDoctorId(doctor.getId());
        for (Availability availability : availabilities) {
            if (startTime.isBefore(availability.getEndTime()) && endTime.isAfter(availability.getStartTime())) {
                return false;
            }
        }

        // Validar indisponibilidad por unavailability
        List<Unavailability> unavailabilities = unavailabilityService.findByDoctorId(doctor.getId());
        for (Unavailability unavailability : unavailabilities) {
            if (startTime.isBefore(unavailability.getEndTime()) && endTime.isAfter(unavailability.getStartTime())) {
                return false;
            }
        }

        // Validar citas existentes
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());
        for (Appointment appointment : appointments) {
            if (startTime.isBefore(appointment.getEndTime()) && endTime.isAfter(appointment.getStartTime())) {
                return false;
            }
        }

        return true;
    }

    private void validateActiveAppointmentsByUser(User user) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(user.getId());
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.PENDING ||
                    appointment.getStatus() == AppointmentStatus.RE_SCHEDULED) {
                throw new ScheduleConflictException("El usuario tiene una cita activa");
            }
        }
    }
}
