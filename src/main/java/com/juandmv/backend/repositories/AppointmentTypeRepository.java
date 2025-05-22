package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {
    @Query("SELECT a FROM AppointmentType a WHERE (:isGeneral IS NULL OR a.isGeneral = :isGeneral)")
    List<AppointmentType> findAll(@Param("isGeneral") Boolean isGeneral);


    @Query("""
    SELECT a FROM AppointmentType a
    WHERE a.specialty.id = :specialtyId
    AND (:isGeneral IS NULL OR a.isGeneral = :isGeneral)
""")
    List<AppointmentType> findBySpecialty_Id(@Param("specialtyId") Long specialtyId,
                                             @Param("isGeneral") Boolean isGeneral);

    List<AppointmentType> findByName(String name);

    long countBySpecialtyId(Long specialtyId);
}
