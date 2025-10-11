package com.sgp.appointment.model;

import com.sgp.common.enums.AppointmentStatus;
import com.sgp.common.model.Auditable;
import com.sgp.parish.model.Parish;
import com.sgp.person.model.Person;
import com.sgp.sacrament.model.Sacrament;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Datos de la Cita ---

    /** Fecha y hora solicitada o confirmada para la cita. */
    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDateTime;

    /** Asunto o descripción de la cita (ej. "Reunión de preparación", "Solicitud de Bautismo"). */
    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    /** Estado actual de la cita (PENDING, CONFIRMED, REJECTED, etc.). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // --- Relaciones ---

    /**
     * Persona Principal (el sujeto) de la cita (e.g., el niño que será bautizado o el solicitante).
     * Mapeo: Muchas citas (Appointment) se asocian a UNA persona (Person).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /**
     * Parroquia donde se realizará o solicitó la cita.
     * Mapeo: Muchas citas (Appointment) se realizan en UNA parroquia (Parish).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parish_id", nullable = false)
    private Parish parish;

    /**
     * Sacramento asociado a la cita (opcional, no todas las citas son para un sacramento).
     * Mapeo: Muchas citas (Appointment) se asocian a UN sacramento (Sacrament).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sacrament_id") // Por defecto es nullable = true, si no se especifica.
    private Sacrament sacrament;

    // TODO: Considerar añadir una relación con el usuario (User) que solicitó la cita.
}