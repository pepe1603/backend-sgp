package com.sgp.user.profile.controller;

import com.sgp.user.dto.ProfileResponse;
import com.sgp.user.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador dedicado a endpoints donde el usuario gestiona SU PROPIO perfil.
 * (Ej: /me, PUT /me, PUT /me/password)
 */
@RestController
@RequestMapping("/api/v1/users") // Mantenemos el prefijo /users para coherencia REST
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

/**
     * Endpoint para obtener el perfil del usuario autenticado.
     * Requiere JWT válido.
     */

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {

        // 1. Obtener la información del usuario autenticado del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // El username es el email

        // 2. Delegar al servicio para obtener los datos
        ProfileResponse profile = userProfileService.getCurrentUserProfile();

        return ResponseEntity.ok(profile);
   }

    // Pendiente: PUT /me para actualizar perfil, PUT /me/password para cambiar contraseña.
}
