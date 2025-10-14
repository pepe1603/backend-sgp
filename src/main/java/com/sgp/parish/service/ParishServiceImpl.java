package com.sgp.parish.service;

import com.sgp.common.exception.ResourceConflictException;
import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.parish.dto.ParishRequest;
import com.sgp.parish.dto.ParishResponse;
import com.sgp.parish.model.Parish;
import com.sgp.parish.repository.ParishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParishServiceImpl implements ParishService {

    private final ParishRepository parishRepository;
    private final ParishMapper parishMapper; // Inyectamos la interfaz de MapStruct

    private static final String RESOURCE_NAME = "Parroquia/Iglesia";
    private static final String FIELD_NAME = "nombre";

    @Override
    @Transactional
    public ParishResponse createParish(ParishRequest request) {
        // Validación: Se asume que el nombre debe ser único.
        if (parishRepository.existsByName(request.getName())) {
            // Usamos una excepción común o puedes crear ParishAlreadyExistsException
            throw new ResourceConflictException(RESOURCE_NAME, FIELD_NAME, request.getName());
        }

        Parish parish = parishMapper.toEntity(request);
        Parish savedParish = parishRepository.save(parish);

        return parishMapper.toResponse(savedParish);
    }

    @Override
    @Transactional(readOnly = true)
    public ParishResponse getParishById(Long id) {
        Parish parish = parishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", id));
        return parishMapper.toResponse(parish);
    }

    // ⭐ NUEVO/MODIFICADO: Implementación de Paginación ⭐
    @Override
    @Transactional(readOnly = true)
    public Page<ParishResponse> findAllParishes(Pageable pageable) {
        // 1. Obtener la página de entidades Parish
        Page<Parish> parishPage = parishRepository.findAll(pageable);

        // 2. Mapear Page<Parish> a Page<ParishResponse>
        return parishPage.map(parishMapper::toResponse);
    }

    @Override
    @Transactional
    public ParishResponse updateParish(Long id, ParishRequest request) {
        Parish parish = parishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", id));

        // Validación de duplicidad si el nombre ha cambiado
        if (!parish.getName().equalsIgnoreCase(request.getName()) && parishRepository.existsByName(request.getName())) {
            throw new ResourceConflictException(RESOURCE_NAME, FIELD_NAME, request.getName());
        }

        // Usamos el método de MapStruct para actualizar solo los campos del DTO
        parishMapper.updateEntityFromRequest(request, parish);

        Parish updatedParish = parishRepository.save(parish);
        return parishMapper.toResponse(updatedParish);
    }

    @Override
    @Transactional
    public void deleteParish(Long id) {
        if (!parishRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, "id", id);
        }
        // Nota: En un sistema real, antes de eliminar, se debe verificar que no tenga
        // entidades dependientes (ej. Sacramentos, Personas) o realizar un borrado lógico.
        parishRepository.deleteById(id);
    }
}
