package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPhysicalLocationId(Long physicalLocationId);

    List<Appointment> findByParentAppointmentId(Long parentAppointmentId);

    int countByDoctorId(Long doctorId);
}
