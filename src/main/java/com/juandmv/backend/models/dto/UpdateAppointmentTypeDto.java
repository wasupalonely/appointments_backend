package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAppointmentTypeDto {

    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String name;

    @Positive(message = "La duración debe ser positiva")
    @Min(value = 15, message = "La duración debe ser mayor a 15")
    private Integer durationInMinutes;

    private Boolean isGeneral;
}
