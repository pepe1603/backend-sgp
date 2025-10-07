package com.sgp.user.repository;

import com.sgp.user.model.User;
import com.sgp.user.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    // Para simplificar, puedes buscar el token y luego el usuario.


    void deleteByUser(User user);

}