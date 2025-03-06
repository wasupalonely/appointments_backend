package com.juandmv.backend.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "appointment_types")
@Getter
@Setter
@NoArgsConstructor
public class AppointmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String icon;

    // ¿Qué es esto?
    @Column(nullable = false)
    private String specialty;

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
