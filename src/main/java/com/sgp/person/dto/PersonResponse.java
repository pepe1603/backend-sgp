package com.sgp.person.dto;

import com.sgp.common.enums.Gender;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PersonResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String phoneNumber;
    private String identificationType;
    private String identificationNumber;
    private boolean isActive;

    // --- Datos de la Parroquia Relacionada ---
    private Long parishId;
    private String parishName;

    // --- Auditoría ---
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}