package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateAppointmentTypeDto;
import com.juandmv.backend.models.entities.AppointmentType;
import com.juandmv.backend.repositories.AppointmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentTypeService {

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    public List<AppointmentType> findAll() { return appointmentTypeRepository.findAll(); }

    public List<AppointmentType> findBySpecialty(String specialty) { return appointmentTypeRepository.findBySpecialty(specialty); }

    public AppointmentType findById(Long id) {
        return appointmentTypeRepository
            .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de cita no encontrada"));
    }

    public AppointmentType save(CreateAppointmentTypeDto appointmentType) {
        AppointmentType newAppointmentType = new AppointmentType();

        newAppointmentType.setTitle(appointmentType.getTitle());
        newAppointmentType.setIcon(appointmentType.getIcon());
        newAppointmentType.setSpecialty(appointmentType.getSpecialty());

        return appointmentTypeRepository.save(newAppointmentType);
    }

    public AppointmentType update(Long id, CreateAppointmentTypeDto appointmentType) {
        AppointmentType updateAppointmentType = this.findById(id);

        updateAppointmentType.setTitle(appointmentType.getTitle());
        updateAppointmentType.setIcon(appointmentType.getIcon());
        updateAppointmentType.setSpecialty(appointmentType.getSpecialty());

        return appointmentTypeRepository.save(updateAppointmentType);
    }

    public void delete(Long id) {
        this.findById(id);
        appointmentTypeRepository.deleteById(id);
    }
}
