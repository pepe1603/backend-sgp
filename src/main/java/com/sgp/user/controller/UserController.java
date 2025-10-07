package com.sgp.user.controller;

import com.sgp.user.dto.ProfileResponse;
import com.sgp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Endpoint para obtener el perfil del usuario autenticado.
     * Requiere JWT válido.
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {

        // 1. Obtener la información del usuario autenticado del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // El username es el email

        // 2. Delegar al servicio para obtener los datos
        ProfileResponse profile = userService.getUserProfile(username);

        return ResponseEntity.ok(profile);
    }
}