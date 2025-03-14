package com.juandmv.backend.mappers;

import com.juandmv.backend.models.dto.CreatePhysicalLocationDto;
import com.juandmv.backend.models.dto.UpdatePhysicalLocationDto;
import com.juandmv.backend.models.entities.PhysicalLocation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PhysicalLocationMapper {

    PhysicalLocation toEntity(CreatePhysicalLocationDto dto);

    CreatePhysicalLocationDto toDto(CreatePhysicalLocationDto physicalLocation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePhysicalLocationFromDto(UpdatePhysicalLocationDto dto, @MappingTarget PhysicalLocation physicalLocation);
}
