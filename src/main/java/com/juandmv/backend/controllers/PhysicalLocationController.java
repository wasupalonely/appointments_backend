package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreatePhysicalLocationDto;
import com.juandmv.backend.models.entities.PhysicalLocation;
import com.juandmv.backend.services.PhysicalLocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/physical-locations")
public class PhysicalLocationController {

    @Autowired
    private PhysicalLocationService physicalLocationService;

    @GetMapping
    public List<PhysicalLocation> findAll() { return physicalLocationService.findAll(); }

    @GetMapping("/{id}")
    public PhysicalLocation findById(@PathVariable Long id) { return physicalLocationService.findById(id); }

    @PostMapping
    public PhysicalLocation save(@Valid @RequestBody CreatePhysicalLocationDto physicalLocation) {
        return physicalLocationService.save(physicalLocation);
    }
}
