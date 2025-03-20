package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.mappers.ReminderMapper;
import com.juandmv.backend.models.dto.CreateReminderDto;
import com.juandmv.backend.models.dto.UpdateReminderDto;
import com.juandmv.backend.models.entities.Appointment;
import com.juandmv.backend.models.entities.Reminder;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.AppointmentRepository;
import com.juandmv.backend.repositories.ReminderRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ReminderMapper reminderMapper;

    public List<Reminder> findAll() { return this.reminderRepository.findAll(); }

    public List<Reminder> findByAppointmentId(Long id) { return this.reminderRepository.findByAppointmentId(id); }

    public List<Reminder> findByReceiverId(Long id) { return this.reminderRepository.findByReceiverId(id); }


    public Reminder findById(Long id) { return this.reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found")); }

    public Reminder save(@Valid CreateReminderDto reminder) {
        Appointment appointment = this.appointmentRepository.findById(reminder.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        User receiver = this.userService.findById(reminder.getReceiverId());

        Reminder newReminder = new Reminder();
        newReminder.setAppointment(appointment);
        newReminder.setReceiver(receiver);
        newReminder.setReminderType(reminder.getReminderType());
        newReminder.setTitle(reminder.getTitle());
        newReminder.setMessage(reminder.getMessage());

        return this.reminderRepository.save(newReminder);
    }

    public Reminder update(Long id, @Valid UpdateReminderDto reminder) {
        Reminder reminderToUpdate = this.findById(id);

        reminderMapper.updateReminderFromDto(reminder, reminderToUpdate);

        return this.reminderRepository.save(reminderToUpdate);
    }

    public void delete(Long id) {
        this.findById(id);
        this.reminderRepository.deleteById(id);
    }
}
