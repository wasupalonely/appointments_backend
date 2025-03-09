package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
}
