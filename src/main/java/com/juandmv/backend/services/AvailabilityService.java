package com.juandmv.backend.services;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.exceptions.InvalidDatesRangeException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.AvailableSlotDto;
import com.juandmv.backend.models.dto.CreateAvailabilityDto;
import com.juandmv.backend.models.entities.*;
import com.juandmv.backend.models.helpers.TimeSlot;
import com.juandmv.backend.repositories.AppointmentRepository;
import com.juandmv.backend.repositories.AvailabilityRepository;
import com.juandmv.backend.repositories.UnavailabilityRepository;
import com.juandmv.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class AvailabilityService {
    private static final int BUFFER_TIME_MINUTES = 20;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private UnavailabilityRepository unavailabilityRepository;

    @Autowired
    private UserService userService;


    public List<Availability> findAll() { return availabilityRepository.findAll(); }

    public Availability findById(Long id) { return availabilityRepository.findById(id)
            .orElseThrow(
                    () -> new ResourceNotFoundException("Disponibilidad no encontrada"));
    }

    public List<Availability> findByDoctorId(Long doctorId) { return availabilityRepository.findByDoctorId(doctorId); }

    public Availability save(CreateAvailabilityDto createAvailabilityDto) {
        User doctor = userService.findById(createAvailabilityDto.getDoctorId());
        if (createAvailabilityDto.getEndTime().isBefore(createAvailabilityDto.getStartTime()) ||
                createAvailabilityDto.getStartTime().isAfter(createAvailabilityDto.getEndTime())) {
            throw new InvalidDatesRangeException("La fecha de finalización debe ser posterior a la de inicio");
        }
        Availability availability = new Availability();
        availability.setDayOfWeek(createAvailabilityDto.getDayOfWeek());
        availability.setStartTime(createAvailabilityDto.getStartTime());
        availability.setEndTime(createAvailabilityDto.getEndTime());
        availability.setSpecificDate(createAvailabilityDto.getSpecificDate() != null ? createAvailabilityDto.getSpecificDate() : null);
        availability.setRecurring(createAvailabilityDto.isRecurring());
        availability.setDoctor(doctor);

        return availabilityRepository.save(availability);
    }

    /**
     * Encuentra todos los slots disponibles para un médico en un período especificado
     * basado en el tipo de cita seleccionado.
     *
     * @param doctorId ID del médico
     * @param appointmentType Tipo de cita con su duración
     * @param startDate Fecha inicial del período
     * @param endDate Fecha final del período
     * @return Lista de slots disponibles
     */
    public List<AvailableSlotDto> findAvailableSlots(
            Long doctorId,
            AppointmentType appointmentType,
            LocalDate startDate,
            LocalDate endDate) {

        // Duración del tipo de cita en minutos
        int appointmentDuration = appointmentType.getDurationInMinutes();

        // Paso 1: Obtener todas las disponibilidades del médico en el período
        List<Availability> availabilities = getAvailabilitiesForPeriod(doctorId, startDate, endDate);

        // Paso 2: Obtener todas las indisponibilidades del médico en el período
        List<Unavailability> unavailabilities = unavailabilityRepository.findByDoctorIdAndDateRange(
                doctorId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        // Paso 3: Obtener todas las citas programadas del médico en el período
        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndDateRangeAndStatus(
                doctorId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                AppointmentStatus.PENDING
        );

        // Paso 4: Generar los slots disponibles
        return generateAvailableSlots(availabilities, unavailabilities, bookedAppointments, appointmentDuration, startDate, endDate);
    }

    /**
     * Obtiene todas las disponibilidades aplicables para un médico en un período específico
     */
    private List<Availability> getAvailabilitiesForPeriod(Long doctorId, LocalDate startDate, LocalDate endDate) {
        // Obtener disponibilidades recurrentes (se aplican a cualquier fecha en el rango)
        List<Availability> recurringAvailabilities = availabilityRepository.findByDoctorIdAndIsRecurring(doctorId, true);

        // Obtener disponibilidades no recurrentes específicas para las fechas en el rango
        List<Availability> nonRecurringAvailabilities = availabilityRepository.findByDoctorIdAndIsRecurringAndSpecificDateBetween(
                doctorId, false, startDate, endDate
        );

        // Combinar ambas listas
        List<Availability> allAvailabilities = new ArrayList<>();
        allAvailabilities.addAll(recurringAvailabilities);
        allAvailabilities.addAll(nonRecurringAvailabilities);

        return allAvailabilities;
    }

    /**
     * Genera slots disponibles basado en disponibilidades, indisponibilidades y citas ya programadas
     */
    private List<AvailableSlotDto> generateAvailableSlots(
            List<Availability> availabilities,
            List<Unavailability> unavailabilities,
            List<Appointment> bookedAppointments,
            int appointmentDurationMinutes,
            LocalDate startDate,
            LocalDate endDate) {

        List<AvailableSlotDto> availableSlots = new ArrayList<>();

        // Para cada día en el período solicitado
        Map<LocalDate, List<TimeSlot>> availableSlotsPerDay = new HashMap<>();

        // Paso 1: Generar todos los slots potenciales de disponibilidad
        for (Availability availability : availabilities) {
            List<LocalDate> applicableDates = getApplicableDates(availability, startDate, endDate);

            for (LocalDate date : applicableDates) {
                LocalTime startTime = availability.getStartTime();
                LocalTime endTime = availability.getEndTime();

                // Crear un TimeSlot para este rango
                TimeSlot timeSlot = new TimeSlot(
                        date.atTime(startTime),
                        date.atTime(endTime)
                );

                // Agregar el slot a la lista de slots para esta fecha
                availableSlotsPerDay.computeIfAbsent(date, k -> new ArrayList<>())
                        .add(timeSlot);
            }
        }

        // Paso 2: Restar las indisponibilidades
        for (Unavailability unavailability : unavailabilities) {
            LocalDate date = unavailability.getStartTime().toLocalDate();
            List<TimeSlot> daySlots = availableSlotsPerDay.get(date);

            if (daySlots != null) {
                List<TimeSlot> updatedSlots = new ArrayList<>();

                for (TimeSlot slot : daySlots) {
                    // Verificar si hay solapamiento con la indisponibilidad
                    if (slot.overlaps(unavailability.getStartTime(), unavailability.getEndTime())) {
                        // Dividir el slot si es necesario
                        List<TimeSlot> splitSlots = slot.removeOverlap(
                                unavailability.getStartTime(),
                                unavailability.getEndTime()
                        );
                        updatedSlots.addAll(splitSlots);
                    } else {
                        updatedSlots.add(slot);
                    }
                }

                availableSlotsPerDay.put(date, updatedSlots);
            }
        }

        // Paso 3: Restar las citas ya programadas
        for (Appointment appointment : bookedAppointments) {
            LocalDate date = appointment.getStartTime().toLocalDate();
            List<TimeSlot> daySlots = availableSlotsPerDay.get(date);

            if (daySlots != null) {
                List<TimeSlot> updatedSlots = new ArrayList<>();

                for (TimeSlot slot : daySlots) {
                    // Verificar si hay solapamiento con la cita
                    if (slot.overlaps(appointment.getStartTime(), appointment.getEndTime())) {
                        // Dividir el slot si es necesario
                        List<TimeSlot> splitSlots = slot.removeOverlap(
                                appointment.getStartTime(),
                                appointment.getEndTime()
                        );
                        updatedSlots.addAll(splitSlots);
                    } else {
                        updatedSlots.add(slot);
                    }
                }

                availableSlotsPerDay.put(date, updatedSlots);
            }
        }

        // Paso 4: Dividir los slots restantes en intervalos según la duración de la cita
        Duration appointmentDuration = Duration.ofMinutes(appointmentDurationMinutes);

        for (Map.Entry<LocalDate, List<TimeSlot>> entry : availableSlotsPerDay.entrySet()) {
            LocalDate date = entry.getKey();
            List<TimeSlot> daySlots = entry.getValue();

            for (TimeSlot slot : daySlots) {
                LocalDateTime currentStart = slot.getStart();
                LocalDateTime slotEnd = slot.getEnd();

                Duration totalDuration = appointmentDuration.plus(Duration.ofMinutes(BUFFER_TIME_MINUTES));

                // Crear slots de duración fija mientras haya suficiente tiempo disponible
                while (currentStart.plus(appointmentDuration).isBefore(slotEnd) ||
                        currentStart.plus(appointmentDuration).isEqual(slotEnd)) {

                    LocalDateTime slotStartTime = currentStart;
                    LocalDateTime slotEndTime = currentStart.plus(appointmentDuration);

                    // Crear DTO para slot disponible
                    AvailableSlotDto availableSlot = new AvailableSlotDto(
                            slotStartTime,
                            slotEndTime
                    );

                    availableSlots.add(availableSlot);

                    // Avanzar al siguiente slot potencial, incluyendo el tiempo buffer
                    currentStart = currentStart.plus(totalDuration);
                }
            }
        }

        // Ordenar los slots por fecha/hora
        availableSlots.sort(Comparator.comparing(AvailableSlotDto::getStartTime));

        return availableSlots;
    }

    /**
     * Obtiene las fechas aplicables para una disponibilidad dentro del período
     */
    private List<LocalDate> getApplicableDates(Availability availability, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();

        if (!availability.isRecurring()) {
            // Para disponibilidades no recurrentes, solo hay una fecha específica
            if (availability.getSpecificDate() != null) {
                dates.add(availability.getSpecificDate());
            }
        } else {
            // Para disponibilidades recurrentes, encontrar todas las fechas que coinciden
            // con el día de la semana especificado en el período
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                if (date.getDayOfWeek() == availability.getDayOfWeek()) {
                    dates.add(date);
                }
                date = date.plusDays(1);
            }
        }

        return dates;
    }

    public void delete(Long id) {
        this.findById(id);
        availabilityRepository.deleteById(id);
    }
}
