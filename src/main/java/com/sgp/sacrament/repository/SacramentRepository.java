package com.sgp.sacrament.repository;

import com.sgp.sacrament.model.Sacrament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SacramentRepository extends JpaRepository<Sacrament, Long> {

    /**
     * Busca los sacramentos recibidos por una persona específica.
     */
    List<Sacrament> findByPerson_Id(Long personId);

    // ⭐ NUEVO MÉTODO PARA PAGINACIÓN POR PERSONA ⭐
    /**
     * Busca los sacramentos recibidos por una persona específica, con paginación y ordenamiento.
     * @param personId ID de la persona.
     * @param pageable Objeto de paginación y ordenamiento.
     * @return Una Page de Sacrament.
     */
    Page<Sacrament> findByPerson_Id(Long personId, Pageable pageable);

    /**
     * Busca un sacramento por su número de acta, libro y página.
     */
    Optional<Sacrament> findByBookNumberAndPageNumberAndEntryNumber(
            String bookNumber,
            String pageNumber,
            String entryNumber);
}