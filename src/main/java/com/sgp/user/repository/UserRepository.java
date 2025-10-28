package com.sgp.user.repository;

import com.sgp.common.enums.RoleName;
import com.sgp.user.model.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // ⭐ NUEVA CONSULTA: Actualiza el lastLoginDate eficientemente ⭐
    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = :date WHERE u.email = :email")
    void updateLastLoginDateByEmail(@Param("date") LocalDateTime date, @Param("email") String email);

    // ⭐ NUEVA CONSULTA: Actualiza el estado 'enabled' eficientemente (para la suspensión) ⭐
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.email = :email")
    void updateEnabledStatusByEmail(@Param("enabled") boolean enabled, @Param("email") String email);

    // ⭐ NUEVA CONSULTA: Actualiza la fecha de último aviso de suspensión ⭐
    @Modifying
    @Query("UPDATE User u SET u.lastWarningSentDate = :date WHERE u.id = :id")
    void updateLastWarningSentDate(@Param("id") Long id, @Param("date") LocalDateTime date);


    // ⭐ CONSULTA OPTIMIZADA: Busca usuarios que necesitan pre-aviso de suspensión ⭐
    /**
     * Busca usuarios que están HABILITADOS (isEnabled = true), cuya fecha de último login
     * cae dentro del rango de pre-aviso (ej. inactivos entre 11 y 12 meses)
     * Y que NO han sido avisados recientemente (lastWarningSentDate es null o anterior a la ventana de aviso).
     * * @param suspensionThreshold La fecha más antigua (Hoy - 12 meses). Logins deben ser POSTERIORES.
     * @param warningThreshold La fecha más reciente (Hoy - 11 meses). Logins deben ser ANTERIORES.
     * @return Lista de usuarios que necesitan el pre-aviso.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.person p WHERE u.isEnabled = true " +
            "AND u.lastLoginDate > :suspensionThreshold " +
            "AND u.lastLoginDate < :warningThreshold " +
            "AND (u.lastWarningSentDate IS NULL OR u.lastWarningSentDate < :suspensionThreshold)")
    List<User> findUsersPendingSuspensionWarning(
            @Param("suspensionThreshold") LocalDateTime suspensionThreshold,
            @Param("warningThreshold") LocalDateTime warningThreshold);




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