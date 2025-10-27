package com.sgp.auth.model;


import com.sgp.auth.enums.TokenType;
import com.sgp.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Data
@NoArgsConstructor
public class VerificationToken {

    // El token expirará 15 minutos después de ser creado
    private static final int EXPIRATION_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se aumenta la longitud para soportar el UUID de Magic Link (36 caracteres) y no solo cortos OTP (6 digitos Alfanumerico)
    @Column(unique = true, nullable = false, length = 255)
    private String token;

    private LocalDateTime expiryDate;

    // ⭐ Nuevo campo para distinguir si es un token de registro o un Magic Link ⭐
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    // Relación OneToOne: Un token pertenece a un usuario
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public VerificationToken(String token, User user, TokenType type) {
        this.token = token;
        this.user = user;
        this.type = type; // Asignar el tipo
        this.expiryDate = calculateExpiryDate();
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }
}