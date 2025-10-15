package com.sgp.auth.controller;

import com.sgp.auth.service.AuthService;
import com.sgp.security.config.jwt.JwtService;
import com.sgp.auth.dto.LoginResponse;
import com.sgp.auth.dto.LoginRequest;
import com.sgp.auth.dto.RegisterRequest;
import com.sgp.auth.dto.RegisterResponse;
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
}
