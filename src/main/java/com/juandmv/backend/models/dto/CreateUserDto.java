package com.juandmv.backend.models.dto;

import com.juandmv.backend.enums.DocumentType;
import com.juandmv.backend.enums.Gender;
import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.models.entities.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateUserDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    private String phone;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=\\[\\]{}|;:'\",.<>?/`~]).{8,}$",
            message = "La contraseña debe contener al menos una letra mayúscula, un número y un carácter especial (por ejemplo: !@#$%^&*()_+-)"
    )
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El tipo de documento es obligatorio")
    private DocumentType documentType;

    @NotNull(message = "El documento es obligatorio")
    @NotBlank(message = "El documento no puede estar vacío")
    private String documentNumber;

    @NotNull(message = "El género es obligatorio")
    private Gender gender;

    private List<Role> roles;

    @Enumerated(EnumType.STRING)
    private Roles role;

    // USAR PARA MÉDICOS
    private Long specialtyId;

    private boolean defaultSchedule = false;

    private Long physicalLocationId;
}
