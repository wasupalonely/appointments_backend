package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.entities.Specialty;
import com.juandmv.backend.repositories.SpecialtyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialtyService {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    public List<Specialty> findAll() { return specialtyRepository.findAll(); }

    public Specialty findById(Long id) {
        return specialtyRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada"));
    }

    public Specialty save(Specialty specialty) { return specialtyRepository.save(specialty); }
}
