package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateSpecialtyDto;
import com.juandmv.backend.models.entities.Specialty;
import com.juandmv.backend.services.SpecialtyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/specialties")
public class SpecialtyController {

    @Autowired
    private SpecialtyService specialtyService;

    @GetMapping
    public List<Specialty> findAll() { return specialtyService.findAll(); }

    @GetMapping("/{id}")
    public Specialty findById(@PathVariable Long id) { return specialtyService.findById(id); }

    @PostMapping
    public Specialty save(@Valid @RequestBody CreateSpecialtyDto specialty) { return specialtyService.save(specialty); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { specialtyService.delete(id); }
}
