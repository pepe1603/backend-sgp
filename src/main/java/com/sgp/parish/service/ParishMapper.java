package com.sgp.parish.service;

import com.sgp.parish.dto.ParishRequest;
import com.sgp.parish.dto.ParishResponse;
import com.sgp.parish.model.Parish;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ParishMapper {

    // Mapea de Request DTO a Entidad (para crear)
    Parish toEntity(ParishRequest request);

    // Mapea de Entidad a Response DTO (para retornar)
    ParishResponse toResponse(Parish parish);

    // Mapea y actualiza una Entidad existente desde el Request DTO (para actualizar)
    // El 'target' es la entidad que será modificada.
    void updateEntityFromRequest(ParishRequest request, @MappingTarget Parish parish);
}
