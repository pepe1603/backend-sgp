package com.sgp.common.model;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter // Usamos @Getter ya que los campos son gestionados por JPA
@MappedSuperclass // No mapear como entidad, solo heredar campos
@EntityListeners(AuditingEntityListener.class) // Habilita la escucha de eventos de JPA para actualizar las fechas
public abstract class Auditable {

    @CreatedDate // Spring Data JPA llena este campo al guardar
    private LocalDateTime createdAt;

    @LastModifiedDate // Spring Data JPA llena este campo al actualizar
    private LocalDateTime updatedAt;
}
