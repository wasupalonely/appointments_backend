package com.juandmv.backend.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "exam_assignments")
@Getter
@Setter
public class ExamAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "examTypeId", referencedColumnName = "id")
    private ExamType examType;

    @ManyToOne
    @JoinColumn(name = "patientId", referencedColumnName = "id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctorId", referencedColumnName = "id")
    private User doctor;

    // TODO: Revisar el tipo de dato de esto
    private boolean status = false;

    private Date completedAt;

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
