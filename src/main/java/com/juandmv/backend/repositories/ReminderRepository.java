package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByAppointmentId(Long id);
}
