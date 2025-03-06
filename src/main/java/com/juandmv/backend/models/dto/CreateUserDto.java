package com.juandmv.backend.models.dto;

import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.models.entities.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateUserDto {

    private String username;

    private String firstName;

    private String lastName;

    private String phone;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Min(8)
    private String password;

    private List<Role> roles;

    @Enumerated(EnumType.STRING)
    private Roles role;
}
