package com.sgp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para solicitar un Magic Link (login sin contraseña).
 */
@Data
public class MagicLinkRequest {

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "Debe ser una dirección de email válida.")
    private String email;
}