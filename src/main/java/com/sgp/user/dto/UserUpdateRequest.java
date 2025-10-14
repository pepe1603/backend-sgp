package com.sgp.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sgp.common.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

/**
 * DTO utilizado por el ADMIN para modificar el estado y roles de otro usuario.
 */
@Data
public class UserUpdateRequest {

    // El email (username) no debe cambiar.
    // La contraseña tampoco debe cambiar a través de este endpoint (debe ser un endpoint de 'reset password').

    // 1. Estado de habilitación (Activar/Desactivar)
    // Forzamos a Jackson a usar "isEnabled" para coincidir con tu convención
    // Estado de habilitación (Activar/Desactivar)
    @JsonProperty("isEnabled")
    private boolean isEnabled;

    // 2. Estado de actividad (Borrado lógico)
    // Forzamos a Jackson a usar "isActive"
    // Estado de actividad (Borrado lógico)
    @JsonProperty("isActive")
    private boolean isActive;

    // 3. Roles
    // Roles: debe haber al menos un rol.
    @NotEmpty(message = "El usuario debe tener al menos un rol asignado.")
    private Set<RoleName> roles;

    //añaidr mas campso recoemndaddos
}