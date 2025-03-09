package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppointmentTypeDto {

    @NotNull(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser positiva")
    private Integer durationInMinutes;

    @NotNull(message = "La especialidad es obligatoria")
    @Positive(message = "El id de la especialidad debe ser positivo")
    private Long specialtyId;

    @NotNull(message = "El tipo de cita es obligatorio")
    private Boolean isGeneral;
}
