package com.sgp.sacrament.dto;

import com.sgp.sacrament.enums.SacramentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SacramentResponse {

    private Long id;
    private SacramentType type;
    private String typeDisplayName;

    // Referencia a la persona receptora
    private Long personId;
    private String personFullName;

    // Detalles del Registro
    private LocalDate celebrationDate;
    private String parishName;
    private String bookNumber;
    private String pageNumber;
    private String entryNumber;
    private String notes;

    // Detalles Adicionales (SacramentDetail)
    private SacramentDetailResponse detail;

    @Data
    @Builder
    public static class SacramentDetailResponse {
        private String officiantMinisterName;
        private String godfather1Name;
        private String godfather2Name;
        private String originParishName;
        private String originDioceseName;
        private String fatherNameText;
        private String motherNameText;
    }
}