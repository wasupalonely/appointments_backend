package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.ExamAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAssignmentRepository extends JpaRepository<ExamAssignment, Long> {

    List<ExamAssignment> findByPatientId(Long patientId);

    List<ExamAssignment> findByDoctorId(Long doctorId);

    List<ExamAssignment> findByExamType_Id(Long examTypeId);
}
