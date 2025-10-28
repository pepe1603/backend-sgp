package com.sgp.auth.controller;

import com.sgp.auth.dto.*;
import com.sgp.auth.service.AuthService;
import com.sgp.security.config.jwt.JwtService;
import com.sgp.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController // Indica que esta clase es un Controller REST
@RequestMapping("/api/v1/auth") // Define la ruta base para todos los endpoints
@RequiredArgsConstructor // Lombok: Inyección de UserService
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService; // Inyectado
    private final AuthenticationManager authenticationManager; // Inyectado

    @GetMapping("/hello")
    public String hello (){
        return "Hello World in Auth";
    }


    /**
     * Endpoint para el registro de nuevos usuarios (Feligreses/USER por defecto).
     * Ruta: POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse reponse = authService.registerAndSendVerification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    // ⭐ NUEVO ENDPOINT PARA VERIFICACIÓN ⭐
    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String code) {
        // El manejo de errores (código inválido/expirado) lo gestionará el @ControllerAdvice
        authService.verifyAccount(code);
        return ResponseEntity.ok("Su cuenta ha sido verificada y habilitada. ¡Bienvenido!");
    }

    /**
     * Endpoint para el login de usuarios.
     * Ruta: POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Delega toda la lógica de autenticación, generación de JWT,
        // y registro de intentos fallidos al AuthService.
        LoginResponse response = authService.login(request);

        // 2. Devuelve la respuesta (Si falla, AuthService lanza excepción
        // que es capturada por GlobalExceptionHandler)
        return ResponseEntity.ok(response);
    }
    // Endpoint para solicitar un nuevo código de verificación
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam String email) {
        authService.resendVerificationCode(email);
        return ResponseEntity.ok("Se ha enviado un nuevo código de verificación a su email.");
    }

    /**
     // ⭐ NUEVO ENDPOINT: SOLICITUD DE MAGIC LINK ⭐
     /**
     * Endpoint para solicitar un Magic Link (Login sin contraseña).
     * Ruta: POST /api/v1/auth/magic-link
     */
    @PostMapping("/magic-link")
    public ResponseEntity<String> requestMagicLink(@Valid @RequestBody MagicLinkRequest request) {
        authService.requestMagicLink(request);
        return ResponseEntity.ok("Se ha enviado un Magic Link a su email para iniciar sesión.");
    }

    // ⭐ NUEVO ENDPOINT: VERIFICACIÓN DE MAGIC LINK ⭐
    /**
     * Endpoint que consume el token del Magic Link (Generalmente llamado desde el email/frontend).
     * Ruta: GET /api/v1/auth/magic-link/verify?token={token}
     * Devuelve el JWT de sesión directamente.
     */
    @GetMapping("/magic-link/verify")
    public ResponseEntity<LoginResponse> verifyMagicLink(@RequestParam String token) {
        LoginResponse response = authService.verifyMagicLink(token);
        // NOTA: En un caso real, el cliente HTTP (navegador) debe manejar esta respuesta
        // (redirección y almacenamiento del token JWT).
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para solicitar un enlace de reactivación de cuenta suspendida.
     * Ruta: POST /api/v1/auth/reactivate/request
     */
    @PostMapping("/reactivate/request")
    public ResponseEntity<String> requestReactivationLink(@Valid @RequestBody ReactivationRequest request) {
        authService.requestReactivationLink(request.getEmail());
        return ResponseEntity.ok("Si la cuenta existe y está suspendida, se ha enviado un enlace de reactivación a su email.");
    }

    /**
     * Endpoint que consume el token de Reactivación.
     * Ruta: POST /api/v1/auth/reactivate/confirm?token={token}
     */
    @PostMapping("/reactivate/confirm")
    public ResponseEntity<String> confirmReactivation(@RequestParam String token) {
        authService.confirmReactivation(token);
        return ResponseEntity.ok("Su cuenta ha sido reactivada exitosamente. Ahora puede iniciar sesión.");
    }
}
