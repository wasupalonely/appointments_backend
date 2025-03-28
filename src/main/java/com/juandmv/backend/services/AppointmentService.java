package com.juandmv.backend.services;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.enums.ReminderType;
import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.exceptions.BadRequestException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.exceptions.ScheduleConflictException;
import com.juandmv.backend.models.dto.*;
import com.juandmv.backend.models.entities.*;
import com.juandmv.backend.repositories.AppointmentRepository;
import com.juandmv.backend.utils.Utils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public List<Appointment> findAll() { return appointmentRepository.findAll(); }

    public List<Appointment> findAppointmentsByFilters(LocalDateTime startDate, LocalDateTime endDate,
                                                       AppointmentStatus status, Long physicalLocationId) {

        return appointmentRepository.findAppointmentsByFilters(startDate, endDate, status, physicalLocationId);
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

        User doctor = null;

        // Asignar un doctor disponible
        if (createAppointmentDto.getDoctorId() == null) {
            doctor = getAppointmentDoctor(appointment);
        } else {
            doctor = userService.findById(createAppointmentDto.getDoctorId());

        }

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

    @Transactional
    public Appointment update(Long id, @Valid UpdateAppointmentDto updateAppointmentDto) {
        Appointment appointment = this.findById(id);

        boolean shouldReassignDoctor = false;

        // Actualizar tipo de cita si se proporciona
        if (updateAppointmentDto.getAppointmentTypeId() != null) {
            appointment.setAppointmentType(appointmentTypeService.findById(updateAppointmentDto.getAppointmentTypeId()));
            shouldReassignDoctor = true;
        }

        // Actualizar la hora de inicio y calcular la hora de finalización
        if (updateAppointmentDto.getStartTime() != null) {
            appointment.setStartTime(updateAppointmentDto.getStartTime());
            appointment.setEndTime(calculateEndTime(appointment));
            appointment.setStatus(AppointmentStatus.RE_SCHEDULED);
            shouldReassignDoctor = shouldReassignDoctor || !isDoctorAvailable(appointment.getDoctor(),
                    appointment.getStartTime(),
                    appointment.getEndTime());
        }

        // Reasignar doctor si es necesario
        if (shouldReassignDoctor) {
            appointment.setDoctor(getAppointmentDoctor(appointment));
        }

        // Envío asíncrono de notificaciones
        this.emailService.sendUpdateNotificationsAsync(appointment, shouldReassignDoctor);

        // Actualizar notas si se proporcionan
        if (updateAppointmentDto.getNotes() != null) {
            appointment.setNotes(updateAppointmentDto.getNotes());
        }

        return appointmentRepository.save(appointment);
    }

    private static CreateReminderDto getCreateReminderDto(Appointment appointment, boolean shouldReassignDoctor) {
        CreateReminderDto createReminderDto = new CreateReminderDto();
        createReminderDto.setTitle("Cita asignada");
        createReminderDto.setAppointmentId(appointment.getId());
        createReminderDto.setMessage(shouldReassignDoctor ?
                "Se le ha asignado una nueva cita. Por favor, revise su calendario." :
                "Su cita ha sido reprogramada. Por favor, revise su calendario.");
        createReminderDto.setReceiverId(appointment.getDoctor().getId());
        createReminderDto.setReminderType(ReminderType.APPOINTMENT_REMINDER);
        return createReminderDto;
    }

    private LocalDateTime calculateEndTime(Appointment appointment) {
        int duration = appointment.getAppointmentType().getDurationInMinutes();
        int bufferMinutes = 20; // Tiempo de descanso o margen
        return appointment.getStartTime().plusMinutes(duration + bufferMinutes);
    }

    public void delete(Long id) {
        // Se puede hacer un update a status cancelado
        this.findById(id);
        appointmentRepository.deleteById(id);
    }

    private User getAppointmentDoctor(Appointment appointment) {
        Long specialtyId = appointment.getAppointmentType().getSpecialty().getId();
        System.out.println("Specialty ID: " + specialtyId);
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
            if (availability.isRecurring()) {
                // Si es recurrente, verificar si el día de la semana coincide
                if (startTime.getDayOfWeek() == availability.getDayOfWeek()) {
                    // Verificar si el horario de la cita está dentro del horario de disponibilidad recurrente
                    LocalDateTime availabilityStart = startTime.with(availability.getStartTime().toLocalTime());
                    LocalDateTime availabilityEnd = startTime.with(availability.getEndTime().toLocalTime());

                    if (startTime.isBefore(availabilityEnd) && endTime.isAfter(availabilityStart)) {
                        return false; // El doctor no está disponible en este horario recurrente
                    }
                }
            } else {
                // Si no es recurrente, verificar si la fecha y hora coinciden exactamente
                if (startTime.isBefore(availability.getEndTime()) && endTime.isAfter(availability.getStartTime())) {
                    return false; // El doctor no está disponible en este horario no recurrente
                }
            }
        }

        // Validar indisponibilidad por unavailability
        List<Unavailability> unavailabilities = unavailabilityService.findByDoctorId(doctor.getId());
        for (Unavailability unavailability : unavailabilities) {
            if (startTime.isBefore(unavailability.getEndTime()) && endTime.isAfter(unavailability.getStartTime())) {
                return false; // El doctor tiene una indisponibilidad en este horario
            }
        }

        // Validar citas existentes
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndStatusNotIn(doctor.getId(), Arrays.asList(AppointmentStatus.CANCELLED_BY_DOCTOR,
                        AppointmentStatus.CANCELLED_BY_PATIENT,
                        AppointmentStatus.NOT_SHOW,
                        AppointmentStatus.COMPLETED));
        for (Appointment appointment : appointments) {
            if (startTime.isBefore(appointment.getEndTime()) && endTime.isAfter(appointment.getStartTime())) {
                return false; // El doctor ya tiene una cita en este horario
            }
        }

        return true; // El doctor está disponible
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

    public List<Map<String, Object>> findAvailableAppointments(Long appointmentTypeId, LocalDateTime startRange, LocalDateTime endRange) {
        // Obtener la duración de la cita basada en el tipo de cita
        AppointmentType appointmentType = appointmentTypeService.findById(appointmentTypeId);
        int appointmentDuration = appointmentType.getDurationInMinutes();

        // Obtener todos los doctores de la especialidad asociada al tipo de cita
        List<User> doctors = userService.findBySpecialtyId(appointmentType.getSpecialty().getId());

        // Lista para almacenar las citas disponibles con información completa
        List<Map<String, Object>> availableAppointments = new ArrayList<>();

        // Iterar sobre cada doctor
        for (User doctor : doctors) {
            // Obtener las disponibilidades del doctor
            List<Availability> availabilities = availabilityService.findByDoctorId(doctor.getId());

            // Obtener las citas ya agendadas del doctor
            List<Appointment> appointments = appointmentRepository.findByDoctorIdAndStatusNotIn(
                    doctor.getId(),
                    Arrays.asList(AppointmentStatus.CANCELLED_BY_DOCTOR, AppointmentStatus.CANCELLED_BY_PATIENT, AppointmentStatus.COMPLETED)
            );

            // Generar slots de tiempo disponibles para el doctor
            for (Availability availability : availabilities) {
                LocalDateTime slotStartTime = availability.getStartTime();
                LocalDateTime slotEndTime = availability.getEndTime();

                // Si la disponibilidad es recurrente, generar slots para cada semana en el rango de fechas
                if (availability.isRecurring()) {
                    LocalDateTime currentSlotStart = slotStartTime;
                    while (currentSlotStart.isBefore(endRange)) {
                        // Verificar si el slot está dentro del rango de fechas solicitado
                        if (currentSlotStart.isAfter(startRange) || currentSlotStart.isEqual(startRange)) {
                            // Verificar si el slot está disponible (no coincide con una cita agendada)
                            if (isSlotAvailable(currentSlotStart, currentSlotStart.plusMinutes(appointmentDuration), appointments)) {
                                // Crear un objeto con la información completa
                                Map<String, Object> appointmentInfo = new HashMap<>();
                                appointmentInfo.put("doctorId", doctor.getId());
                                appointmentInfo.put("doctorName", doctor.getFullName());
                                appointmentInfo.put("specialty", appointmentType.getSpecialty().getName()); // Obtener la especialidad del tipo de cita
                                appointmentInfo.put("appointmentTypeId", appointmentType.getId()); // ID del tipo de cita
                                appointmentInfo.put("appointmentType", appointmentType.getName());
                                appointmentInfo.put("availableDateTime", currentSlotStart);

                                // Agregar a la lista de citas disponibles
                                availableAppointments.add(appointmentInfo);
                            }
                        }
                        // Mover al siguiente slot recurrente (1 semana después)
                        currentSlotStart = currentSlotStart.plusWeeks(1);
                    }
                } else {
                    // Si no es recurrente, verificar si el slot está dentro del rango de fechas
                    if (slotStartTime.isAfter(startRange) || slotStartTime.isEqual(startRange)) {
                        if (slotStartTime.isBefore(endRange) && isSlotAvailable(slotStartTime, slotEndTime, appointments)) {
                            // Crear un objeto con la información completa
                            Map<String, Object> appointmentInfo = new HashMap<>();
                            appointmentInfo.put("doctorId", doctor.getId());
                            appointmentInfo.put("doctorName", doctor.getFullName());
                            appointmentInfo.put("specialty", appointmentType.getSpecialty().getName()); // Obtener la especialidad del tipo de cita
                            appointmentInfo.put("appointmentTypeId", appointmentType.getId()); // ID del tipo de cita
                            appointmentInfo.put("appointmentType", appointmentType.getName());
                            appointmentInfo.put("availableDateTime", slotStartTime);

                            // Agregar a la lista de citas disponibles
                            availableAppointments.add(appointmentInfo);
                        }
                    }
                }
            }
        }

        // Ordenar las citas disponibles por fecha y hora
        availableAppointments.sort((a1, a2) -> {
            LocalDateTime dateTime1 = (LocalDateTime) a1.get("availableDateTime");
            LocalDateTime dateTime2 = (LocalDateTime) a2.get("availableDateTime");
            return dateTime1.compareTo(dateTime2);
        });

        return availableAppointments;
    }

    private boolean isSlotAvailable(LocalDateTime slotStartTime, LocalDateTime slotEndTime, List<Appointment> appointments) {
        // Verificar si el slot coincide con una cita ya agendada
        for (Appointment appointment : appointments) {
            if (slotStartTime.isBefore(appointment.getEndTime()) && slotEndTime.isAfter(appointment.getStartTime())) {
                return false; // El slot no está disponible
            }
        }
        return true; // El slot está disponible
    }
}
