package com.juandmv.backend.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.juandmv.backend.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "appointments")
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String notes;

    private Date createdAt = new Date();

    private Date updatedAt = new Date();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    private String cancellationReason;

    private LocalDateTime cancelledAt;

    @ManyToOne
    @JoinColumn(name = "cancelledByUserId")
    private User cancelledBy;

    @ManyToOne
    @JoinColumn(name = "patientId", referencedColumnName = "id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "physicalLocationId", referencedColumnName = "id")
    private PhysicalLocation physicalLocation;

    @ManyToOne
    @JoinColumn(name = "doctorId", referencedColumnName = "id")
    private User doctor;

    @ManyToOne
    @JoinColumn(name = "appointmentTypeId", referencedColumnName = "id")
    private AppointmentType appointmentType;

    @ManyToOne
    @JoinColumn(name = "parentAppointmentId", referencedColumnName = "id")
    @JsonBackReference
    private Appointment parentAppointment; // Cita principal (padre)

    @OneToMany(mappedBy = "parentAppointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Appointment> derivedAppointments = new ArrayList<>();

}
