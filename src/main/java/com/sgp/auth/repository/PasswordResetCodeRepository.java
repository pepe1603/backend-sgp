package com.sgp.auth.repository;

import com.sgp.auth.model.PasswordResetCode;
import com.sgp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByUser(User user);
    void deleteByUser(User user);
}
