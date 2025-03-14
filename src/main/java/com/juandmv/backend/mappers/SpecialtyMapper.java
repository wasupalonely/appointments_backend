package com.juandmv.backend.mappers;

import com.juandmv.backend.models.dto.CreateSpecialtyDto;
import com.juandmv.backend.models.dto.UpdateSpecialtyDto;
import com.juandmv.backend.models.entities.Specialty;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {

    Specialty toEntity(CreateSpecialtyDto dto);

    CreateSpecialtyDto toDto(Specialty specialty);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSpecialtyFromDto(UpdateSpecialtyDto dto, @MappingTarget Specialty specialty);
}
