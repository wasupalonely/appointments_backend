package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSpecialtyDto {

    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @NotNull(message = "El nombre es obligatorio")
    private String name;

    @Size(min = 10, max = 100, message = "La descripción debe tener entre 10 y 100 caracteres")
    @NotNull(message = "La descripción es obligatoria")
    private String description;
}
