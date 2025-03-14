package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePhysicalLocationDto {

    @NotNull(message = "El nombre es obligatorio")
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "La dirección es obligatoria")
    @NotBlank(message = "La dirección es obligatoria")
    private String address;
}
