package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.Availability;
import com.juandmv.backend.models.entities.Unavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UnavailabilityRepository extends JpaRepository<Unavailability, Long> {

    List<Unavailability> findByDoctorId(Long doctorId);

    @Query("SELECT u FROM Unavailability u WHERE u.doctor.id = :doctorId " +
            "AND ((u.startTime BETWEEN :startTime AND :endTime) " +
            "OR (u.endTime BETWEEN :startTime AND :endTime) " +
            "OR (:startTime BETWEEN u.startTime AND u.endTime))")
    List<Unavailability> findByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
