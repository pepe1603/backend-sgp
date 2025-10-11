package com.sgp.sacrament.model;

import com.sgp.common.model.Auditable;
import com.sgp.parish.model.Parish;
import com.sgp.person.model.Person;
import com.sgp.sacrament.enums.SacramentType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "sacraments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Sacrament extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Clave al Receptor del Sacramento (Persona) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    // --- Tipo de Sacramento ---
    @Enumerated(EnumType.STRING)
    @Column(name = "sacrament_type", nullable = false)
    private SacramentType type;

    // --- Fecha y Lugar de Celebración ---
    @Column(name = "celebration_date", nullable = false)
    private LocalDate celebrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parish_id", nullable = false)
    private Parish parish; // La parroquia donde se celebró

    @Column(name = "book_number", nullable = false)
    private String bookNumber; // Número de libro

    @Column(name = "page_number", nullable = false)
    private String pageNumber; // Número de página

    @Column(name = "entry_number", nullable = false)
    private String entryNumber; // Número de acta

    // Campo adicional para notas o observaciones canónicas
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ⭐ RELACIÓN ONE-TO-ONE CON DETAIL ⭐
    // Mapeado por el campo 'sacrament' en SacramentDetail
    @OneToOne(mappedBy = "sacrament", cascade = CascadeType.ALL, orphanRemoval = true)
    private SacramentDetail sacramentDetail;

    // Campo específico para Matrimonio, Anulación, o datos relevantes
    private String canonicalStatus;
}