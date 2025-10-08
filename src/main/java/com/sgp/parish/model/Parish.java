package com.sgp.parish.model;

import com.sgp.common.model.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "parishes")
@EqualsAndHashCode(callSuper = true) // Importante si extiendes de Auditable
public class Parish extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = true, length = 15)
    private String phone;

    @Column(nullable = true, unique = true, length = 100)
    private String email;

    @Column(nullable = true, length = 20)
    private String city;

    // Opcional: Relaciones futuras
    // @OneToMany(mappedBy = "parish", cascade = CascadeType.ALL)
    // private Set<SacramentEnrollment> sacramentEnrollments;
}