package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.InvalidDatesRangeException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateAvailabilityDto;
import com.juandmv.backend.models.entities.Availability;
import com.juandmv.backend.repositories.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    public List<Availability> findAll() { return availabilityRepository.findAll(); }

    public Availability findById(Long id) { return availabilityRepository.findById(id)
            .orElseThrow(
                    () -> new ResourceNotFoundException("Disponibilidad no encontrada"));
    }

    public List<Availability> findByDoctorId(Long doctorId) { return availabilityRepository.findByDoctorId(doctorId); }

    public Availability save(CreateAvailabilityDto createAvailabilityDto) {
        if (createAvailabilityDto.getEndTime().isBefore(createAvailabilityDto.getStartTime()) ||
                createAvailabilityDto.getStartTime().isAfter(createAvailabilityDto.getEndTime())) {
            throw new InvalidDatesRangeException("La fecha de finalización debe ser posterior a la de inicio");
        }
        Availability availability = new Availability();
        availability.setDayOfWeek(createAvailabilityDto.getDayOfWeek());
        availability.setStartTime(createAvailabilityDto.getStartTime());
        availability.setEndTime(createAvailabilityDto.getEndTime());
        availability.setRecurring(createAvailabilityDto.isRecurring());

        return availabilityRepository.save(availability);
    }
}
