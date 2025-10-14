package com.sgp.user.dto;

import com.sgp.common.enums.RoleName;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserManagementResponse {
    private Long id;
    private String email;
    private boolean isActive;
    private boolean isEnabled;
    private Set<RoleName> roles;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private String createdBy; // ✅ Ya existe
    private LocalDateTime updatedAt; // 👈 NUEVO: Auditoría de actualización
    private String updatedBy; // 👈 NUEVO: Auditoría de actualización
}