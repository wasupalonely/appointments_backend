package com.juandmv.backend.repositories;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.models.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findAll(Pageable pageable);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndStatusNotIn(Long doctorId, List<AppointmentStatus> statuses);

    List<Appointment> findByPhysicalLocationId(Long physicalLocationId);

    List<Appointment> findByParentAppointmentId(Long parentAppointmentId);

    int countByDoctorId(Long doctorId);

    @Query("SELECT a FROM Appointment a WHERE " +
            "(CAST(:startDate AS timestamp) IS NULL OR a.startTime >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR a.endTime <= :endDate) AND " +
            "(:status IS NULL OR a.status = :status)")
    Page<Appointment> findAppointmentsByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") AppointmentStatus status,
            Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND ((a.startTime BETWEEN :startTime AND :endTime) " +
            "OR (a.endTime BETWEEN :startTime AND :endTime) " +
            "OR (:startTime BETWEEN a.startTime AND a.endTime))")
    List<Appointment> findByDoctorIdAndDateRangeAndStatus(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") AppointmentStatus status
    );

    long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);
}