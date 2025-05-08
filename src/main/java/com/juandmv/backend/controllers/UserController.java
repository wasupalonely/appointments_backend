package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.UpdateUserDto;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(this.userService.findAll());
    }

    @GetMapping("/specialty/{specialtyId}")
    public ResponseEntity<List<User>> findBySpecialtyId(@PathVariable Long specialtyId) {
        return ResponseEntity.ok(this.userService.findBySpecialtyId(specialtyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody UpdateUserDto user) {
        return ResponseEntity.ok(this.userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
