package com.sgp.user.controller;

import com.sgp.security.config.jwt.JwtService;
import com.sgp.user.dto.AuthResponse;
import com.sgp.user.dto.LoginRequest;
import com.sgp.user.dto.RegisterRequest;
import com.sgp.user.model.User;
import com.sgp.user.service.UserService;
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

    private final UserService userService;
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

        // 1. Verificar si el email ya existe (Lógica manejada en el Service)
        try {
            User newUser = userService.registerNewUser(request);

            // 2. Éxito: Aunque aún no generamos JWT, devolvemos una respuesta de éxito.
            // Más adelante, JWT se generará aquí.
            AuthResponse response = AuthResponse.builder()
                    .email(newUser.getEmail())
                    .role(newUser.getRoles().iterator().next().getName().name()) // Solo para demostración
                    .token("PENDIENTE_JWT_GENERATION") // Placeholder
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // 3. Fallo: Devuelve un error 400 (Bad Request) si el email ya existe o hay un error de rol
            // En un caso real, esto se manejaría con un @ControllerAdvice para respuestas uniformes.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint para el login de usuarios.
     * Ruta: POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // 1. Autentica las credenciales (usa el AuthenticationManager)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Si la autenticación es exitosa, genera el JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);

        // Asume que el primer rol es el principal para la respuesta DTO
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        // 3. Devuelve el token al cliente
        AuthResponse response = AuthResponse.builder()
                .token(jwtToken)
                .email(userDetails.getUsername())
                .role(role)
                .build();

        return ResponseEntity.ok(response);
    }
}
