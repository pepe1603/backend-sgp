package com.sgp.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter // Usamos @Getter ya que los campos son gestionados por JPA
@Where(clause = "is_active = true") // 👈 AÑADIR: Solo consulta registros activos por defecto
@MappedSuperclass // No mapear como entidad, solo heredar campos
@EntityListeners(AuditingEntityListener.class) // Habilita la escucha de eventos de JPA para actualizar las fechas
public abstract class Auditable {

    @CreatedDate // Spring Data JPA llena este campo al guardar
    private LocalDateTime createdAt;

    @LastModifiedDate // Spring Data JPA llena este campo al actualizar
    private LocalDateTime updatedAt;

    // ⭐ CLAVE: Añadir los campos de usuario de auditoría
    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
    /**
     * Nota: Para que @CreatedBy y @LastModifiedBy funcionen, debes tener una configuración de auditoría en Spring Boot (usando AuditorAware) que le diga a JPA cómo obtener el nombre (o ID) del usuario logueado actualmente.
     * */

    // Campo de estado (Activo/Inactivo) o eliminación lógica, // ⭐ CAMPO DE BORRADO LÓGICO ⭐
    @Setter
    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true" )
    private boolean isActive = true;
}
