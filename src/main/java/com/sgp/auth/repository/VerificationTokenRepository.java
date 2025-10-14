package com.sgp.auth.repository;

import com.sgp.user.model.User;
import com.sgp.auth.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    // Para simplificar, puedes buscar el token y luego el usuario.

    // 2. Método de modificación para usar en CleanupService (Seguro y eficiente para lotes)
    // CLAVE: Usar @Modifying para la eliminación directa.
    @Modifying
    void deleteByUser(User user);

}