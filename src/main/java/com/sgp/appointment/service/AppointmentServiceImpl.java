package com.sgp.appointment.service;

import com.sgp.appointment.dto.AppointmentRequest;
import com.sgp.appointment.dto.AppointmentResponse;
import com.sgp.appointment.model.Appointment;
import com.sgp.appointment.repository.AppointmentRepository;
import com.sgp.common.enums.AppointmentStatus;
import com.sgp.common.exception.InvalidStateTransitionException;
import com.sgp.common.exception.ResourceConflictException;
import com.sgp.common.exception.ResourceNotAuthorizedException; // Nueva importación
import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.common.service.SecurityContextService;
import com.sgp.parish.model.Parish;
import com.sgp.parish.repository.ParishRepository;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.sacrament.model.Sacrament;
import com.sgp.sacrament.repository.SacramentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PersonRepository personRepository;
    private final ParishRepository parishRepository;
    private final SacramentRepository sacramentRepository;
    private final AppointmentMapper appointmentMapper;
    private final SecurityContextService securityContextService; // ⭐ NUEVA INYECCIÓN ⭐


    private static final String RESOURCE_APPOINTMENT = "Cita/Agendamiento";
    private static final String RESOURCE_PERSON = "Persona";
    private static final String RESOURCE_PARISH = "Parroquia";
    private static final String RESOURCE_SACRAMENT = "Sacramento";

    // Roles que tienen acceso de gestión (ADMIN, GESTOR, COORDINATOR)
    private static final Set<String> MANAGEMENT_ROLES = Set.of("ADMIN", "GESTOR", "COORDINATOR");

    // --- Métodos de Ayuda de Búsqueda de Entidades Básicas (Se mantienen aquí) ---

    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_APPOINTMENT, "id", id));
    }

    private Person findPersonById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PERSON, "ID", id));
    }

    private Parish findParishById(Long id) {
        return parishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PARISH, "ID", id));
    }

    private Sacrament findSacramentById(Long id) {
        if (id == null) return null;
        return sacramentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SACRAMENT, "ID", id));
    }

    // --- LÓGICA DE VALIDACIÓN DE PROPIEDAD (Ajustada) ---
    private void validateAppointmentOwnership(Appointment appointment) {
        // Si el usuario *no* es de gestión, debe ser el dueño
        if (!securityContextService.isManagementUser()) { // ⭐ USANDO EL SERVICIO ⭐
            Person currentUserPerson = securityContextService.findPersonForCurrentUser(); // ⭐ USANDO EL SERVICIO ⭐

            if (!appointment.getPerson().getId().equals(currentUserPerson.getId())) {
                throw new ResourceNotAuthorizedException("Solo puedes acceder a tus propias citas.");
            }
        }
    }

    // --- CREATE ---
    @Override
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // ⭐ AJUSTE DE SEGURIDAD 1: Forzar que el USER solo cree citas para su propia persona ⭐
        if (!securityContextService.isManagementUser()) { // ⭐ USANDO EL SERVICIO ⭐
            Person currentUserPerson = securityContextService.findPersonForCurrentUser(); // ⭐ USANDO EL SERVICIO ⭐

            if (!request.getPersonId().equals(currentUserPerson.getId())) {
                throw new ResourceNotAuthorizedException("Un usuario solo puede crear citas para su propia persona.");
            }
        }

        // 1. Buscar y validar entidades relacionadas
        Person person = findPersonById(request.getPersonId());
        Parish parish = findParishById(request.getParishId());
        Sacrament sacrament = findSacramentById(request.getSacramentId());

        // 2. Mapear DTO a entidad (el estado se inicializa a PENDING en el Mapper)
        Appointment appointment = appointmentMapper.toEntity(request);

        // 3. Asignar las entidades relacionadas
        appointment.setPerson(person);
        appointment.setParish(parish);
        appointment.setSacrament(sacrament);

        // 4. Guardar y Responder
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(savedAppointment);
    }

    // --- READ (Single) ---
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = findAppointmentById(id);
        validateAppointmentOwnership(appointment);
        return appointmentMapper.toResponse(appointment);
    }

    // --- READ (All) ---
    // Este método queda para roles de gestión (ADMIN, GESTOR) y se controla en el Controller.
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- READ (My Appointments) ---
    @Transactional(readOnly = true)
    @Override
    public List<AppointmentResponse> getMyAppointments() {
        Person currentUserPerson = securityContextService.findPersonForCurrentUser(); // ⭐ USANDO EL SERVICIO ⭐
        return appointmentRepository.findAllByPersonId(currentUserPerson.getId()).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- READ (By Status) ---
    // Este método se usa en la gestión y se controla en el Controller.
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findAllByStatus(status).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- READ (By Person ID) ---
    // Este método se usa en la gestión y se controla en el Controller.
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPersonId(Long personId) {
        return appointmentRepository.findAllByPersonId(personId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- UPDATE Full ---
    @Override
    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        Appointment existingAppointment = findAppointmentById(id);

        validateAppointmentOwnership(existingAppointment);

        // ⭐ AJUSTE DE SEGURIDAD 3: Solo permitir actualizar si es gestor O es el dueño Y el estado es PENDING ⭐
        // Restricción de estado y rol
        // Regla de Negocio Adicional: Si el USER intenta actualizar, debe ser PENDING.
        // Si es un rol de gestión, puede actualizar incluso si ya fue CONFIRMED/REJECTED.
        if (!securityContextService.isManagementUser() && existingAppointment.getStatus() != AppointmentStatus.PENDING) { // ⭐ USANDO EL SERVICIO ⭐
            throw new ResourceNotAuthorizedException("Solo puedes modificar citas que están en estado PENDIENTE.");
        }

        // Restricción de persona
        if (!securityContextService.isManagementUser()) { // ⭐ USANDO EL SERVICIO ⭐
            Person currentUserPerson = securityContextService.findPersonForCurrentUser(); // ⭐ USANDO EL SERVICIO ⭐
            if (!request.getPersonId().equals(currentUserPerson.getId())) {
                throw new ResourceNotAuthorizedException("No puedes cambiar la persona asociada a la cita.");
            }
        }

        // 1. Buscar y validar entidades relacionadas para posible cambio
        Person person = findPersonById(request.getPersonId());
        Parish parish = findParishById(request.getParishId());
        Sacrament sacrament = findSacramentById(request.getSacramentId());

        // 2. Actualizar campos de la entidad
        existingAppointment.setAppointmentDateTime(request.getAppointmentDateTime());
        existingAppointment.setSubject(request.getSubject());
        existingAppointment.setNotes(request.getNotes());

        // 3. Actualizar relaciones si han cambiado (controlado por el chequeo de roles arriba)
        if (!existingAppointment.getPerson().getId().equals(person.getId())) {
            existingAppointment.setPerson(person);
        }
        if (!existingAppointment.getParish().getId().equals(parish.getId())) {
            existingAppointment.setParish(parish);
        }
        if ((existingAppointment.getSacrament() == null && sacrament != null) ||
                (existingAppointment.getSacrament() != null && !existingAppointment.getSacrament().equals(sacrament))) {
            existingAppointment.setSacrament(sacrament);
        }

        // 4. Guardar y Responder
        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);
        return appointmentMapper.toResponse(updatedAppointment);
    }

    // --- DELETE (Lógico) ---
    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = findAppointmentById(id);

        // ⭐ AJUSTE DE SEGURIDAD 4: Solo permitir eliminar si es gestor O es el dueño Y el estado es PENDING ⭐
        // ⭐ AJUSTE DE SEGURIDAD: Validar propiedad y estado PENDING/CONFIRMED para USER ⭐
        validateAppointmentOwnership(appointment);

        // Regla de Negocio: El USER solo puede "cancelar/eliminar" si aún no está completa.
        // Si no es un usuario de gestión (solo USER) y la cita está COMPLETED, no puede eliminarla.
        if (!securityContextService.isManagementUser() && appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ResourceNotAuthorizedException("No puedes eliminar una cita que ya ha sido completada.");
        }

        // Si no es un usuario de gestión (solo USER), forzar a que use la acción de negocio (Cancelar)
        // en lugar de la eliminación lógica si la cita está activa.
        if (!securityContextService.isManagementUser()) {
            throw new ResourceNotAuthorizedException("La eliminación no está permitida. Por favor, use la función de 'Cancelar Cita' (updateStatus) si es necesario.");
        }


        // ⭐ LÓGICA DE ELIMINACIÓN LÓGICA (Solo para roles de GESTIÓN) ⭐
        if (!appointment.isActive()) {
            throw new ResourceConflictException(RESOURCE_APPOINTMENT, "estado", "ya está inactivo");
        }

        // Mejora: Cambiar estado a CANCELADO en lugar de eliminar si la cita fue CONFIRMED
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED && !securityContextService.isManagementUser()) { // ⭐ USANDO EL SERVICIO ⭐
            appointment.setStatus(AppointmentStatus.CANCELED);
        }

        //simplemente sse marca como inactivo
        appointment.setActive(false);
        appointmentRepository.save(appointment);
    }

    // --- UPDATE Status ---
    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        Appointment existingAppointment = findAppointmentById(id);
        AppointmentStatus currentStatus = existingAppointment.getStatus();

        if (currentStatus == newStatus) {
            throw new ResourceConflictException(RESOURCE_APPOINTMENT, "Estado",
                    String.format("ya tiene el estado '%s'", newStatus));
        }

        // ⭐ REGLA DE NEGOCIO: VALIDACIÓN DE FLUJO DE ESTADO ⭐
        validateStatusTransition(currentStatus, newStatus);

        // ⭐ REGLA DE NEGOCIO: Un USER solo puede CANCELAR ⭐
        if (!securityContextService.isManagementUser() && newStatus != AppointmentStatus.CANCELED) {
            throw new ResourceNotAuthorizedException("Un feligrés solo puede cambiar el estado de su cita a CANCELADO.");
        }

        existingAppointment.setStatus(newStatus);
        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

        // TODO: Enviar notificación

        return appointmentMapper.toResponse(updatedAppointment);
    }

    /**
     * Valida si la transición de estado de una cita es permitida según las reglas de negocio.
     */
    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus target) {
        switch (current) {
            case PENDING:
                // PENDING puede pasar a todo excepto COMPLETED directamente.
                if (target == AppointmentStatus.COMPLETED) {
                    throw new InvalidStateTransitionException(
                            String.format("No se puede marcar como '%s' una cita que está en '%s'. Debe ser 'CONFIRMED' primero.", target, current));

                }
                break;
            case CONFIRMED:
                // CONFIRMED solo puede pasar a COMPLETED o CANCELED.
                if (target != AppointmentStatus.COMPLETED && target != AppointmentStatus.CANCELED) {
                    // ⭐ CAMBIO DE EXCEPCIÓN ⭐
                    throw new InvalidStateTransitionException(
                            String.format("Una cita '%s' solo puede ser 'COMPLETED' o 'CANCELED'. Transición a '%s' no permitida.", current, target));

                }
                break;
            case REJECTED:
            case COMPLETED:
            case CANCELED:
                // Estos son estados finales, no se puede cambiar a NADA más.
                // ⭐ CAMBIO DE EXCEPCIÓN ⭐
                throw new InvalidStateTransitionException(
                        String.format("Una cita '%s' es un estado final y no puede ser modificada a '%s'.", current, target));

        }
    }
}