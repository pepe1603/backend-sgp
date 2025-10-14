package com.sgp.parish.controller;

import com.sgp.parish.dto.ParishRequest;
import com.sgp.parish.dto.ParishResponse;
import com.sgp.parish.service.ParishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parishes")
@RequiredArgsConstructor
public class ParishController {

    private final ParishService parishService;

    // Solo el ADMIN puede crear una Parroquia : Configuracion Criticaq
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<ParishResponse> createParish(@Valid @RequestBody ParishRequest request) {
        ParishResponse response = parishService.createParish(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ⭐ MODIFICADO: Listar todas las Parroquias (Ahora Paginado y Ordenado) ⭐
    /**
     * GET /api/v1/parishes?page=0&size=10&sort=name,asc
     * Obtiene una lista paginada de parroquias.
     *
     * @param pageable Parámetros de paginación (page, size) y ordenamiento (sort).
     * @return Una respuesta Page con la lista de DTOs de parroquias.
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR', 'USER')")
    @GetMapping // Endpoint Unificado
    public ResponseEntity<Page<ParishResponse>> findAllParishes(
            // Por defecto, ordenamos por 'name' ascendente.
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<ParishResponse> responsePage = parishService.findAllParishes(pageable);
        return ResponseEntity.ok(responsePage);
    }

    // Obtener Parroquia por ID (Accesible para todos los roles internos)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ParishResponse> getParishById(@PathVariable Long id) {
        return ResponseEntity.ok(parishService.getParishById(id));
    }

    // Actualizar Parroquia (Solo ADMIN, requiere cambio de configuración)
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ParishResponse> updateParish(@PathVariable Long id, @Valid @RequestBody ParishRequest request) {
        return ResponseEntity.ok(parishService.updateParish(id, request));
    }

    // Eliminar Parroquia (Solo ADMIN, Operacion peligrosa)
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteParish(@PathVariable Long id) {
        parishService.deleteParish(id);
    }
}