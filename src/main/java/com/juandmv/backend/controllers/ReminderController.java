package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateReminderDto;
import com.juandmv.backend.models.dto.UpdateReminderDto;
import com.juandmv.backend.models.entities.Reminder;
import com.juandmv.backend.services.ReminderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<Reminder>> findAll() {
        return ResponseEntity.ok(this.reminderService.findAll());
    }

    @GetMapping("/appointment/{id}")
    public ResponseEntity<List<Reminder>> findByAppointmentId(@PathVariable Long id) {
        return ResponseEntity.ok(this.reminderService.findByAppointmentId(id));
    }

    @GetMapping("/receiver/{id}")
    public ResponseEntity<List<Reminder>> findByReceiverId(@PathVariable Long id) {
        return ResponseEntity.ok(this.reminderService.findByReceiverId(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reminder> findById(@PathVariable Long id) {
        return ResponseEntity.ok(this.reminderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Reminder> save(@Valid @RequestBody CreateReminderDto reminder) {
        return ResponseEntity.ok(this.reminderService.save(reminder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reminder> update(@PathVariable Long id, @Valid @RequestBody UpdateReminderDto reminder) {
        return ResponseEntity.ok(this.reminderService.update(id, reminder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.reminderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
