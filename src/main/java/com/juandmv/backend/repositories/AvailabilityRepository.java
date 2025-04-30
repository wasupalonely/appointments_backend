package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByDoctorId(Long doctorId);

    List<Availability> findByDoctorIdAndIsRecurring(Long doctorId, boolean isRecurring);

    List<Availability> findByDoctorIdAndIsRecurringAndSpecificDateBetween(
            Long doctorId,
            boolean isRecurring,
            LocalDate startDate,
            LocalDate endDate
    );
}
