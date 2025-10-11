package com.sgp.appointment.service;

import com.sgp.appointment.dto.AppointmentRequest;
import com.sgp.appointment.dto.AppointmentResponse;
import com.sgp.common.enums.AppointmentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentService {

    /**
     * Crea una nueva cita a partir de una solicitud.
     * @param request DTO con los datos de la cita.
     * @return DTO de la cita creada.
     */
    AppointmentResponse createAppointment(AppointmentRequest request);

    /**
     * Obtiene una cita por su ID.
     * @param id ID de la cita.
     * @return DTO de la cita.
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Obtiene todas las citas.
     * @return Lista de DTOs de citas.
     */
    List<AppointmentResponse> getAllAppointments();

    // --- READ (My Appointments) ---
    @Transactional(readOnly = true)
    List<AppointmentResponse> getMyAppointments();

    /**
     * Obtiene todas las citas con un estado específico.
     * @param status Estado de la cita (e.g., PENDING, CONFIRMED).
     * @return Lista de DTOs de citas.
     */
    List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status);

    /**
     * Obtiene todas las citas de una persona específica.
     * @param personId ID de la persona.
     * @return Lista de DTOs de citas.
     */
    List<AppointmentResponse> getAppointmentsByPersonId(Long personId);

    /**
     * Actualiza el estado de una cita.
     * @param id ID de la cita a actualizar.
     * @param newStatus El nuevo estado de la cita.
     * @return DTO de la cita actualizada.
     */
    AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus newStatus);

    /**
     * Actualiza completamente una cita (fecha, sujeto, notas).
     * @param id ID de la cita.
     * @param request DTO con los datos a actualizar.
     * @return DTO de la cita actualizada.
     */
    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);

    /**
     * Realiza una eliminación lógica (desactiva) de una cita.
     * @param id ID de la cita a eliminar.
     */
    void deleteAppointment(Long id);
}