package com.sgp.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    // El ID de Profile será el mismo ID que el de User
    private Long id;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 20)
    private String phone;
    private String address;

    // Relación OneToOne: Un perfil pertenece a un usuario, y comparten la PK
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
