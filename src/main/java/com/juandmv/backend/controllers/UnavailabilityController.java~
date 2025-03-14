package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateUnavailabilityDto;
import com.juandmv.backend.models.entities.Unavailability;
import com.juandmv.backend.services.UnavailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unavailabilities")
public class UnavailabilityController {

    @Autowired
    private UnavailabilityService unavailabilityService;

    @GetMapping
    public List<Unavailability> findAll() { return unavailabilityService.findAll(); }

    @GetMapping("/doctor/{id}")
    public List<Unavailability> findByDoctorId(@PathVariable Long id) { return unavailabilityService.findByDoctorId(id); }

    @PostMapping
    public Unavailability save(@Valid @RequestBody CreateUnavailabilityDto unavailability) { return unavailabilityService.save(unavailability); }
}
