package com.sgp.common.enums;

public enum AppointmentStatus {
    /** Solicitud inicial enviada por el feligrés/personal. */
    PENDING,

    /** Cita aprobada y confirmada. */
    CONFIRMED,

    /** Cita rechazada por falta de disponibilidad, documentos, etc. */
    REJECTED,

    /** Cita se ha completado (el sacramento/evento se llevó a cabo). */
    COMPLETED,

    /** Cita cancelada por el solicitante o el personal parroquial antes de la fecha. */
    CANCELED
}