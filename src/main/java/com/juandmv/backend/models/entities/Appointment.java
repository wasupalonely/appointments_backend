package com.juandmv.backend.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "appointments")
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date startTime;

    private Date endTime;

    // Esto se puede cambiar en un futuro a un enum
    private boolean status = true;

    private String notes;

    private Date createdAt = new Date();

    private Date updatedAt = new Date();

    @ManyToOne
    @JoinColumn(name = "patientId", referencedColumnName = "id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctorId", referencedColumnName = "id")
    private User doctor;

    // TODO: Añadir relaciones con tipo de cita

}
