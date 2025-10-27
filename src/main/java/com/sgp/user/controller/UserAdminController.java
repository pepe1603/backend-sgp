package com.sgp.user.controller;

import com.sgp.user.dto.UserCreationRequest;
import com.sgp.user.dto.UserManagementResponse;
import com.sgp.user.dto.UserUpdateRequest;
import com.sgp.user.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    // -------------------------------------------------------------
    // ⭐ NUEVO ENDPOINT: Crear Usuario (Personal) por Administrador ⭐
    // -------------------------------------------------------------
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN puede crear cuentas de personal/gestores
    @PostMapping
    public ResponseEntity<UserManagementResponse> createUserByAdmin(
            @Valid @RequestBody UserCreationRequest request
    ) {
        UserManagementResponse response = userAdminService.createUserByAdmin(request);
        // Retorna 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 🚀 NUEVO ENDPOINT DE ADMINISTRACIÓN CON PAGINACIÓN Y ORDENAMIENTO

    /**
     * Obtiene una lista paginada de todos los usuarios del sistema.
     * Solo accesible por roles de administración.
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR')")
    @GetMapping
    public ResponseEntity<Page<UserManagementResponse>> getAllUsers(
            @PageableDefault(size = 15, sort = "email", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<UserManagementResponse> responsePage = userAdminService.findAllUsers(pageable);
        return ResponseEntity.ok(responsePage);
    }

    // --- NUEVO: Obtener Usuario por ID ---
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GESTOR', 'COORDINATOR')") // COORDINATOR puede necesitar ver detalles
    @GetMapping("/{id}")
    public ResponseEntity<UserManagementResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userAdminService.getUserById(id));
    }

    // --- NUEVO: Actualizar Roles y Estado (Requiere ADMIN) ---
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserManagementResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userAdminService.updateUser(id, request));
    }

    // --- NUEVO: Borrado Lógico (Requiere ADMIN) ---
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Retorna 204 No Content para operaciones DELETE exitosas
    public void deactivateUser(@PathVariable Long id) {
        userAdminService.deactivateUser(id);
    }

}