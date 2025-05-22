package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {

    private String name;
    private String firstName;
    private String lastName;
    private String username;

    @Size(min = 10, max = 10, message = "El teléfono debe tener 10 caracteres")
    private String phone;

    @Email(message = "El correo debe ser válido")
    private String email;

    // Para médicos
    @Positive(message = "El id de la especialidad debe ser positivo")
    private Long specialtyId;

    @Positive(message = "El id de la ubicación física debe ser positivo")
    private Long physicalLocationId;
}
