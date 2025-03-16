package com.juandmv.backend.mappers;

import com.juandmv.backend.models.dto.CreateAppointmentTypeDto;
import com.juandmv.backend.models.dto.UpdateAppointmentTypeDto;
import com.juandmv.backend.models.entities.AppointmentType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AppointmentTypeMapper {

    AppointmentType toEntity(CreateAppointmentTypeDto dto);

    CreateAppointmentTypeDto toDto(CreateAppointmentTypeDto appointmentTypeDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAppointmentTypeFromDto(UpdateAppointmentTypeDto dto, @MappingTarget AppointmentType appointmentType);
}
