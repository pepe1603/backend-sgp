package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.user.dto.UserManagementResponse;
import com.sgp.user.dto.UserUpdateRequest; // ¡Necesitamos crear este DTO en el siguiente paso!
import com.sgp.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // --- Mapeo para el Listado de Administración ---
    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    // MapStruct es lo suficientemente inteligente para mapear Set<Role> a Set<RoleName>
    // usando un @Named, pero simplificaremos el calificador si ya tenemos la lógica directa:
    // ⚠️ ELIMINAR EL qualifiedByName si usamos la conversión directa en el default method ⚠️

    // Mapeo de Auditoría explícito:
    @Mapping(target = "isActive", source = "active") // 👈 CORRECCIÓN: 'active' en la entidad es 'isActive' en DTO
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    UserManagementResponse toManagementResponse(User user);


    // --- Mapeo para Actualización ---

    // Este es para actualizar campos de User, si los hubiera. Aquí solo mapeamos la actividad.
    // ... (sin cambios, ya que ignora la mayoría y solo mapea los que el admin debe cambiar)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);


    // --- Mapeo Global para Roles (Sustituye la necesidad del @Mapping con qualifiedByName) ---
    /**
     * Este método 'default' se usa automáticamente para mapear de Set<Role> a Set<RoleName>.
     * No necesita @Named si es un método default en la misma interfaz y MapStruct lo detecta.
     */
    default Set<RoleName> mapRolesToRoleNames(Set<com.sgp.user.model.Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName()) // Retorna el RoleName (el enum)
                .collect(Collectors.toSet());
    }
}