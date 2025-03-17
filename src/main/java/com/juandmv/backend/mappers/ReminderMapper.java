package com.juandmv.backend.mappers;

import com.juandmv.backend.models.dto.CreateReminderDto;
import com.juandmv.backend.models.dto.UpdateReminderDto;
import com.juandmv.backend.models.entities.Reminder;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ReminderMapper {
    Reminder toEntity(CreateReminderDto dto);

    CreateReminderDto toDto(Reminder reminder);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReminderFromDto(UpdateReminderDto dto, @MappingTarget Reminder reminder);
}
