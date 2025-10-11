package com.sgp.sacrament.controller;

import com.sgp.sacrament.dto.SacramentRequest;
import com.sgp.sacrament.dto.SacramentResponse;
import com.sgp.sacrament.service.SacramentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importación clave
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sacraments")
@RequiredArgsConstructor
public class SacramentController {

    private final SacramentService sacramentService;

    // 1. CREAR SACRAMENTO (Registro Canónico)
    // Acceso restringido a quienes gestionan registros (ADMIN, GESTOR).
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<SacramentResponse> createSacrament(@Valid @RequestBody SacramentRequest request) {
        SacramentResponse response = sacramentService.createSacrament(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. OBTENER SACRAMENTO POR ID
    // El acceso de lectura es más amplio (ADMIN, GESTOR, COORDINATOR).
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SacramentResponse> getSacramentById(@PathVariable Long id) {
        SacramentResponse response = sacramentService.getSacramentById(id);
        return ResponseEntity.ok(response);
    }

    // 3. OBTENER TODOS LOS SACRAMENTOS
    // Lista general para la gestión.
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping
    public ResponseEntity<List<SacramentResponse>> getAllSacraments() {
        List<SacramentResponse> response = sacramentService.getAllSacraments();
        return ResponseEntity.ok(response);
    }

    // 4. OBTENER SACRAMENTOS POR PERSONA
    // Útil para coordinadores y gestores que consultan el historial de un feligrés.
    // El USER simple podría acceder a *sus propios* sacramentos (requiere lógica de seguridad a nivel de servicio),
    // pero a nivel de controlador, restringimos la búsqueda por ID a los roles internos.
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping("/person/{personId}")
    public ResponseEntity<List<SacramentResponse>> getSacramentsByPersonId(@PathVariable Long personId) {
        List<SacramentResponse> response = sacramentService.getSacramentsByPersonId(personId);
        return ResponseEntity.ok(response);
    }

    // 5. ACTUALIZAR SACRAMENTO
    // Acceso restringido para modificar un registro existente (ADMIN, GESTOR).
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<SacramentResponse> updateSacrament(@PathVariable Long id,
                                                             @Valid @RequestBody SacramentRequest request) {
        SacramentResponse response = sacramentService.updateSacrament(id, request);
        return ResponseEntity.ok(response);
    }

    // 6. ELIMINAR SACRAMENTO (Eliminación Lógica)
    // Operación muy sensible. Generalmente se restringe al máximo (ADMIN o GESTOR).
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSacrament(@PathVariable Long id) {
        sacramentService.deleteSacrament(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/sacraments/me
     * Obtiene todos los sacramentos de la persona asociada al usuario autenticado (el feligrés).
     */
    @PreAuthorize("hasAuthority('USER')") // Solo el feligrés puede ver "sus" sacramentos.
    @GetMapping("/me")
    public ResponseEntity<List<SacramentResponse>> getMySacraments() {
        // Llama al nuevo método de servicio que obtiene el ID de la Persona internamente
        List<SacramentResponse> response = sacramentService.getMySacraments();
        return ResponseEntity.ok(response);
    }
}