package com.sgp.user.dto;

import com.sgp.common.enums.RoleName;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para la respuesta de listado y consulta de usuarios
 * en el módulo de administración.
 */
@Data
@Builder
public class UserManagementResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;

    private Set<RoleName> roles;

    // Campos de estado
    private boolean isEnabled;
    private boolean isActive; // Mapeado desde 'active' del Auditable
    private boolean forcePasswordChange;

    // ⭐ CAMPOS DE INACTIVIDAD/ADVERTENCIA ⭐
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastWarningSentDate;

    // Campos de Auditoría
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}