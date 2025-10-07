package com.sgp.user.repository;

import com.sgp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Método crucial para el login y validación de unicidad
    Optional<User> findByEmail(String email);

    // Para verificar rápidamente si un email ya está en uso
    Boolean existsByEmail(String email);

    /**
     *  Busca usuarios no habilitados creados antes del umbral de tiempo.
     * Esto reemplaza la consulta JPQL anterior, usando el nuevo campo 'createdAt'.
    */
    List<User> findByIsEnabledFalseAndCreatedAtBefore(LocalDateTime threshold);

    // ELIMINAR O COMENTAR LA CONSULTA JPQL ANTERIOR:
    // @Query("SELECT u FROM User u WHERE u.isEnabled = false AND u.id IN (..."
    // List<User> findUnverifiedUsersOlderThan(LocalDateTime threshold);
}