package com.juandmv.backend.services;

import com.juandmv.backend.models.dto.CreateExamAssignmentDto;
import com.juandmv.backend.models.entities.ExamAssignment;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.ExamAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamAssignmentService {

    @Autowired
    private ExamAssignmentRepository examAssignmentRepository;

    @Autowired
    private UserService userService;

    public List<ExamAssignment> findAll() { return examAssignmentRepository.findAll(); }

    public List<ExamAssignment> findByPatientId(Long patientId) {
        userService.findById(patientId);
        return examAssignmentRepository.findByPatientId(patientId);
    }

    public List<ExamAssignment> findByDoctorId(Long doctorId) {
        userService.findById(doctorId);
        return examAssignmentRepository.findByDoctorId(doctorId);
    }

    public ExamAssignment save(CreateExamAssignmentDto examAssignment) {
        User patient = userService.findById(examAssignment.getPatientId());
        User doctor = userService.findById(examAssignment.getDoctorId());
        ExamAssignment newAssignment = new ExamAssignment();
        newAssignment.setPatient(patient);
        newAssignment.setDoctor(doctor);
        newAssignment.setCompletedAt(null);
        newAssignment.setStatus(true);

        return examAssignmentRepository.save(newAssignment);
    }

    public void delete(Long id) {
        this.userService.findById(id);
        examAssignmentRepository.deleteById(id);
    }
}
