package com.sgp.person.model;

import com.sgp.common.enums.Gender;
import com.sgp.common.model.Auditable;
import com.sgp.parish.model.Parish;
import com.sgp.user.model.User;
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

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender; // Usaremos un enum Gender

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String address;

    // --- Identificación Única (CRUCIAL) ---
    @Column(name = "identification_type", length = 50)
    private String identificationType; // Ej: DNI, Cédula, Pasaporte

    @Column(name = "identification_number",  length = 50)
    private String identificationNumber;

    // --- Relación con la Parroquia (La Cesta) ---
    // Clave Foránea: Muchas personas pertenecen a UNA parroquia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parish_id")
    private Parish parish;

    // --- Relación Opcional con Usuario de Sistema ---
    // Mapeo: UNA persona puede estar asociada a UN usuario (feligrés).
    // Usamos LAZY para evitar bucles de carga accidental.
    // Usamos 'unique = true' para asegurar que un User solo se asocie a una Person.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = true) // 'user_id' será la FK
    private User user;

    // ⭐ MÉTODO CALCULADO PARA OBTENER EL NOMBRE COMPLETO ⭐
    @Transient // Indica a JPA que este campo no debe ser persistido en la DB
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}