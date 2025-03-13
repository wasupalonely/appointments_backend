package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateAppointmentTypeDto;
import com.juandmv.backend.models.entities.AppointmentType;
import com.juandmv.backend.models.entities.Specialty;
import com.juandmv.backend.repositories.AppointmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentTypeService {

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @Autowired
    private SpecialtyService specialtyService;

    public List<AppointmentType> findAll() { return appointmentTypeRepository.findAll(); }

    public List<AppointmentType> findBySpecialty(Long specialtyId) { return appointmentTypeRepository.findBySpecialty_Id(specialtyId); }

    public AppointmentType findById(Long id) {
        return appointmentTypeRepository
            .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de cita no encontrada"));
    }

    public AppointmentType save(CreateAppointmentTypeDto appointmentType) {
        AppointmentType newAppointmentType = new AppointmentType();
        Specialty specialty = this.specialtyService.findById(appointmentType.getSpecialtyId());

        newAppointmentType.setName(appointmentType.getName());
        newAppointmentType.setDurationInMinutes(appointmentType.getDurationInMinutes());
        newAppointmentType.setSpecialty(specialty);
        newAppointmentType.setIsGeneral(appointmentType.getIsGeneral());

        return appointmentTypeRepository.save(newAppointmentType);
    }

    // TODO: Implementar actualización
//    public AppointmentType update(Long id, CreateAppointmentTypeDto appointmentType) {
//        AppointmentType updateAppointmentType = this.findById(id);
//
//        updateAppointmentType.setTitle(appointmentType.getTitle());
//
//        return appointmentTypeRepository.save(updateAppointmentType);
//    }

    public void delete(Long id) {
        this.findById(id);
        appointmentTypeRepository.deleteById(id);
    }
}
