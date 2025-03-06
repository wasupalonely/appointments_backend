package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamTypeRepository extends JpaRepository<ExamType, Long> {
}
