package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class CreateUnavailabilityDto {

    @Positive(message = "El id del doctor debe ser positivo")
    @NotNull(message = "El id del doctor es obligatorio")
    private Long doctorId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startTime;

    @NotNull(message = "La fecha de finalización es obligatoria")
    private LocalDateTime endTime;

    @NotNull(message = "La razón es obligatoria")
    private String reason;
}
