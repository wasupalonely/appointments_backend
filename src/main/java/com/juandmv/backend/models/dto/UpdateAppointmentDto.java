package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateAppointmentDto {

    @FutureOrPresent
    private LocalDateTime startTime;

    @FutureOrPresent
    private LocalDateTime endTime;
}
