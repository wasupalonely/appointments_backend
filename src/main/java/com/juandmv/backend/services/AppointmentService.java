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
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    public static class AppointmentCancellationConfig {
        public static final int MIN_HOURS_TO_CANCEL = 24; // Mínimo 24 horas antes
        public static final int MAX_RESCHEDULE_ATTEMPTS = 3; // Máximo 3 intentos de reagendar
    }

    @Transactional
    public Map<String, Object> cancelAppointment(Long appointmentId, Long userId, String cancellationReason) {
        Appointment appointment = this.findById(appointmentId);
        User user = userService.findById(userId);

        // Validaciones previas
        validateCancellation(appointment, user);

        boolean isDoctorCancelling = user.getSpecialty() != null;
        AppointmentStatus newStatus = isDoctorCancelling ?
                AppointmentStatus.CANCELLED_BY_DOCTOR :
                AppointmentStatus.CANCELLED_BY_PATIENT;

        // Actualizar estado de la cita
        appointment.setStatus(newStatus);
        appointment.setCancellationReason(cancellationReason);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelledBy(user);

        Map<String, Object> result = new HashMap<>();

        if (isDoctorCancelling) {
            result = handleDoctorCancellation(appointment, user);
        } else {
            result = handlePatientCancellation(appointment, user);
        }

        // Guardar cambios
        appointmentRepository.save(appointment);

        // Enviar notificaciones
        emailService.sendCancellationEmailAndReminderAsync(appointment, user, !isDoctorCancelling);

        return result;
    }

    private void validateCancellation(Appointment appointment, User user) {
        // Verificar que la cita no esté ya cancelada o completada
        if (appointment.getStatus() == AppointmentStatus.CANCELLED_BY_DOCTOR ||
                appointment.getStatus() == AppointmentStatus.CANCELLED_BY_PATIENT ||
                appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("No se puede cancelar una cita que ya está " +
                    appointment.getStatus().toString().toLowerCase());
        }

        // Verificar que el usuario tenga permisos para cancelar
        boolean isPatient = appointment.getPatient().getId().equals(user.getId());
        boolean isDoctor = appointment.getDoctor().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().contains(Roles.ADMIN);

        if (!isPatient && !isDoctor && !isAdmin) {
            throw new BadRequestException("No tienes permisos para cancelar esta cita");
        }

        // Verificar tiempo mínimo para cancelación (solo para pacientes)
        if (isPatient && !isAdmin) {
            LocalDateTime minCancellationTime = appointment.getStartTime()
                    .minusHours(AppointmentCancellationConfig.MIN_HOURS_TO_CANCEL);

            if (LocalDateTime.now().isAfter(minCancellationTime)) {
                throw new BadRequestException(
                        "No se puede cancelar la cita con menos de " +
                                AppointmentCancellationConfig.MIN_HOURS_TO_CANCEL + " horas de anticipación"
                );
            }
        }
    }

    private Map<String, Object> handlePatientCancellation(Appointment appointment, User patient) {
        Map<String, Object> result = new HashMap<>();

        // Cancelar también las citas derivadas si existen
        if (!appointment.getDerivedAppointments().isEmpty()) {
            for (Appointment derivedAppointment : appointment.getDerivedAppointments()) {
                if (derivedAppointment.getStatus() == AppointmentStatus.PENDING) {
                    derivedAppointment.setStatus(AppointmentStatus.CANCELLED_BY_PATIENT);
                    derivedAppointment.setCancellationReason("Cancelada por cancelación de cita principal");
                    derivedAppointment.setCancelledAt(LocalDateTime.now());
                    derivedAppointment.setCancelledBy(patient);
                }
            }
        }

        result.put("appointment", appointment);
        result.put("message", "La cita ha sido cancelada exitosamente");
        result.put("canReschedule", true);
        result.put("suggestedSlots", getSuggestedRescheduleSlots(appointment));

        return result;
    }

    private Map<String, Object> handleDoctorCancellation(Appointment appointment, User doctor) {
        Map<String, Object> result = new HashMap<>();

        // Intentar reagendar automáticamente
        RescheduleResult rescheduleResult = attemptAutomaticReschedule(appointment);

        if (rescheduleResult.isSuccess()) {
            result.put("appointment", appointment);
            result.put("newAppointment", rescheduleResult.getNewAppointment());
            result.put("message", "La cita ha sido reagendada automáticamente con otro doctor disponible");
            result.put("automaticReschedule", true);
        } else {
            // No se pudo reagendar automáticamente
            result.put("appointment", appointment);
            result.put("message", "La cita ha sido cancelada. Se contactará al paciente para reagendarla");
            result.put("automaticReschedule", false);
            result.put("availableSlots", rescheduleResult.getAvailableSlots());
            result.put("alternativeDoctors", rescheduleResult.getAlternativeDoctors());

            // Crear tarea pendiente para reagendar manualmente
            createRescheduleTask(appointment, rescheduleResult);
        }

        return result;
    }

    private RescheduleResult attemptAutomaticReschedule(Appointment originalAppointment) {
        RescheduleResult result = new RescheduleResult();

        try {
            // 1. Buscar doctores alternativos con la misma especialidad
            List<User> alternativeDoctors = userService.findBySpecialtyId(
                    originalAppointment.getDoctor().getSpecialty().getId()
            );

            // Excluir el doctor original
            alternativeDoctors.removeIf(doc -> doc.getId().equals(originalAppointment.getDoctor().getId()));

            if (alternativeDoctors.isEmpty()) {
                result.setSuccess(false);
                result.setReason("No hay doctores alternativos con la misma especialidad");
                return result;
            }

            // 2. Buscar slots disponibles en la misma fecha/hora
            LocalDateTime originalStart = originalAppointment.getStartTime();
            LocalDateTime originalEnd = originalAppointment.getEndTime();

            for (User alternativeDoctor : alternativeDoctors) {
                if (isDoctorAvailableForSlot(alternativeDoctor.getId(), originalStart, originalEnd, originalAppointment.getAppointmentType())) {
                    // Crear nueva cita con el doctor alternativo
                    Appointment newAppointment = createRescheduledAppointment(
                            originalAppointment,
                            alternativeDoctor,
                            originalStart,
                            originalEnd
                    );

                    System.out.println("CREANDO CITA");

                    result.setSuccess(true);
                    result.setNewAppointment(newAppointment);
                    result.setNewDoctor(alternativeDoctor);
                    return result;
                }
            }

            // 3. Si no hay disponibilidad en la misma hora, buscar slots cercanos
            List<AvailableSlotDto> nearbySlots = findNearbyAvailableSlots(
                    originalAppointment,
                    alternativeDoctors
            );

            result.setSuccess(false);
            result.setAvailableSlots(nearbySlots);
            result.setAlternativeDoctors(alternativeDoctors);
            result.setReason("No hay disponibilidad en la misma hora, pero hay slots alternativos");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setReason("Error al intentar reagendar: " + e.getMessage());
        }

        return result;
    }

    private boolean isDoctorAvailableForSlot(Long doctorId, LocalDateTime startTime, LocalDateTime endTime, AppointmentType appointmentType) {
        // Verificar disponibilidad del doctor
        LocalDate date = startTime.toLocalDate();
        List<AvailableSlotDto> availableSlots = this.availabilityService.findAvailableSlots(
                doctorId,
                appointmentType, // Usaremos la duración del slot existente
                date,
                date
        );

        return availableSlots.stream()
                .anyMatch(slot ->
                        !slot.getStartTime().isAfter(startTime) &&
                                !slot.getEndTime().isBefore(endTime)
                );
    }

    private Appointment createRescheduledAppointment(
            Appointment originalAppointment,
            User newDoctor,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        CreateAppointmentDto createDto = new CreateAppointmentDto();
        createDto.setPatientId(originalAppointment.getPatient().getId());
        createDto.setDoctorId(newDoctor.getId());
        createDto.setAppointmentTypeId(originalAppointment.getAppointmentType().getId());
        createDto.setStartTime(startTime);
        createDto.setEndTime(endTime);
        createDto.setNotes(originalAppointment.getNotes() + " - Reagendada por cancelación de doctor");

        // NO establecer parentAppointmentId para evitar el bucle
        // createDto.setParentAppointmentId(originalAppointment.getId());

        Appointment newAppointment = this.save(createDto);

        // Establecer la relación manualmente después del guardado
        newAppointment.setParentAppointment(originalAppointment);
        originalAppointment.getDerivedAppointments().add(newAppointment);

        // Guardar solo la nueva cita, no la original para evitar triggers
        return appointmentRepository.save(newAppointment);
    }

    private List<AvailableSlotDto> getSuggestedRescheduleSlots(Appointment originalAppointment) {
        // Sugerir slots en los próximos 7 días
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(7);

        return this.availabilityService.findAvailableSlots(
                originalAppointment.getDoctor().getId(),
                originalAppointment.getAppointmentType(),
                startDate,
                endDate
        );
    }

    private List<AvailableSlotDto> findNearbyAvailableSlots(
            Appointment originalAppointment,
            List<User> alternativeDoctors) {

        List<AvailableSlotDto> allSlots = new ArrayList<>();
        LocalDate originalDate = originalAppointment.getStartTime().toLocalDate();

        // Buscar en un rango de ±3 días
        LocalDate startDate = originalDate.minusDays(3);
        LocalDate endDate = originalDate.plusDays(3);

        for (User doctor : alternativeDoctors) {
            List<AvailableSlotDto> doctorSlots = this.availabilityService.findAvailableSlots(
                    doctor.getId(),
                    originalAppointment.getAppointmentType(),
                    startDate,
                    endDate
            );
            allSlots.addAll(doctorSlots);
        }

        // Ordenar por proximidad a la fecha original
        LocalDateTime originalDateTime = originalAppointment.getStartTime();
        allSlots.sort(Comparator.comparing(slot ->
                Math.abs(Duration.between(originalDateTime, slot.getStartTime()).toMinutes())
        ));

        // Retornar solo los primeros 10 slots más cercanos
        return allSlots.stream().limit(10).collect(Collectors.toList());
    }

    private void createRescheduleTask(Appointment appointment, RescheduleResult rescheduleResult) {
        // Aquí podrías crear una tarea en tu sistema de gestión de tareas
        // para que el personal administrativo contacte al paciente

        // Por ejemplo, guardar en una tabla de tareas pendientes
        // o enviar una notificación al equipo administrativo
    }

    // Clase auxiliar para el resultado del reagendamiento
    @Setter
    @Getter
    public static class RescheduleResult {
        // Getters y setters...
        private boolean success;
        private Appointment newAppointment;
        private User newDoctor;
        private List<AvailableSlotDto> availableSlots;
        private List<User> alternativeDoctors;
        private String reason;

    }
}
