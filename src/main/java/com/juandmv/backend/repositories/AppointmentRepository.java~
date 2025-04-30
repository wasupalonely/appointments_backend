package com.juandmv.backend.repositories;

import com.juandmv.backend.enums.AppointmentStatus;
import com.juandmv.backend.models.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndStatusNotIn(Long doctorId, List<AppointmentStatus> statuses);

    List<Appointment> findByPhysicalLocationId(Long physicalLocationId);

    List<Appointment> findByParentAppointmentId(Long parentAppointmentId);

    int countByDoctorId(Long doctorId);

    @Query("SELECT a FROM Appointment a WHERE " +
            "(:startDate IS NULL OR a.startTime >= :startDate) AND " +
            "(:endDate IS NULL OR a.endTime <= :endDate) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:physicalLocationId IS NULL OR a.physicalLocation.id = :physicalLocationId)")
    List<Appointment> findAppointmentsByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") AppointmentStatus status,
            @Param("physicalLocationId") Long physicalLocationId);
}
