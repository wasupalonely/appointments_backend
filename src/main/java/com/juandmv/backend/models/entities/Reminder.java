package com.juandmv.backend.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appointmentId", referencedColumnName = "id")
    private Appointment appointment;

    // TODO: Make this an enum
    @Column(nullable = false)
    private String reminderType;

    private Date sentAt = new Date();

    // TODO: Validate the type of this!
    private boolean status;

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
