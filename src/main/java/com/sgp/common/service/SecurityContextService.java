package com.sgp.common.service;

import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.common.util.SecurityUtil;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service // Utilizamos @Service ya que inyecta dependencias y contiene lógica de negocio
@RequiredArgsConstructor
public class SecurityContextService {

    private final PersonRepository personRepository;

    private static final Set<String> MANAGEMENT_ROLES = Set.of("ADMIN", "GESTOR", "COORDINATOR");

    // --- Métodos de Ayuda de Seguridad y Búsqueda de Entidades ---

    /**
     * Obtiene la entidad Person asociada al usuario actualmente logueado.
     * @return Entidad Person.
     * @throws ResourceNotFoundException si la Persona no se encuentra o el usuario no está asociado.
     */
    public Person findPersonForCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId(); // Obtiene el ID del contexto puro

        return personRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "Usuario ID", userId));
    }

    /**
     * Verifica si el usuario logueado tiene un rol de gestión (ADMIN, GESTOR, COORDINATOR).
     * @return true si tiene rol de gestión, false en caso contrario.
     */
    public boolean isManagementUser() {
        // Obtenemos los roles directamente del contexto
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(MANAGEMENT_ROLES::contains);
    }
}