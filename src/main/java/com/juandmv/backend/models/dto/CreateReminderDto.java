package com.juandmv.backend.models.dto;

import com.juandmv.backend.enums.ReminderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReminderDto {

    @NotNull(message = "El título es obligatorio")
    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 3, max = 50, message = "El título debe tener entre 3 y 50 caracteres")
    private String title;

    @NotNull(message = "El mensaje es obligatorio")
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;

    @NotNull(message = "El tipo de recordatorio es obligatorio")
    private ReminderType reminderType;

    @NotNull(message = "El id de la cita es obligatorio")
    @Positive(message = "El id de la cita debe ser positivo")
    private Long appointmentId;

    @NotNull(message = "El id del receptor es obligatorio")
    @Positive(message = "El id del receptor debe ser positivo")
    private Long receiverId;
}
