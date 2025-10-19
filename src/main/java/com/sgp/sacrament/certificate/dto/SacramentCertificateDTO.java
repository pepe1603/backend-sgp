package com.sgp.sacrament.certificate.dto;

import com.sgp.sacrament.enums.SacramentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO que consolida todos los datos necesarios para generar un certificado o acta sacramental.
 * Actúa como el modelo de datos que se pasa a la plantilla de Thymeleaf.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SacramentCertificateDTO {

    // --- Datos del Sacramento (Tomados de Sacrament.java) ---
    private Long sacramentId;
    private SacramentType tipoSacramento; // Enum: BAPTISM, CONFIRMATION, MARRIAGE, etc.
    private LocalDate fechaCelebracion;
    private String libro;
    private String folio;
    private String asiento;
    private String notas;

    // --- Datos de la Persona (Feligrés que recibe el sacramento) ---
    private String nombreCompletoFeligres;
    private LocalDate fechaNacimientoFeligres;
    private String identificacionFeligres; // ID Type + ID Number

    // --- Datos de la Parroquia de Celebración (Tomados de Parish.java) ---
    private String nombreParroquiaCelebracion;
    private String direccionParroquiaCelebracion;
    private String ciudadParroquiaCelebracion;

    // --- Datos Adicionales (Tomados de SacramentDetail.java) ---
    private String ministroOficianteNombre; // Del objeto Person (officiantMinister)
    private String padrino1Nombre; // Del objeto Person (godfather1)
    private String padrino2Nombre; // Del objeto Person (godfather2)
    private String padreNombreText;   // Nombre del padre (como texto)
    private String madreNombreText;   // Nombre de la madre (como texto)

    // --- ⭐ DATOS EXCLUSIVOS PARA MATRIMONIO (Con campos claros) ⭐ ---
    private String contrayente2NombreCompleto; // El cónyuge (si es Matrimonio)
    private String contrayente2PadreNombreText;
    private String contrayente2MadreNombreText;

    private String testigo1Nombre; // Testigo 1 o Padrino de Confirmación/Bautismo 1 (si no usa godfather1)
    private String testigo2Nombre; // Testigo 2 o Padrino de Confirmación/Bautismo 2 (si no usa godfather2)

    // Opcional: Para seguimiento o validez
    private LocalDate fechaEmisionCertificado = LocalDate.now();
}