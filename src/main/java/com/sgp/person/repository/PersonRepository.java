package com.sgp.person.repository;

import com.sgp.person.model.Person;
import com.sgp.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Verifica si una persona ya existe con el mismo tipo y número de identificación.
     * Esto es crucial para la validación de unicidad en el flujo de negocio.
     *
     * @param identificationType El tipo de identificación (ej: DNI, Cédula).
     * @param identificationNumber El número de identificación.
     * @return true si la persona ya existe, false en caso contrario.
     */
    boolean existsByIdentificationTypeAndIdentificationNumber(
            String identificationType,
            String identificationNumber);

    /**
     * Busca una persona por su tipo y número de identificación.
     *
     * @param identificationType Tipo de identificación.
     * @param identificationNumber Número de identificación.
     * @return Un Optional que contiene la Persona si existe.
     */
    Optional<Person> findByIdentificationTypeAndIdentificationNumber(
            String identificationType,
            String identificationNumber);

// ⭐ NUEVO MÉTODO PARA PAGINACIÓN POR PARROQUIA ⭐
    /**
     * Retorna una página de Personas asociadas a un ID de Parroquia específico, aplicando paginación y ordenamiento.
     * @param parishId ID de la parroquia.
     * @param pageable Configuración de paginación y ordenamiento.
     * @return Una Page de Person.
     */
    Page<Person> findByParish_Id(Long parishId, Pageable pageable);

    /**
     * Busca una Persona por el ID del Usuario asociado.
     */
    Optional<Person> findByUser_Id(Long userId); // ⭐ NUEVO MÉTODO DE BÚSQUEDA ⭐

    // ⭐ MÉTODO CRÍTICO para obtener la persona del usuario logueado ⭐
    Optional<Person> findByUser(User user);
}
