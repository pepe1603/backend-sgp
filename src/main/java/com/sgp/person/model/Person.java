package com.sgp.person.model;

import com.sgp.common.enums.Gender;
import com.sgp.common.model.Auditable;
import com.sgp.parish.model.Parish;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "persons", uniqueConstraints = {
        @UniqueConstraint(name = "uc_person_identification", columnNames = {"identification_type", "identification_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Datos Básicos ---
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender; // Usaremos un enum Gender

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // --- Identificación Única (CRUCIAL) ---
    @Column(name = "identification_type", nullable = false, length = 50)
    private String identificationType; // Ej: DNI, Cédula, Pasaporte

    @Column(name = "identification_number", nullable = false, length = 50)
    private String identificationNumber;

    // --- Relación con la Parroquia (La Cesta) ---
    // Clave Foránea: Muchas personas pertenecen a UNA parroquia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parish_id", nullable = false)
    private Parish parish;

    // Campo de estado (Activo/Inactivo)
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}