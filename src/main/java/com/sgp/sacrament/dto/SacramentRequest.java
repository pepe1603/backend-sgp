package com.sgp.sacrament.dto;

import com.sgp.sacrament.enums.SacramentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SacramentRequest {

    // --- Sacrament Fields ---
    @NotNull(message = "El tipo de sacramento es obligatorio.")
    private SacramentType type;

    @NotNull(message = "El ID de la persona que recibe el sacramento es obligatorio.")
    private Long personId;

    @NotNull(message = "El ID de la parroquia donde se celebró es obligatorio.")
    private Long parishId;

    @PastOrPresent(message = "La fecha de celebración no puede ser futura.")
    private LocalDate celebrationDate;

    @NotBlank(message = "El número de libro es obligatorio.")
    private String bookNumber;

    @NotBlank(message = "El número de página es obligatorio.")
    private String pageNumber;

    @NotBlank(message = "El número de acta es obligatorio.")
    private String entryNumber;

    private String notes;

    // --- SacramentDetail Fields ---
    private Long officiantMinisterId; // ID del sacerdote o ministro

    private Long godfather1Id; // ID del primer padrino

    private Long godfather2Id; // ID del segundo padrino

    private String originParishName;

    private String originDioceseName;

    private String fatherNameText; // Nombre del padre (si no es una Person registrada)

    private String motherNameText; // Nombre de la madre (si no es una Person registrada)

    // ⭐ CAMPOS NUEVOS DE MATRIMONIO/TESTIGOS ⭐
    private Long spouseId; // ID del Contrayente 2

    private Long witness1Id; // ID del Testigo 1

    private Long witness2Id; // ID del Testigo 2

    private String spouseFatherNameText; // Padre del cónyuge 2
    private String spouseMotherNameText; // Madre del cónyuge 2
}