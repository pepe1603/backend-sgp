package com.sgp.sacrament.service;

import com.sgp.common.exception.ResourceConflictException;
import com.sgp.common.exception.ResourceNotAuthorizedException;
import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.parish.model.Parish;
import com.sgp.parish.repository.ParishRepository;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.sacrament.dto.SacramentRequest;
import com.sgp.sacrament.dto.SacramentResponse;
import com.sgp.sacrament.model.Sacrament;
import com.sgp.sacrament.model.SacramentDetail;
import com.sgp.sacrament.repository.SacramentRepository;
import com.sgp.sacrament.repository.SacramentDetailRepository; // Asumo que crearemos este
import com.sgp.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SacramentServiceImpl implements SacramentService {

    private final SacramentRepository sacramentRepository;
    private final SacramentDetailRepository sacramentDetailRepository; // Lo necesitaremos para guardar detalles
    private final PersonRepository personRepository;
    private final ParishRepository parishRepository;
    private final SacramentMapper sacramentMapper;

    private static final String RESOURCE_SACRAMENT = "Sacramento";
    private static final String RESOURCE_PERSON = "Persona";
    private static final String RESOURCE_PARISH = "Parroquia";

    // --- Métodos de Ayuda para Búsqueda y Validación ---

    private Sacrament findSacramentById(Long id) {
        return sacramentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SACRAMENT, "id", id));
    }

    // Busca una entidad Person (ministro, padrino)
    private Person findPersonById(Long id, String role) {
        if (id == null) return null; // Si el ID es nulo, la persona es opcional
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PERSON, role + " ID", id));
    }

    // Busca la entidad Parish
    private Parish findParishById(Long id) {
        return parishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_PARISH, "ID", id));
    }

    // Valida la unicidad del Acta
    private void validateUniqueActa(String book, String page, String entry) {
        if (sacramentRepository.findByBookNumberAndPageNumberAndEntryNumber(book, page, entry).isPresent()) {
            throw new ResourceConflictException(RESOURCE_SACRAMENT, "Acta",
                    String.format("Libro: %s, Página: %s, Acta: %s", book, page, entry));
        }
    }

    // --- CREATE ---
    @Override
    @Transactional
    public SacramentResponse createSacrament(SacramentRequest request) {
        // 1. Validar unicidad del Acta
        validateUniqueActa(request.getBookNumber(), request.getPageNumber(), request.getEntryNumber());

        // 2. Buscar y validar entidades relacionadas
        Person recipient = findPersonById(request.getPersonId(), "Receptora");
        Parish parish = findParishById(request.getParishId());

        Person minister = findPersonById(request.getOfficiantMinisterId(), "Ministro");
        Person godfather1 = findPersonById(request.getGodfather1Id(), "Padrino 1");
        Person godfather2 = findPersonById(request.getGodfather2Id(), "Padrino 2");

        // ⭐ BUSCAR ENTIDADES DE MATRIMONIO/TESTIGOS ⭐
        Person spouse = findPersonById(request.getSpouseId(), "Cónyuge"); // Suponiendo request.getSpouseId() existe
        Person witness1 = findPersonById(request.getWitness1Id(), "Testigo 1"); // Suponiendo request.getWitness1Id() existe
        Person witness2 = findPersonById(request.getWitness2Id(), "Testigo 2"); // Suponiendo request.getWitness2Id() existe

        // 3. Mapear y asignar
        Sacrament sacrament = sacramentMapper.toSacramentEntity(request);
        sacrament.setPerson(recipient);
        sacrament.setParish(parish);

        // 4. Guardar Sacrament
        Sacrament savedSacrament = sacramentRepository.save(sacrament);

        // 5. Crear y guardar SacramentDetail
        SacramentDetail detail = new SacramentDetail();
        detail.setSacrament(savedSacrament);
        detail.setOfficiantMinister(minister);
        detail.setGodfather1(godfather1);
        detail.setGodfather2(godfather2);

        detail.setOriginParishName(request.getOriginParishName());
        detail.setOriginDioceseName(request.getOriginDioceseName());
        detail.setFatherNameText(request.getFatherNameText());
        detail.setMotherNameText(request.getMotherNameText());

        // ⭐ ASIGNAR ENTIDADES DE MATRIMONIO/TESTIGOS ⭐
        detail.setSpouse(spouse);
        detail.setWitness1(witness1);
        detail.setWitness2(witness2);
        detail.setSpouseFatherNameText(request.getSpouseFatherNameText());
        detail.setSpouseMotherNameText(request.getSpouseMotherNameText());

        SacramentDetail savedDetail = sacramentDetailRepository.save(detail);
        savedSacrament.setSacramentDetail(savedDetail); // Enlazar el detalle de vuelta al sacramento

        // 6. Responder
        return sacramentMapper.toResponse(savedSacrament, savedDetail);
    }

    // --- READ (Single) ---
    @Override
    @Transactional(readOnly = true)
    public SacramentResponse getSacramentById(Long id) {
        Sacrament sacrament = findSacramentById(id);
        // Cargar el detalle explícitamente si no está cargado por defecto (Lazy)
        SacramentDetail detail = sacrament.getSacramentDetail();
        return sacramentMapper.toResponse(sacrament, detail);
    }

    // ⭐ NUEVO/MODIFICADO: Implementación de Paginación y Filtrado Unificado ⭐
    @Override
    @Transactional(readOnly = true)
    public Page<SacramentResponse> findAllSacraments(Long personId, Pageable pageable) {
        Page<Sacrament> sacramentPage;

        // 1. Decidir si filtrar por persona
        if (personId != null) {
            // Opcional: Validar la existencia de la persona si no se espera que el filtro falle
            if (!personRepository.existsById(personId)) {
                throw new ResourceNotFoundException(RESOURCE_PERSON, "id", personId);
            }
            sacramentPage = sacramentRepository.findByPerson_Id(personId, pageable);
        } else {
            // 2. Si no hay filtro, buscar todos paginados
            sacramentPage = sacramentRepository.findAll(pageable);
        }

        // 3. Mapear Page<Sacrament> a Page<SacramentResponse>
        return sacramentPage.map(s -> sacramentMapper.toResponse(s, s.getSacramentDetail()));
    }

    // --- READ (By Person) ---
    @Override
    @Transactional(readOnly = true)
    public List<SacramentResponse> getSacramentsByPersonId(Long personId) {
        return sacramentRepository.findByPerson_Id(personId).stream()
                .map(s -> sacramentMapper.toResponse(s, s.getSacramentDetail()))
                .collect(Collectors.toList());
    }

    // --- UPDATE ---
    @Override
    @Transactional
    public SacramentResponse updateSacrament(Long id, SacramentRequest request) {
        Sacrament existingSacrament = findSacramentById(id);
        SacramentDetail existingDetail = existingSacrament.getSacramentDetail();

        // 1. Validar unicidad si los números de Acta han cambiado
        boolean actaChanged = !existingSacrament.getBookNumber().equals(request.getBookNumber()) ||
                !existingSacrament.getPageNumber().equals(request.getPageNumber()) ||
                !existingSacrament.getEntryNumber().equals(request.getEntryNumber());

        if (actaChanged) {
            sacramentRepository.findByBookNumberAndPageNumberAndEntryNumber(
                            request.getBookNumber(), request.getPageNumber(), request.getEntryNumber())
                    .filter(s -> !s.getId().equals(id)) // Asegurar que no sea este mismo registro
                    .ifPresent(s -> {
                        throw new ResourceConflictException(RESOURCE_SACRAMENT, "Acta", "ya existe");
                    });
        }

        // 2. Buscar y validar entidades relacionadas para actualizar
        Person recipient = findPersonById(request.getPersonId(), "Receptora");
        Parish parish = findParishById(request.getParishId());

        Person minister = findPersonById(request.getOfficiantMinisterId(), "Ministro");
        Person godfather1 = findPersonById(request.getGodfather1Id(), "Padrino 1");
        Person godfather2 = findPersonById(request.getGodfather2Id(), "Padrino 2");

        // ⭐ BUSCAR ENTIDADES DE MATRIMONIO/TESTIGOS ⭐
        Person spouse = findPersonById(request.getSpouseId(), "Cónyuge");
        Person witness1 = findPersonById(request.getWitness1Id(), "Testigo 1");
        Person witness2 = findPersonById(request.getWitness2Id(), "Testigo 2");



        // 3. Actualizar Sacrament (Usando el mapper para los campos básicos, si es posible)
        // Ya que MapStruct no soporta el mapeo de Request a Entity sobre un existente fácilmente con IDs
        // haremos la actualización manual de los campos.
        existingSacrament.setType(request.getType());
        existingSacrament.setCelebrationDate(request.getCelebrationDate());
        existingSacrament.setBookNumber(request.getBookNumber());
        existingSacrament.setPageNumber(request.getPageNumber());
        existingSacrament.setEntryNumber(request.getEntryNumber());
        existingSacrament.setNotes(request.getNotes());

        // Actualizar relaciones si han cambiado
        if (!existingSacrament.getPerson().getId().equals(recipient.getId())) {
            existingSacrament.setPerson(recipient);
        }
        if (!existingSacrament.getParish().getId().equals(parish.getId())) {
            existingSacrament.setParish(parish);
        }

        // 4. Actualizar SacramentDetail
        existingDetail.setOfficiantMinister(minister);
        existingDetail.setGodfather1(godfather1);
        existingDetail.setGodfather2(godfather2);
        existingDetail.setOriginParishName(request.getOriginParishName());
        existingDetail.setOriginDioceseName(request.getOriginDioceseName());
        existingDetail.setFatherNameText(request.getFatherNameText());
        existingDetail.setMotherNameText(request.getMotherNameText());


        // ⭐ ACTUALIZAR ENTIDADES DE MATRIMONIO/TESTIGOS ⭐
        existingDetail.setSpouse(spouse);
        existingDetail.setWitness1(witness1);
        existingDetail.setWitness2(witness2);
        // ⭐ ASIGNAR NUEVOS CAMPOS ⭐
        existingDetail.setSpouseFatherNameText(request.getSpouseFatherNameText());
        existingDetail.setSpouseMotherNameText(request.getSpouseMotherNameText());
        // Guardar ambos (Detail se guardará por la cascada si es @OneToOne(cascade=ALL) en Sacrament,
        // pero lo haremos explícitamente si lo creamos)
        // Por ahora, confiamos en que Spring Data JPA gestione la persistencia del Detail
        // o que la salvación explícita en el detail repo sea necesaria.
        // Si tienes una relación bidireccional adecuada, solo guardar el Sacrament es suficiente.
        Sacrament updatedSacrament = sacramentRepository.save(existingSacrament);

        // 5. Responder
        return sacramentMapper.toResponse(updatedSacrament, existingDetail);
    }

    // --- DELETE (Lógico) ---
    @Override
    @Transactional
    public void deleteSacrament(Long id) {
        Sacrament sacrament = findSacramentById(id);

        // ⭐ IMPLEMENTACIÓN DE ELIMINACIÓN LÓGICA ⭐
        if (!sacrament.isActive()) {
            // Opcional: lanzar una excepción si ya está inactivo
            throw new ResourceConflictException(RESOURCE_SACRAMENT, "estado", "ya está inactivo");
        }
        // Nota: Si SacramentDetail también debe ser marcado,
        // Pero dado que es un registro canónico, el principal es el Sacrament.
        // 1. Eliminación Lógica del Sacramento principal
        sacrament.setActive(false);
        sacramentRepository.save(sacrament);

        // 2. Eliminación Lógica del Detalle asociado (CRÍTICO)
        // El 'detail' debe cargarse junto con el sacramento, o buscarse por separado.
        SacramentDetail detail = sacrament.getSacramentDetail();
        if (detail != null && detail.isActive()) {
            detail.setActive(false);
            sacramentDetailRepository.save(detail);
        }
    }

    // --- READ (My Sacraments) ---
    @Override
    @Transactional(readOnly = true)
    public List<SacramentResponse> getMySacraments() {
        // 1. Obtener el User ID del contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // El 'principal' es el objeto UserDetails (que en tu caso es la entidad User)
        // pero necesitamos el ID del User para buscar la Person.

        Long userId ;
        if (auth.getPrincipal() instanceof UserDetails) {
            // Asumiendo que tu entidad User implementa UserDetails y tiene el ID
            userId = ((User) auth.getPrincipal()).getId();
        } else {
            // En caso de que no sea un usuario autenticado (debería ser filtrado por @PreAuthorize)
            throw new ResourceNotAuthorizedException("Acceso no autorizado al recurso personal.");
        }

        // 2. Buscar la Persona asociada a este User ID
        Person person = personRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "Usuario ID", userId));

        // 3. Llamar a la función existente con el ID de la Persona canónica
        return getSacramentsByPersonId(person.getId());
    }

}