package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.Availability;
import com.juandmv.backend.models.entities.Unavailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnavailabilityRepository extends JpaRepository<Unavailability, Long> {

    List<Unavailability> findByDoctorId(Long doctorId);
}
