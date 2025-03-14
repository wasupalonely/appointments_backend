package com.juandmv.backend.mappers;

import com.juandmv.backend.models.dto.CreateUserDto;
import com.juandmv.backend.models.dto.UpdateUserDto;
import com.juandmv.backend.models.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(CreateUserDto dto);

    CreateUserDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserDto dto, @MappingTarget User user);
}

