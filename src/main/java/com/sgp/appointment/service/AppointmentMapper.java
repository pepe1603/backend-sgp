package com.sgp.appointment.service;

import com.sgp.appointment.dto.AppointmentRequest;
import com.sgp.appointment.dto.AppointmentResponse;
import com.sgp.appointment.model.Appointment;
import com.sgp.common.enums.AppointmentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        // Indicamos que use Spring como componente
        componentModel = MappingConstants.ComponentModel.SPRING,
        // Ignorar campos no mapeados explícitamente para evitar errores de compilación
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AppointmentMapper {

    /**
     * Convierte un AppointmentRequest a una entidad Appointment,
     * inicializando el estado a PENDING.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)      // Mapeo manual en el servicio
    @Mapping(target = "parish", ignore = true)      // Mapeo manual en el servicio
    @Mapping(target = "sacrament", ignore = true)   // Mapeo manual en el servicio
    @Mapping(target = "status", expression = "java(com.sgp.common.enums.AppointmentStatus.PENDING)")
    Appointment toEntity(AppointmentRequest request);

    /**
     * Convierte la entidad Appointment a un AppointmentResponse,
     * mapeando los detalles de las entidades relacionadas.
     */
    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "person.fullName", target = "personFullName") // Usa el método getFullName() de Person
    @Mapping(source = "parish.id", target = "parishId")
    @Mapping(source = "parish.name", target = "parishName")
    @Mapping(source = "sacrament.id", target = "sacramentId")
    @Mapping(source = "sacrament.type", target = "sacramentType") // Asume que Sacrament tiene un campo/método getSacramentType()
    AppointmentResponse toResponse(Appointment appointment);
}