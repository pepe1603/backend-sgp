package com.sgp.parish.repository;

import com.sgp.parish.model.Parish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ParishRepository extends JpaRepository<Parish, Long> {
    Optional<Parish> findByName(String name);
    boolean existsByName(String name);
}