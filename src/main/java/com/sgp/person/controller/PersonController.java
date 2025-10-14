package com.sgp.person.controller;

import com.sgp.person.dto.PersonRequest;
import com.sgp.person.dto.PersonResponse;
import com.sgp.person.service.PersonService;
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
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    // Crear Persona: Solo ADMIN o GESTOR
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<PersonResponse> createPerson(@Valid @RequestBody PersonRequest request) {
        PersonResponse response = personService.createPerson(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ⭐ MODIFICADO/UNIFICADO: OBTENER TODAS LAS PERSONAS (Paginada y Opcionalmente Filtrada por Parroquia) ⭐
    /**
     * GET /api/v1/people?page=0&size=20&sort=lastName,asc&parishId=1
     * Obtiene una lista paginada de personas.
     *
     * @param pageable Parámetros de paginación (page, size) y ordenamiento (sort).
     * @param parishId Filtro opcional por el ID de la Parroquia.
     * @return Una respuesta Page con la lista de DTOs de personas.
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping // Endpoint Unificado
    public ResponseEntity<Page<PersonResponse>> findAllPeople(
            // Por defecto, ordenamos por apellido (lastName) en orden ascendente (alfabético).
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC)
            Pageable pageable,
            // El ID de la parroquia es un filtro opcional
            @RequestParam(required = false) Long parishId
    ) {
        Page<PersonResponse> responsePage = personService.findAllPeople(parishId, pageable);
        return ResponseEntity.ok(responsePage);
    }

    // Obtener Persona por ID: ADMIN, GESTOR, COORDINATOR
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    // Actualizar Persona: Solo ADMIN o GESTOR
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> updatePerson(@PathVariable Long id, @Valid @RequestBody PersonRequest request) {
        return ResponseEntity.ok(personService.updatePerson(id, request));
    }

    // Eliminar (Desactivar) Persona: Solo ADMIN (Operación crítica/seguridad)
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
    }
}