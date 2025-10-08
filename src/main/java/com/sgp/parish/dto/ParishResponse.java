package com.sgp.parish.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParishResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String city;

    // --- Campos de Auditoría (Añadir los faltantes) ---
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt; // <--- Añadido
    private String updatedBy;   // <--- Añadido
    // ----------------------------------------------------
}