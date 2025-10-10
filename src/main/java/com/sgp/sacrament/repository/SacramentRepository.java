package com.sgp.sacrament.repository;

import com.sgp.sacrament.model.Sacrament;
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

    /**
     * Busca un sacramento por su número de acta, libro y página.
     */
    Optional<Sacrament> findByBookNumberAndPageNumberAndEntryNumber(
            String bookNumber,
            String pageNumber,
            String entryNumber);
}