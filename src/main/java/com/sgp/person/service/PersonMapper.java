package com.sgp.person.service;

import com.sgp.person.dto.PersonRequest;
import com.sgp.person.dto.PersonResponse;
import com.sgp.person.model.Person;
import com.sgp.parish.model.Parish; // Necesitas esto para la relación
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    // --- Mapeo Request a Entity (Creación) ---
    // Ignoramos el ID de la parroquia ya que será inyectado manualmente en el servicio.
    @Mapping(target = "parish", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true") // Por defecto, una nueva persona está activa
    Person toEntity(PersonRequest request);

    // --- Mapeo Entity a Response (Lectura) ---
    @Mapping(source = "parish.id", target = "parishId")
    @Mapping(source = "parish.name", target = "parishName")
    PersonResponse toResponse(Person person);

    // --- Mapeo de Actualización ---
    // Ignoramos el ID de la parroquia y los campos de auditoría para la actualización
    @Mapping(target = "parish", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(PersonRequest request, @MappingTarget Person person);


    // NOTA: Es útil añadir un método para mapear listas, aunque se puede hacer con streams
    // List<PersonResponse> toResponseList(List<Person> people);
}