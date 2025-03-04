package com.juandmv.backend.models.entities;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.Date;

@Entity
@Table(name = "availabilities")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "doctorId", referencedColumnName = "id")
    public User doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private Date startTime;

    @Column(nullable = false)
    private Date endTime;

    @Column(nullable = false)
    private boolean isRecurring;
}
