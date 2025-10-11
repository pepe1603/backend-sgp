package com.sgp.appointment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * DTO para la creación o actualización de una cita.
 * Solo incluye los IDs de las entidades relacionadas y la información propia de la cita.
 */
@Value // Genera Getters, un constructor con todos los campos, y métodos equals/hashCode
@Builder
public class AppointmentRequest {

    // --- Datos de la Cita ---

    @NotNull(message = "La fecha y hora de la cita es obligatoria.")
    @FutureOrPresent(message = "La cita debe ser en el presente o futuro.")
    LocalDateTime appointmentDateTime;

    @NotBlank(message = "El asunto de la cita es obligatorio.")
    @Size(max = 100, message = "El asunto no puede exceder los 100 caracteres.")
    String subject;

    String notes; // Opcional

    // --- Relaciones (solo IDs) ---

    @NotNull(message = "El ID de la persona asociada a la cita es obligatorio.")
    Long personId;

    @NotNull(message = "El ID de la parroquia donde se solicita la cita es obligatorio.")
    Long parishId;

    /**
     * ID del sacramento asociado. Puede ser nulo si la cita no es para un sacramento.
     */
    Long sacramentId;
}