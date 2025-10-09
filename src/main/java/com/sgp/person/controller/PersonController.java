package com.sgp.person.controller;

import com.sgp.person.dto.PersonRequest;
import com.sgp.person.dto.PersonResponse;
import com.sgp.person.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    // Listar todas las Personas: ADMIN, GESTOR, COORDINATOR
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping
    public ResponseEntity<List<PersonResponse>> getAllPeople() {
        return ResponseEntity.ok(personService.getAllPeople());
    }

    // Obtener Persona por ID: ADMIN, GESTOR, COORDINATOR
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    // Obtener Personas por Parroquia: ADMIN, GESTOR, COORDINATOR
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')")
    @GetMapping("/parish/{parishId}")
    public ResponseEntity<List<PersonResponse>> getPeopleByParishId(@PathVariable Long parishId) {
        return ResponseEntity.ok(personService.getPeopleByParishId(parishId));
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