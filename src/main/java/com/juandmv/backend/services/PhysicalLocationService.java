package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.BadRequestException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.mappers.PhysicalLocationMapper;
import com.juandmv.backend.models.dto.CreatePhysicalLocationDto;
import com.juandmv.backend.models.dto.UpdatePhysicalLocationDto;
import com.juandmv.backend.models.entities.PhysicalLocation;
import com.juandmv.backend.repositories.PhysicalLocationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhysicalLocationService {

    @Autowired
    private PhysicalLocationRepository physicalLocationRepository;

    @Autowired
    private PhysicalLocationMapper physicalLocationMapper;

    public List<PhysicalLocation> findAll() { return physicalLocationRepository.findAll(); }

    public PhysicalLocation findById(Long id) {
        return physicalLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada"));
    }

    public PhysicalLocation save(@Valid CreatePhysicalLocationDto physicalLocation) {
        return physicalLocationRepository.save(physicalLocationMapper.toEntity(physicalLocation));
    }

    @Transactional
    public PhysicalLocation update(Long id, @Valid UpdatePhysicalLocationDto physicalLocation) {
        PhysicalLocation location = findById(id);

        physicalLocationMapper.updatePhysicalLocationFromDto(physicalLocation, location);
        return physicalLocationRepository.save(location);
    }

    public void delete(Long id) {
        // Verificar que la ubicación no tenga nada asociado, de lo contrario que no pueda eliminar
        PhysicalLocation location = findById(id);

        if (!location.getAppointments().isEmpty() || !location.getUsers().isEmpty()) {
            throw new BadRequestException("La ubicación o doctores asociados y no puede ser eliminada");
        }
        physicalLocationRepository.delete(location);
    }
}
