package com.sgp.person.repository;

import com.sgp.person.model.Person;
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

    // Retorna todas las Personas asociadas a un ID de Parroquia específico.
    List<Person> findByParish_Id(Long parishId);

    /**
     * Busca una Persona por el ID del Usuario asociado.
     */
    Optional<Person> findByUser_Id(Long userId); // ⭐ NUEVO MÉTODO DE BÚSQUEDA ⭐
}
