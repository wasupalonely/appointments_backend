package com.juandmv.backend.controllers;

import com.juandmv.backend.models.dto.CreateExamTypeDto;
import com.juandmv.backend.models.entities.ExamType;
import com.juandmv.backend.services.ExamTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-types")
public class ExamTypeController {

    @Autowired
    private ExamTypeService examTypeService;

    @GetMapping
    public ResponseEntity<List<ExamType>> findAll() { return ResponseEntity.ok(this.examTypeService.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<ExamType> findById(@PathVariable Long id) { return ResponseEntity.ok(this.examTypeService.findById(id)); }

    @PostMapping
    public ResponseEntity<ExamType> save(@Valid @RequestBody CreateExamTypeDto examType) { return ResponseEntity.ok(this.examTypeService.save(examType)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) { this.examTypeService.delete(id); return ResponseEntity.noContent().build(); }
}
