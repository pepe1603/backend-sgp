package com.sgp.user.profile.controller;

import com.sgp.user.dto.ProfileResponse;
import com.sgp.user.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()") // Mejor usar isAuthenticated() ya que es para el dueño de la sesión.
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {
        ProfileResponse profile = userProfileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
   }

    // Pendiente: PUT /me para actualizar perfil, PUT /me/password para cambiar contraseña.
}
