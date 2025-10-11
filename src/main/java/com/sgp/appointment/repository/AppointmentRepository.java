package com.sgp.appointment.repository;

import com.sgp.appointment.model.Appointment;
import com.sgp.common.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Busca todas las citas asociadas a una persona específica (por ID).
     */
    List<Appointment> findAllByPersonId(Long personId);

    /**
     * Busca todas las citas por un estado específico.
     */
    List<Appointment> findAllByStatus(AppointmentStatus status);

    /**
     * Busca todas las citas de una parroquia específica y por estado.
     */
    List<Appointment> findAllByParishIdAndStatus(Long parishId, AppointmentStatus status);
}