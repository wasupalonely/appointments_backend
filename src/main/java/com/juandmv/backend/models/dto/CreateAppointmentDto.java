package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CreateAppointmentDto {

    @NotNull(message = "El id del paciente es obligatorio")
    @Positive(message = "El id del paciente debe ser positivo")
    private Long patientId;

    @NotNull(message = "El id del tipo de cita es obligatorio")
    @Positive(message = "El id del tipo de cita debe ser positivo")
    private Long appointmentTypeId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private Date startTime;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private Date endTime;

    private String notes;
}
