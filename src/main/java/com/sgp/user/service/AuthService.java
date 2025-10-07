package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.EmailAlreadyExistsException;
import com.sgp.common.service.MailService;
import com.sgp.common.service.RandomDataService;
import com.sgp.common.service.TokenService;
import com.sgp.security.config.jwt.JwtService;
import com.sgp.security.service.LoginAttemptService;
import com.sgp.user.dto.AuthResponse;
import com.sgp.user.dto.LoginRequest;
import com.sgp.user.dto.RegisterRequest;
import com.sgp.user.model.Profile;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.user.model.VerificationToken;
import com.sgp.user.repository.RoleRepository;
import com.sgp.user.repository.UserRepository;
import com.sgp.user.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService; // Para enviar el email
    private final TokenService tokenService; // Para generar el código
    private final VerificationTokenRepository verificationTokenRepository; // Nuevo

    private final AuthenticationManager authenticationManager; // ⬅️ Necesitas inyectar esto
    private final JwtService jwtService; // ⬅️ Necesitas inyectar esto
    private final LoginAttemptService loginAttemptService; // ⬅️ Necesitas inyectar esto

    private final RandomDataService randomDataService; //Se neceita apra generar datros aleatorios

    /**
     * Lógica de registro con verificación por email.
     */
    @Transactional
    public User registerAndSendVerification(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso.");
        }

        // 1. Crear el User (DESHABILITADO) y Profile
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role no encontrado."));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false); // <--- Clave: Inicia deshabilitado
        user.setRoles(Collections.singleton(userRole));

        Profile profile = new Profile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(randomDataService.generateRandomPhoneNumber());
        profile.setAddress(randomDataService.generateRandomAddress());
        profile.setUser(user);
        user.setProfile(profile);

        User newUser = userRepository.save(user); // Guardar el User y Profile

        // 2. Generar y Guardar el Código de Verificación
        String code = tokenService.generateAlphanumericCode(); // Código de 6 dígitos
        VerificationToken verificationToken = new VerificationToken(code, newUser);
        verificationTokenRepository.save(verificationToken);
        // 3. Enviar el Email
        sendVerificationEmail(newUser, code);
        return newUser;
    }

    /**
     * Habilita la cuenta del usuario si el código es válido y no ha expirado.
     */
    @Transactional
    public void verifyAccount(String code) {
        VerificationToken token = verificationTokenRepository.findByToken(code)
                .orElseThrow(() -> new RuntimeException("Código de verificación inválido."));

        // 1. Comprobar Expiración
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            // Manejar la expiración, posiblemente eliminando el token y lanzando un error
            verificationTokenRepository.delete(token);
            throw new RuntimeException("El código de verificación ha expirado.");
        }

        // 2. Habilitar el Usuario
        User user = token.getUser();
        if (user.isEnabled()) {
            throw new RuntimeException("La cuenta ya ha sido verificada.");
        }
        user.setEnabled(true);
        userRepository.save(user);

        // 3. Eliminar el Token (Ya no es necesario)
        verificationTokenRepository.delete(token);
    }

    private void sendVerificationEmail(User user, String code) {
        Map<String, Object> model = new HashMap<>();
        model.put("firstName", user.getProfile().getFirstName());
        model.put("code", code);

        mailService.sendHtmlMail(
                user.getEmail(),
                "Verificación de Cuenta SGP",
                "email/verification-template", // <-- Debe existir en src/main/resources/templates/email/
                model
        );
    }

    // ⭐ NUEVO MÉTODO DE LOGIN EN EL SERVICE ⭐
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        try {
            // 1. Autenticar las credenciales (SecurityConfig ya revisó si está Locked)

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );

            // 2. Si la autenticación es exitosa, resetear el contador de Redis
            loginAttemptService.recordSuccessfulAttempt(email);

            // 3. Generar JWT y construir la respuesta
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            return AuthResponse.builder()
                    .token(jwtToken)
                    .email(userDetails.getUsername())
                    .role(role)
                    .build();

        } catch (BadCredentialsException e) {
            // 4. Si falla: Registrar el intento fallido en Redis(HTTP 401)
            loginAttemptService.recordFailedAttempt(email);

            // 5. Re-lanzar la excepción para que el GlobalExceptionHandler la capture
            throw e;
        }
    }
}
