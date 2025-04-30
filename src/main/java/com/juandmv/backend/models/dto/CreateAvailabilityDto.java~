package com.juandmv.backend.models.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateAvailabilityDto {

    @Positive(message = "El id del doctor debe ser positivo")
    @NotNull(message = "El id del doctor es obligatorio")
    private Long doctorId;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startTime;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime endTime;

    @NotNull(message = "La recurrencia es obligatorio")
    private boolean isRecurring;
}
