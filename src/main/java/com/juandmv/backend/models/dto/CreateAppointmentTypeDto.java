package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppointmentTypeDto {

    @NotNull(message = "El título es obligatorio")
    private String title;

    @NotNull(message = "El icono es obligatorio")
    private String icon;

    @NotNull(message = "La especialidad es obligatoria")
    private String specialty;
}
