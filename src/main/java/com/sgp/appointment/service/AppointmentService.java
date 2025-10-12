package com.sgp.appointment.service;

import com.sgp.appointment.dto.AppointmentRequest;
import com.sgp.appointment.dto.AppointmentResponse;
import com.sgp.common.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Obtiene una lista paginada de citas, opcionalmente filtrada por estado.
     * Este método reemplaza a los antiguos getAllAppointments y getAppointmentsByStatus.
     * Solo accesible por roles de gestión (ADMIN, GESTOR, COORDINATOR).
     *
     * @param status Estado de la cita para filtrar (opcional, si es null trae todas).
     * @param pageable Objeto que contiene información de paginación y ordenamiento.
     * @return Un objeto Page con los DTOs de las citas.
     */
    Page<AppointmentResponse> findAllAppointments(AppointmentStatus status, Pageable pageable);


    // --- READ (My Appointments) ---
    @Transactional(readOnly = true)
    List<AppointmentResponse> getMyAppointments();

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