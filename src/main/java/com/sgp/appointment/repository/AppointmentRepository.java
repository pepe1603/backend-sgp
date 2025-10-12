package com.sgp.appointment.repository;

import com.sgp.appointment.model.Appointment;
import com.sgp.common.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Busca todas las citas asociadas a una persona específica (por ID).
     * Se mantiene para la funcionalidad de 'Mis Citas'.
     * @param personId ID de la persona.
     * @return Lista de citas de la persona.
     */
    List<Appointment> findAllByPersonId(Long personId);

    /**
     * Busca todas las citas por un estado específico, aplicando paginación y ordenamiento.
     * Utilizado para el filtrado en el panel de gestión.
     * @param status Estado por el cual filtrar.
     * @param pageable Objeto de paginación y ordenamiento.
     * @return Página de citas que coinciden con el estado.
     */
    Page<Appointment> findAllByStatus(AppointmentStatus status, Pageable pageable);

    // Nota: El método Page<Appointment> findAll(Pageable pageable) lo hereda de JpaRepository.

    /**
     * Busca todas las citas de una parroquia específica y por estado.
     * Se mantiene si esta lógica es usada en algún otro servicio.
     * @param parishId ID de la parroquia.
     * @param status Estado de la cita.
     * @return Lista de citas.
     */
    List<Appointment> findAllByParishIdAndStatus(Long parishId, AppointmentStatus status);

    // ⭐ AÑADIDO: Encuentra todas las citas con paginación (sin filtro de estado). ⭐
    /**
     * Busca todas las citas con paginación.
     */
    Page<Appointment> findAll(Pageable pageable);

}