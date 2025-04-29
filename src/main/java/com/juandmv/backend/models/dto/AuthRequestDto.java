package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {
    @NotNull(message = "El tipo documento es obligatorio")
    @NotBlank(message = "El tipo documento es obligatorio")
    private String documentType;

    @NotNull(message = "El documento es obligatorio")
    @NotBlank(message = "El documento es obligatorio")
    private String documentNumber;

    @NotNull(message = "La contraseña es obligatoria")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
