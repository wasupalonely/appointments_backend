package com.juandmv.backend.services;

import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateExamTypeDto;
import com.juandmv.backend.models.entities.ExamType;
import com.juandmv.backend.repositories.ExamTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamTypeService {

    @Autowired
    private ExamTypeRepository examTypeRepository;

    public List<ExamType> findAll() { return examTypeRepository.findAll(); }

    public ExamType findById(Long id) {
        return examTypeRepository
            .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de examen no encontrado"));
    }

    public ExamType save(CreateExamTypeDto examType) {
        ExamType newExamType = new ExamType();
        newExamType.setName(examType.getName());
        newExamType.setDescription(examType.getDescription());
        return examTypeRepository.save(newExamType);
    }

    public void delete(Long id) {
        this.findById(id);
        examTypeRepository.deleteById(id);
    }
}
