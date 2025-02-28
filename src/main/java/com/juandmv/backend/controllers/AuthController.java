package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateUserDto;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.services.UserService;
import com.juandmv.backend.utils.Utils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserDto createUserDto) {
        User savedUser = userService.save(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}
