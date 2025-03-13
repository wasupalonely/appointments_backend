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
    private String name;

    @Column(nullable = false)
    private Integer durationInMinutes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialtyId", referencedColumnName = "id")
    private Specialty specialty;

    @Column(nullable = false)
    private Boolean isGeneral;

    private Boolean isActive = true;

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
