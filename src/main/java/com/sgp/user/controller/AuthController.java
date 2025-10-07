package com.sgp.user.controller;

import com.sgp.security.config.jwt.JwtService;
import com.sgp.user.dto.AuthResponse;
import com.sgp.user.dto.LoginRequest;
import com.sgp.user.dto.RegisterRequest;
import com.sgp.user.dto.RegisterResponse;
import com.sgp.user.model.User;
import com.sgp.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
        // 1. Delega al AuthService. La generación de JWT ya no es inmediata.
        User newUser = authService.registerAndSendVerification(request);

        // 2. Devuelve un mensaje de éxito, indicando que se envió un email.
        // Devolver el nuevo DTO de registro
        RegisterResponse response = RegisterResponse.builder()
                .email(request.getEmail())
                .message("Registro exitoso. Se ha enviado un código de verificación a su email para habilitar la cuenta.")
                .requiresVerification(true)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // 1. Delega toda la lógica de autenticación, generación de JWT,
        // y registro de intentos fallidos al AuthService.
        AuthResponse response = authService.login(request);

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
