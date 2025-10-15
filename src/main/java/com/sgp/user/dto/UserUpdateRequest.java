package com.sgp.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sgp.common.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Optional;
import java.util.Set;

/**
 * DTO utilizado por el ADMIN para modificar el estado y roles de otro usuario.
 * Nota: Los campos se envuelven en Optional para soportar actualizaciones parciales (PATCH).
 */
@Data
public class UserUpdateRequest {

    // El email (username) no debe cambiar.
    // La contraseña tampoco debe cambiar a través de este endpoint (debe ser un endpoint de 'reset password').

    // 1. Estado de habilitación (Activar/Desactivar)
    // Forzamos a Jackson a usar "isEnabled" para coincidir con tu convención
    // Estado de habilitación (Activar/Desactivar)
    @JsonProperty("isEnabled")
    private Optional<Boolean> isEnabled = Optional.empty();

    // 2. Estado de actividad (Borrado lógico)
    // Forzamos a Jackson a usar "isActive"
    // Estado de actividad (Borrado lógico)
    @JsonProperty("isActive")
    private Optional<Boolean> isActive = Optional.empty();

    // 3. Roles
    // Aunque sigue siendo una lista, la convertimos en Optional.
    // La validación @NotEmpty se debe eliminar porque es opcional.
    // Si se envía, *debe* contener roles, pero si no se envía, se omite.
    // @NotEmpty(message = "El usuario debe tener al menos un rol asignado.") // 👈 ELIMINAR O MANEJAR DENTRO DEL SERVICE
    private Optional<Set<RoleName>> roles = Optional.empty(); // 👈 CAMBIAR a Optional

    private Optional<Boolean> forcePasswordChange = Optional.empty();



    //añaidr mas campso recoemndaddos, por ahroas solo esstos.
}