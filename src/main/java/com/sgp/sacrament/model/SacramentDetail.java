package com.sgp.sacrament.model;

import com.sgp.common.model.Auditable;
import com.sgp.person.model.Person;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que almacena los detalles específicos de un sacramento, como padrinos,
 * ministro oficiante y lugar de bautismo (si aplica).
 */
@Entity
@Table(name = "sacrament_details")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SacramentDetail extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relación One-to-One con Sacrament (clave foránea) ---
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sacrament_id", nullable = false)
    private Sacrament sacrament;

    // --- Ministro Oficiante (Puede ser otra Persona) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officiant_minister_id")
    private Person officiantMinister;

    // --- Padrino 1 (Madre/Padrino de Bautismo, etc.) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "godfather_1_id")
    private Person godfather1;

    // --- Padrino 2 (Padre/Madrina de Bautismo, etc.) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "godfather_2_id")
    private Person godfather2;

    // --- Nombre de la Parroquia de Origen o Bautismo (si es diferente a la actual) ---
    @Column(name = "origin_parish_name")
    private String originParishName;

    // --- Nombre de la Diócesis de Origen ---
    @Column(name = "origin_diocese_name")
    private String originDioceseName;

    // --- Información de los Padres (Para Bautismo/Comunión) ---
    // Usaremos los campos de Person para los padres si ya están registrados.
    // Si no, guardaremos el nombre como texto.

    @Column(name = "father_name_text")
    private String fatherNameText;

    @Column(name = "mother_name_text")
    private String motherNameText;

    // *** CAMPOS EXCLUSIVOS PARA MATRIMONIO (opcionales) ***

    // Contrayente 2 (La otra persona en el matrimonio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spouse_id") // Nueva FK para el cónyuge
    private Person spouse;

    // Testigo 1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "witness_1_id") // Nueva FK
    private Person witness1;

    // Testigo 2
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "witness_2_id") // Nueva FK
    private Person witness2;

    // ⭐ NUEVOS CAMPOS: Padres del Cónyuge 2 (Contrayente 2) ⭐
    private String spouseFatherNameText; // Nombre del padre del cónyuge 2
    private String spouseMotherNameText; // Nombre de la madre del cónyuge 2
}