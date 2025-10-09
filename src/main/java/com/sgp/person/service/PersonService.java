package com.sgp.person.service;

import com.sgp.person.dto.PersonRequest;
import com.sgp.person.dto.PersonResponse;
import java.util.List;

public interface PersonService {

    // Crear una nueva persona y asociarla a una parroquia.
    PersonResponse createPerson(PersonRequest request);

    // Obtener una persona por su ID.
    PersonResponse getPersonById(Long id);

    // Listar todas las personas. (Considerar paginación en producción)
    List<PersonResponse> getAllPeople();

    // Actualizar los datos de una persona.
    PersonResponse updatePerson(Long id, PersonRequest request);

    // Eliminar (o desactivar) una persona.
    void deletePerson(Long id);

    // *Método adicional que podríamos necesitar*
    // Listar personas por parroquia (útil para la UI)
    List<PersonResponse> getPeopleByParishId(Long parishId);
}