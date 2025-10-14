package com.sgp.person.service;

import com.sgp.person.dto.PersonRequest;
import com.sgp.person.dto.PersonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonService {

    // Crear una nueva persona y asociarla a una parroquia.
    PersonResponse createPerson(PersonRequest request);

    // Obtener una persona por su ID.
    PersonResponse getPersonById(Long id);

    // Actualizar los datos de una persona.
    PersonResponse updatePerson(Long id, PersonRequest request);

    // Eliminar (o desactivar) una persona.
    void deletePerson(Long id);

    /**
     * Obtiene una lista paginada de personas.
     * @param parishId ID opcional de la Parroquia para filtrar.
     * @param pageable Objeto de paginación y ordenamiento.
     * @return Una Page de PersonResponse.
     */
    Page<PersonResponse> findAllPeople(Long parishId, Pageable pageable);

}