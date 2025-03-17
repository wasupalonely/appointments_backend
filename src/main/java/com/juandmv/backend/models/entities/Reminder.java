package com.juandmv.backend.models.entities;

import com.juandmv.backend.enums.ReminderType;
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

    @ManyToOne
    @JoinColumn(name = "receiverId", referencedColumnName = "id")
    private User receiver;

    @Column(nullable = false)
    private ReminderType reminderType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    private Date sentAt = new Date();

    private boolean isRead = false;

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
