package com.juandmv.backend.services;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.enums.ReminderType;
import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.exceptions.BadRequestException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.exceptions.ScheduleConflictException;
import com.juandmv.backend.models.dto.*;
import com.juandmv.backend.models.entities.*;
import com.juandmv.backend.models.responses.CountResponse;
import com.juandmv.backend.repositories.AppointmentRepository;
import com.juandmv.backend.utils.Utils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ReminderService reminderService;

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    public Page<Appointment> findAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    public Page<Appointment> findAppointmentsByFilters(LocalDateTime startDate, LocalDateTime endDate,
                                                       AppointmentStatus status, Pageable pageable) {

        return appointmentRepository.findAppointmentsByFilters(startDate, endDate, status, pageable);
    }

    public List<Appointment> findByPatientId(Long patientId) { return appointmentRepository.findByPatientId(patientId); }

    public List<Appointment> findByDoctorId(Long doctorId) { return appointmentRepository.findByDoctorId(doctorId); }

    public List<Appointment> findByDoctorIdAndStatusNotIn(Long doctorId, List<AppointmentStatus> status) {
        return appointmentRepository.findByDoctorIdAndStatusNotIn(doctorId, status);
    }

    @Transactional
    public Appointment save(CreateAppointmentDto createAppointmentDto) {
        AppointmentType appointmentType = appointmentTypeService.findById(createAppointmentDto.getAppointmentTypeId());

        User patient = userService.findById(createAppointmentDto.getPatientId());
        User doctor = userService.findById(createAppointmentDto.getDoctorId());

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartTime(createAppointmentDto.getStartTime());
        appointment.setEndTime(createAppointmentDto.getEndTime());
        appointment.setNotes(createAppointmentDto.getNotes());
        appointment.setAppointmentType(appointmentType);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setDoctor(doctor);
        appointment.setPhysicalLocation(doctor.getPhysicalLocation());

        // Manejar citas derivadas
        if (createAppointmentDto.getParentAppointmentId() != null) {
            Appointment parentAppointment = this.findById(createAppointmentDto.getParentAppointmentId());
            appointment.setParentAppointment(parentAppointment);
            parentAppointment.getDerivedAppointments().add(appointment);
            appointmentRepository.save(parentAppointment);
        }

        Appointment appointmentSaved = appointmentRepository.save(appointment);

        emailService.sendAppointmentNotifications(appointmentSaved);
        
        return appointmentSaved;
    }

    // TODO: Revisar lógica para actualización.
    @Transactional
    public Appointment update(Long id, @Valid UpdateAppointmentDto updateAppointmentDto) {
        Appointment appointment = this.findById(id);

        appointment.setStartTime(updateAppointmentDto.getStartTime());
        appointment.setEndTime(updateAppointmentDto.getEndTime());

        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        // Se puede hacer un update a status cancelado
        this.findById(id);
        appointmentRepository.deleteById(id);
    }

    // TODO: Revisar lógica
    public Map<String, Object> cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = this.findById(appointmentId);
        User user = userService.findById(userId);
        appointment.setStatus(user.getSpecialty() != null
                ? AppointmentStatus.CANCELLED_BY_DOCTOR :
                AppointmentStatus.CANCELLED_BY_PATIENT);

        this.appointmentRepository.save(appointment);

        if (user.getSpecialty() != null) {
            // Se crea un unavailability temporal para que no se asigne el mismo doctor
            CreateUnavailabilityDto createUnavailabilityDto = new CreateUnavailabilityDto();
            createUnavailabilityDto.setDoctorId(user.getId());
            createUnavailabilityDto.setStartTime(appointment.getStartTime());
            createUnavailabilityDto.setEndTime(appointment.getEndTime());
            createUnavailabilityDto.setReason("Cita cancelada por el doctor (Es un unavailability temporal)");
            Unavailability tempUnavailability = unavailabilityService.save(createUnavailabilityDto);

            // Se crea otra cita
            CreateAppointmentDto createAppointmentDto = new CreateAppointmentDto();
            createAppointmentDto.setPatientId(appointment.getPatient().getId());
            createAppointmentDto.setStartTime(appointment.getStartTime());
            createAppointmentDto.setNotes(appointment.getNotes());
            createAppointmentDto.setAppointmentTypeId(appointment.getAppointmentType().getId());
            // Se debería crear un parent appointment?
            this.save(createAppointmentDto);

            // Se borra el unavailability temporal
            unavailabilityService.delete(tempUnavailability.getId());
        }

        Map<String, Object> body = new HashMap<>();

        boolean isPatient = user.getSpecialty() == null;

        body.put("appointment", appointment);
        if (isPatient) {
            body.put("message", "La cita ha sido cancelada por el paciente");
        } else {
            body.put("message", "La cita ha sido cancelada por el doctor, se le asignara un nuevo doctor");
        }

        this.emailService.sendCancellationEmailAndReminderAsync(appointment, user, isPatient);

        return body;
    }

    public Appointment changeStatus(Long id, AppointmentStatus status) {
        Appointment appointment = this.findById(id);
        appointment.setStatus(status);
        return this.appointmentRepository.save(appointment);
    }

    public CountResponse countByPatientIdAndStatus(Long patientId, AppointmentStatus status) {
        Long count = this.appointmentRepository.countByPatientIdAndStatus(patientId, status);

        return new CountResponse(
                count
        );
    }

    public Appointment getNextAppointmentByPatientId(Long patientId) {
        this.userService.findById(patientId);

        LocalDateTime now = LocalDateTime.now();

        List<Appointment> patientAppointmentsResp = this.findByPatientId(patientId);

        List<Appointment> patientAppointments = patientAppointmentsResp.stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.PENDING)
                .toList();

        Optional<Appointment> closestAppointment = patientAppointments.stream()
                .min(Comparator.comparing(date ->
                        Math.abs(ChronoUnit.SECONDS.between(now, date.getStartTime()))
                ));

        return closestAppointment.orElse(null);
    }
}
