package com.juandmv.backend.services;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.exceptions.ScheduleConflictException;
import com.juandmv.backend.models.dto.CreateAppointmentDto;
import com.juandmv.backend.models.dto.UpdateAppointmentDto;
import com.juandmv.backend.models.entities.*;
import com.juandmv.backend.repositories.AppointmentRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
            // TODO: Si el doctor es el mismo, se envía notificación al doctor de que se ha cambiado la cita
            shouldReassignDoctor = shouldReassignDoctor || !isDoctorAvailable(appointment.getDoctor(),
                    appointment.getStartTime(),
                    appointment.getEndTime());
        }

        // Reasignar doctor si es necesario
        if (shouldReassignDoctor) {
            // TODO: Se envía notificación al doctor nuevo
            appointment.setDoctor(getAppointmentDoctor(appointment));
        }

        // Actualizar notas si se proporcionan
        if (updateAppointmentDto.getNotes() != null) {
            appointment.setNotes(updateAppointmentDto.getNotes());
        }

        return appointmentRepository.save(appointment);
    }

    private LocalDateTime calculateEndTime(Appointment appointment) {
        int duration = appointment.getAppointmentType().getDurationInMinutes();
        int bufferMinutes = 20; // Tiempo de descanso o margen
        return appointment.getStartTime().plusMinutes(duration + bufferMinutes);
    }

//    @Transactional
//    public Appointment update(Long id, @Valid UpdateAppointmentDto updateAppointmentDto) {
//        Appointment appointment = this.findById(id);
//
//        if (updateAppointmentDto.getStartTime() != null && updateAppointmentDto.getAppointmentTypeId() != null) {
//            // Se tiene que buscar otro doctor disponible
//            // Setear la cita como reprogramada
//            appointment.setStartTime(updateAppointmentDto.getStartTime());
//            appointment.setEndTime(updateAppointmentDto
//                    .getStartTime()
//                    .plusMinutes(appointmentTypeService
//                            .findById(appointment.getAppointmentType().getId())
//                            .getDurationInMinutes() + 20));
//            appointment.setStatus(AppointmentStatus.RE_SCHEDULED);
//
//            // Setear el nueva especialidad, por eso se tiene que cambiar el doctor
//            appointment.setAppointmentType(appointmentTypeService.findById(updateAppointmentDto.getAppointmentTypeId()));
//            User newDoctor = getAppointmentDoctor(appointment);
//            appointment.setDoctor(newDoctor);
//
//        } else if (updateAppointmentDto.getStartTime() != null) {
//            appointment.setStartTime(updateAppointmentDto.getStartTime());
//            appointment.setEndTime(updateAppointmentDto
//                    .getStartTime()
//                    .plusMinutes(appointmentTypeService
//                            .findById(appointment.getAppointmentType().getId())
//                            .getDurationInMinutes() + 20));
//            appointment.setStatus(AppointmentStatus.RE_SCHEDULED);
//
//            if (!isDoctorAvailable(appointment.getDoctor(),
//                    updateAppointmentDto.getStartTime(),
//                    updateAppointmentDto
//                            .getStartTime()
//                            .plusMinutes(appointmentTypeService
//                                    .findById(appointment.getAppointmentType().getId())
//                                    .getDurationInMinutes())))
//            {
//                User newDoctor = getAppointmentDoctor(appointment);
//                appointment.setDoctor(newDoctor);
//            }
//        } else if (updateAppointmentDto.getAppointmentTypeId() != null) {
//            User doctor = getAppointmentDoctor(appointment);
//            appointment.setDoctor(doctor);
//        }
//
//        appointment.setNotes(updateAppointmentDto.getNotes() != null ?
//                updateAppointmentDto.getNotes() :
//                appointment.getNotes());
//
//        return appointmentRepository.save(appointment);
//    }

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
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndStatusNotIn(doctor.getId(), Arrays.asList(AppointmentStatus.CANCELLED_BY_DOCTOR,
                        AppointmentStatus.CANCELLED_BY_PATIENT,
                        AppointmentStatus.NOT_SHOW,
                        AppointmentStatus.COMPLETED));
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

    public Map<String, Object> cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = this.findById(appointmentId);
        User user = userService.findById(userId);
        appointment.setStatus(user.getSpecialty() != null
                ? AppointmentStatus.CANCELLED_BY_DOCTOR :
                AppointmentStatus.CANCELLED_BY_PATIENT);

        if (user.getSpecialty() != null) {
            User newDoctor = getAppointmentDoctor(appointment);
            appointment.setDoctor(newDoctor);
        }

        Map<String, Object> body = new HashMap<>();

        this.appointmentRepository.save(appointment);

        body.put("appointment", appointment);
        if (user.getSpecialty() != null) {
            // TODO: Se envía notificación al paciente
            body.put("message", "La cita ha sido cancelada por el doctor, se le asignara un nuevo doctor");
        } else {
            // TODO: Se envía notificación al doctor y al paciente
            body.put("message", "La cita ha sido cancelada por el paciente");
        }

        return body;
    }
}
