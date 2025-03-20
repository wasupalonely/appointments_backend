package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.InvalidDatesRangeException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateAvailabilityDto;
import com.juandmv.backend.models.entities.Availability;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.AvailabilityRepository;
import com.juandmv.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

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
        availability.setRecurring(createAvailabilityDto.isRecurring());
        availability.setDoctor(doctor);

        return availabilityRepository.save(availability);
    }

    public void delete(Long id) {
        this.findById(id);
        availabilityRepository.deleteById(id);
    }
}
