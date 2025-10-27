package com.sgp.sacrament.service;

import com.sgp.sacrament.dto.SacramentRequest;
import com.sgp.sacrament.dto.SacramentResponse;
import com.sgp.sacrament.model.Sacrament;
import com.sgp.sacrament.model.SacramentDetail;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface SacramentMapper {

    // --- Mapeo de Request a Entidad ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parish", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "canonicalStatus", ignore = true)
    @Mapping(target = "sacramentDetail", ignore = true)
    Sacrament toSacramentEntity(SacramentRequest request);

    // --- Mapeo de Entidad a Response ---

    // ⭐ CORRECCIÓN 1: Especificar la fuente del ID ⭐
    @Mapping(source = "sacrament.id", target = "id")

    @Mapping(source = "sacrament.type.displayName", target = "typeDisplayName")
    @Mapping(source = "sacrament.person.id", target = "personId")
    @Mapping(source = "sacrament.person.fullName", target = "personFullName")
    @Mapping(source = "sacrament.parish.name", target = "parishName")
    @Mapping(source = "detail", target = "detail")
    SacramentResponse toResponse(Sacrament sacrament, SacramentDetail detail);

    // Mapeo del detalle (sub-DTO)
    @Mapping(source = "officiantMinister.fullName", target = "officiantMinisterName")

    // ⭐ CORRECCIÓN 2: Usar getFullName() para Padrinos ⭐
    @Mapping(source = "godfather1.fullName", target = "godfather1Name")
    @Mapping(source = "godfather2.fullName", target = "godfather2Name")
    // ⭐ NUEVOS MAPEOS PARA MATRIMONIO/TESTIGOS ⭐
    @Mapping(source = "spouse.fullName", target = "spouseName")
    @Mapping(source = "witness1.fullName", target = "witness1Name")
    @Mapping(source = "witness2.fullName", target = "witness2Name")
    SacramentResponse.SacramentDetailResponse toDetailResponse(SacramentDetail detail);
}