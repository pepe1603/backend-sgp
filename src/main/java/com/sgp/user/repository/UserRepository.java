package com.sgp.user.repository;

import com.sgp.common.enums.RoleName;
import com.sgp.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Cuenta el número de usuarios que tienen el rol especificado.
     * Útil para validar que siempre haya al menos un administrador.
     * @param name El nombre del Rol (e.g., RoleName.ADMIN).
     * @return El número de usuarios con ese rol.
     */
    long countByRolesName(RoleName name);

    /**
     * Optimiza la búsqueda de usuarios para el panel de administración
     * cargando sus roles (EAGER) y su Persona asociada (LEFT JOIN FETCH) en una sola consulta.
     * Esto evita N+1 queries al acceder a la información de la Persona.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH Person p ON p.user = u") // Usa la relación 'user' de Person
    Page<User> findAllUsersWithPerson(Pageable pageable); // 👈 NUEVO MÉTODO DE BÚSQUEDA OPTIMIZADO

    // Buscar usuario junto con su Person (evita N+1)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.person WHERE u.id = :id")
    Optional<User> findByIdWithPerson(Long id);

    // Buscar todos los usuarios con su Person (paginado)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.person")
    Page<User> findAllWithPerson(Pageable pageable);
}