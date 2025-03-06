package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateExamTypeDto {

    @NotNull(message = "El nombre es obligatorio")
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;
}
