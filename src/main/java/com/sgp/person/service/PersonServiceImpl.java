package com.sgp.person.service;

import com.sgp.common.exception.ResourceConflictException;
import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.parish.model.Parish;
import com.sgp.parish.repository.ParishRepository;
import com.sgp.person.dto.PersonRequest;
import com.sgp.person.dto.PersonResponse;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final ParishRepository parishRepository; // Necesario para buscar la Parroquia
    private final PersonMapper personMapper;

    private static final String RESOURCE_PERSON = "Persona";
    private static final String RESOURCE_PARISH = "Parroquia";

    @Override
    @Transactional
    public PersonResponse createPerson(PersonRequest request) {
        // 1. Validar unicidad por identificación
        if (personRepository.existsByIdentificationTypeAndIdentificationNumber(
                request.getIdentificationType(),
                request.getIdentificationNumber())) {
            throw new ResourceConflictException(RESOURCE_PERSON, "identificación",
                    request.getIdentificationType() + " " + request.getIdentificationNumber());
        }

        // 2. Buscar y validar la Parroquia
        Parish parish = parishRepository.findById(request.getParishId())
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PARISH, "id", request.getParishId()));

        // 3. Mapear y asignar la Parroquia
        Person person = personMapper.toEntity(request);
        person.setParish(parish); // Asignar la entidad Parish a la Persona

        // 4. Guardar y responder
        Person savedPerson = personRepository.save(person);
        return personMapper.toResponse(savedPerson);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonResponse getPersonById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PERSON, "id", id));
        return personMapper.toResponse(person);
    }

    @Override
    @Transactional
    public PersonResponse updatePerson(Long id, PersonRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PERSON, "id", id));

        // 1. Validar unicidad si el número de identificación ha cambiado
        String newIdType = request.getIdentificationType();
        String newIdNumber = request.getIdentificationNumber();

        if (!person.getIdentificationType().equals(newIdType) || !person.getIdentificationNumber().equals(newIdNumber)) {
            if (personRepository.findByIdentificationTypeAndIdentificationNumber(newIdType, newIdNumber)
                    .filter(p -> !p.getId().equals(id)) // Asegurar que no sea la misma persona
                    .isPresent()) {
                throw new ResourceConflictException(RESOURCE_PERSON, "identificación", newIdType + " " + newIdNumber);
            }
        }

        // 2. Validar y re-asignar la Parroquia si el ID ha cambiado
        if (!person.getParish().getId().equals(request.getParishId())) {
            Parish newParish = parishRepository.findById(request.getParishId())
                    .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PARISH, "id", request.getParishId()));
            person.setParish(newParish);
        }

        // 3. Mapear los otros campos y guardar
        personMapper.updateEntityFromRequest(request, person);

        Person updatedPerson = personRepository.save(person);
        return personMapper.toResponse(updatedPerson);
    }

    @Override
    @Transactional
    public void deletePerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PERSON, "id", id));

        // **MEJOR PRÁCTICA:** En lugar de eliminar, marcamos como inactivo (eliminación lógica)
        // Esto preserva el historial de sacramentos y documentos.
        person.setActive(false);
        personRepository.save(person);
    }
    // ⭐ NUEVO/MODIFICADO: Implementación de Paginación y Filtrado Unificado ⭐
    @Override
    @Transactional(readOnly = true)
    public Page<PersonResponse> findAllPeople(Long parishId, Pageable pageable) {
        Page<Person> personPage;

        // Si se proporciona un parishId, filtramos por parroquia
        if (parishId != null) {
            // Opcional: Validar la existencia de la parroquia antes de filtrar
            if (!parishRepository.existsById(parishId)) {
                throw new ResourceNotFoundException(RESOURCE_PARISH, "id", parishId);
            }
            personPage = personRepository.findByParish_Id(parishId, pageable);
        } else {
            // Si no hay filtro, buscamos todos paginados
            personPage = personRepository.findAll(pageable);
        }

        // Mapeamos Page<Person> a Page<PersonResponse>
        return personPage.map(personMapper::toResponse);
    }

}