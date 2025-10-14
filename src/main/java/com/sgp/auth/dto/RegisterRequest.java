package com.sgp.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data // Lombok: Getters, Setters, toString, etc.
public class RegisterRequest {

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email debe ser una dirección válida.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    @NotBlank(message = "El nombre es obligatorio.")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio.")
    private String lastName;

    // NOTA: No incluimos el rol. El rol por defecto ('USER') se asigna en el Service Layer.
}