package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CreateExamAssignmentDto {

    @NotNull(message = "El id del examen es obligatorio")
    private Long examTypeId;

    @NotNull(message = "El id del paciente es obligatorio")
    private Long patientId;

    @NotNull(message = "El id del doctor es obligatorio")
    private Long doctorId;

    private boolean status;
    private Date completedAt;

}
