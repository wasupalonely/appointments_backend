package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.PhysicalLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhysicalLocationRepository extends JpaRepository<PhysicalLocation, Long> {
}
