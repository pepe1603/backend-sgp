package com.sgp.user.profile.controller;

import com.sgp.user.profile.dto.ProfileResponse;
import com.sgp.user.profile.dto.PasswordUpdateRequest;
import com.sgp.user.profile.dto.ProfileUpdateRequest;
import com.sgp.user.profile.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
     * Endpoint para obtener el perfil del usuario autenticado (GET /api/v1/users/me).
     * Requiere JWT válido.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Mejor usar isAuthenticated() ya que es para el dueño de la sesión.
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {
        ProfileResponse profile = userProfileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Endpoint para actualizar los datos personales del usuario autenticado (PUT /api/v1/users/me).
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse updatedProfile = userProfileService.updateMyProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Endpoint para cambiar la contraseña del usuario autenticado (PUT /api/v1/users/me/password).
     */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changeMyPassword(
            @Valid @RequestBody PasswordUpdateRequest request) {
        userProfileService.changeMyPassword(request);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * ⭐ NUEVO ENDPOINT: Borrado Lógico (Dar de Baja) de la cuenta autenticada (DELETE /api/v1/users/me).
     */
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Retorna 204 No Content
    public void softDeleteMyAccount() {
        userProfileService.softDeleteMyAccount();
    }
}
