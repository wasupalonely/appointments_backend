package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {

    List<AppointmentType> findBySpecialty_Id(Long specialtyId);

    List<AppointmentType> findByName(String name);

    long countBySpecialtyId(Long specialtyId);
}
