package com.sgp.auth.dto;

import jakarta.annotation.Nullable;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data // Lombok: Getters, Setters, toString, etc.
public class RegisterRequest {

    // --- Credenciales Obligatorias ---
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email debe ser una dirección válida.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    // --- Datos Básicos de Persona (Obligatorios para cualquier registro público) ---
    @NotBlank(message = "El nombre es obligatorio.")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio.")
    private String lastName;

    // --- Datos de Identificación para VINCULACIÓN (Opcionales/Condicionales) ---
    // Si se proporciona uno, el otro debería ser proporcionado. La validación condicional
    // se maneja mejor en el Service o con anotaciones personalizadas. Aquí los dejamos como String para flexibilidad JSON.

    @Nullable // Indica al lector que puede ser nulo en el JSON
    private String identificationType; // Tipo de Identificación (Ej: DNI, Cédula)

    @Nullable // Indica al lector que puede ser nulo en el JSON
    private String identificationNumber; // Número Único y Privado

    // NOTA: Se ha ELIMINADO la necesidad de enviar Gender y BirthDate para el registro inicial,
    // ya que solo son cruciales para la vinculación.
}