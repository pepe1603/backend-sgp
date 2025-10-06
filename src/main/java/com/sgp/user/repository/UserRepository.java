package com.sgp.user.repository;

import com.sgp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Método crucial para el login y validación de unicidad
    Optional<User> findByEmail(String email);

    // Para verificar rápidamente si un email ya está en uso
    Boolean existsByEmail(String email);
}