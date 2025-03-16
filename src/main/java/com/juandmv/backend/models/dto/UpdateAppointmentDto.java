package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateAppointmentDto {

    @FutureOrPresent
    // Acá se cambia el status a re_scheduled y se tiene que buscar un doctor con ese horario
    private LocalDateTime startTime;
    private String notes;

    // Acá se tiene que buscar el doctor si se cambia la especialidad
    private Long appointmentTypeId;
}
