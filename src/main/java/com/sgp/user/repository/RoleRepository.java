package com.sgp.user.repository;

import com.sgp.common.enums.RoleName;
import com.sgp.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Buscar un rol por su nombre (ej. encontrar el objeto 'Role' para 'USER')
    Optional<Role> findByName(RoleName name);
}