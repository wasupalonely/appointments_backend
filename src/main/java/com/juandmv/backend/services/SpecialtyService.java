package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.BadRequestException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.mappers.SpecialtyMapper;
import com.juandmv.backend.models.dto.CreateSpecialtyDto;
import com.juandmv.backend.models.dto.UpdateSpecialtyDto;
import com.juandmv.backend.models.entities.Specialty;
import com.juandmv.backend.repositories.AppointmentTypeRepository;
import com.juandmv.backend.repositories.SpecialtyRepository;
import com.juandmv.backend.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialtyService {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private SpecialtyMapper specialtyMapper;

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Specialty> findAll() { return specialtyRepository.findAll(); }

    public Specialty findById(Long id) {
        return specialtyRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada"));
    }

    public Specialty save(CreateSpecialtyDto specialty) {
        Specialty newSpecialty = new Specialty();
        newSpecialty.setName(specialty.getName());
        newSpecialty.setDescription(specialty.getDescription());
        return specialtyRepository.save(newSpecialty);
    }

    public Specialty update(Long id, @Valid UpdateSpecialtyDto specialty) {
        Specialty specialtyToUpdate = this.findById(id);

        specialtyMapper.updateSpecialtyFromDto(specialty, specialtyToUpdate);

        return specialtyRepository.save(specialtyToUpdate);
    }

    public void delete(Long id) {
        Specialty specialty = this.findById(id);

        if (appointmentTypeRepository.countBySpecialtyId(id) > 0 || userRepository.countBySpecialtyId(id) > 0) {
            throw new BadRequestException("La especialidad tiene tipos de citas o doctores asociadas y no puede ser eliminada");
        }

        specialtyRepository.delete(specialty);
    }
}
