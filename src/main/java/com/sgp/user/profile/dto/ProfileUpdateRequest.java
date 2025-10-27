package com.sgp.user.profile.dto;

import com.sgp.common.enums.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Optional;

/**
 * DTO utilizado por el usuario autenticado para actualizar su PROPIO perfil.
 * Usa Optional para permitir actualizaciones parciales (PATCH-like behavior on PUT).
 */
@Data
public class ProfileUpdateRequest {

    // --- Campos de Persona que pueden ser actualizados ---

    private Optional<String> firstName = Optional.empty();
    private Optional<String> lastName = Optional.empty();

    // Fecha de nacimiento no suele cambiar, pero la dejamos opcional:
    private Optional<LocalDate> birthDate = Optional.empty();

    private Optional<Gender> gender = Optional.empty();

    @Size(max = 20, message = "El número de teléfono no debe exceder los 20 caracteres.")
    private Optional<String> phoneNumber = Optional.empty();

    @Size(max = 100, message = "La dirección no debe exceder los 100 caracteres.")
    private Optional<String> address = Optional.empty();

    // --- Campos de Usuario que NO se actualizan aquí (email, password, roles) ---
}

