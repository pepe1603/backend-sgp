package com.sgp.appointment.dto;

import com.sgp.common.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una cita.
 * Incluye datos de auditoría y detalles de las entidades relacionadas.
 */
@Value
@Builder
public class AppointmentResponse {

    Long id;

    // --- Datos de la Cita ---
    LocalDateTime appointmentDateTime;
    String subject;
    AppointmentStatus status;
    String notes;

    // --- Información de Entidades Relacionadas ---

    Long personId;
    String personFullName; // Nombre completo de la persona asociada

    Long parishId;
    String parishName; // Nombre de la parroquia

    Long sacramentId;
    String sacramentType; // Tipo de sacramento (e.g., "Bautismo", "Confirmación")

    // --- Auditoría ---
    LocalDateTime createdAt;
    String createdBy;
    LocalDateTime updatedAt;
    String updatedBy;
    Boolean isActive;
}