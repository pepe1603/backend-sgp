package com.sgp.auth.repository;

import com.sgp.auth.enums.TokenType;
import com.sgp.user.model.User;
import com.sgp.auth.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    // ⭐ Nuevo método para buscar por token Y tipo, asegurando que se use en el contexto correcto ⭐
    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);

    // El método anterior findByToken(String token) ya no es seguro para la validación,
    // pero lo dejamos comentado para recordarlo.
    // Optional<VerificationToken> findByToken(String token);

    // 2. Método de modificación para usar en CleanupService (Seguro y eficiente para lotes)
    // CLAVE: Usar @Modifying para la eliminación directa.
    @Modifying
    void deleteByUser(User user);

}