package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.InvalidDatesRangeException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateAvailabilityDto;
import com.juandmv.backend.models.dto.CreateUnavailabilityDto;
import com.juandmv.backend.models.entities.Availability;
import com.juandmv.backend.models.entities.Unavailability;
import com.juandmv.backend.repositories.UnavailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnavailabilityService {

    @Autowired
    private UnavailabilityRepository unavailabilityRepository;

    public List<Unavailability> findAll() { return unavailabilityRepository.findAll(); }

    public Unavailability findById(Long id) { return unavailabilityRepository.findById(id)
            .orElseThrow(
                    () -> new ResourceNotFoundException("Indisponibilidad no encontrada"));
    }

    public List<Unavailability> findByDoctorId(Long doctorId) { return unavailabilityRepository.findByDoctorId(doctorId); }

    public Unavailability save(CreateUnavailabilityDto createUnavailabilityDto) {
        if (createUnavailabilityDto.getEndTime().isBefore(createUnavailabilityDto.getStartTime()) ||
                createUnavailabilityDto.getStartTime().isAfter(createUnavailabilityDto.getEndTime())) {
            throw new InvalidDatesRangeException("La fecha de finalización debe ser posterior a la de inicio");
        }
        Unavailability unavailability = new Unavailability();
        unavailability.setStartTime(createUnavailabilityDto.getStartTime());
        unavailability.setEndTime(createUnavailabilityDto.getEndTime());
        unavailability.setReason(createUnavailabilityDto.getReason());

        return unavailabilityRepository.save(unavailability);
    }

    public void delete(Long id) {
        this.findById(id);
        unavailabilityRepository.deleteById(id);
    }
}
