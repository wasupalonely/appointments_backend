package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreatePhysicalLocationDto;
import com.juandmv.backend.models.entities.PhysicalLocation;
import com.juandmv.backend.repositories.PhysicalLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhysicalLocationService {

    @Autowired
    private PhysicalLocationRepository physicalLocationRepository;

    public List<PhysicalLocation> findAll() { return physicalLocationRepository.findAll(); }

    public PhysicalLocation findById(Long id) {
        return physicalLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada"));
    }

    public PhysicalLocation save(CreatePhysicalLocationDto physicalLocation) {
        PhysicalLocation newPhysicalLocation = new PhysicalLocation();
        newPhysicalLocation.setName(physicalLocation.getName());
        newPhysicalLocation.setAddress(physicalLocation.getAddress());
        return physicalLocationRepository.save(newPhysicalLocation);
    }
}
