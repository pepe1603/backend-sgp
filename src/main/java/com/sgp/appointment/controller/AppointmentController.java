package com.sgp.appointment.controller;

import com.sgp.appointment.dto.AppointmentRequest;
import com.sgp.appointment.dto.AppointmentResponse;
import com.sgp.appointment.service.AppointmentService;
import com.sgp.common.enums.AppointmentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // --- 1. CREAR CITA (Solicitud) ---
    // Permitido para GESTORES (que crean citas directamente) y USUARIOS (que las solicitan).
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'USER')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // --- 2. OBTENER CITA POR ID ---
    // Acceso para todos los roles de gestión y coordinación, incluyendo el USER que hizo la solicitud (si se implementa en el servicio).
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        // Nota: En el servicio, se debe añadir lógica para que un 'USER' solo pueda ver SU cita.
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(response);
    }

    // --- 3. OBTENER TODAS LAS CITAS (Lista de Gestión) ---
    // Lista general para el personal interno.
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<AppointmentResponse> response = appointmentService.getAllAppointments();
        return ResponseEntity.ok(response);
    }

    // --- 4. OBTENER CITAS POR ESTADO ---
    // Útil para filtrar el panel de gestión (ej. solo ver PENDING).
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        List<AppointmentResponse> response = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(response);
    }

    // --- 5. ACTUALIZAR ESTADO DE CITA (Aprobación/Rechazo/Completado/CANCELAR) ---
    // Acción clave de gestión (ADMIN, GESTOR), pero permitimos a USER usarla para CANCELAR.
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'USER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(@PathVariable Long id,
                                                                       @RequestParam AppointmentStatus newStatus) {
        // La validación de que un USER solo puede pasar a CANCELED se hace en el Servicio.
        AppointmentResponse response = appointmentService.updateAppointmentStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }

    // --- 6. ACTUALIZAR DATOS DE CITA (Fecha/Asunto/Notas) ---
    // Permitido a GESTORES, y opcionalmente al USER si la cita aún está PENDING.
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable Long id,
                                                                 @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(response);
    }

    // --- 7. ELIMINAR CITA (Eliminación Lógica) ---
    // Operación de gestión sensible.
    // Restringido solo para roles de gestión (ADMIN, GESTOR)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        // Esta es la eliminación lógica (isActive=false) solo para fines de mantenimiento.
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // --- 8. Obtiene citas/agendamiento del usuario auth ---
    // --------------------------------------------------------------------------------------------------
    // ⭐ NUEVO ENDPOINT: OBTENER MIS CITAS (USER) ⭐
    // --------------------------------------------------------------------------------------------------
    /**
     * GET /api/v1/appointments/me
     * Obtiene todas las citas asociadas a la persona del usuario autenticado (el feligrés).
     */
    @PreAuthorize("hasAuthority('USER')") // Solo el feligrés puede ver "sus" citas.
    @GetMapping("/me")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        // Llama al nuevo método de servicio que obtiene el ID de la Persona internamente
        List<AppointmentResponse> response = appointmentService.getMyAppointments();
        return ResponseEntity.ok(response);
    }
}