package com.sgp.user.dto;

import com.sgp.common.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * DTO utilizado por el ADMIN para crear nuevas cuentas de personal.
 */
@Data
public class UserCreationRequest {

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "Debe ser un formato de correo válido.")
    private String email;

    @NotBlank(message = "La contraseña temporal es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password; // Contraseña temporal o inicial

    @NotBlank(message = "El nombre es obligatorio.")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio.")
    private String lastName;

    @NotEmpty(message = "El usuario debe tener al menos un rol asignado.")
    private Set<RoleName> roles;

    private Boolean forcePasswordChange;


    // Opcional: Podrías añadir campos del Profile si el ADMIN necesita llenarlos (ej. phone, address)
}