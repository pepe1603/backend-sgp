package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.*;
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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
//    private final MailService mailService; // Para enviar el email -> eLIMIANRR OP COENMNTAR
    private final TokenService tokenService; // Para generar el código
    private final VerificationTokenRepository verificationTokenRepository; // Nuevo

    private final AuthenticationManager authenticationManager; // ⬅️ Necesitas inyectar esto
    private final JwtService jwtService; // ⬅️ Necesitas inyectar esto
    private final MailProducer mailProducer; // ⬅️ Inyectar el productor
    private final LoginAttemptService loginAttemptService; // ⬅️ Necesitas inyectar esto

    private final RandomDataService randomDataService; //Se neceita apra generar datros aleatorios

    private final EntityManager entityManager;

    /**
     * Lógica de registro con verificación por email.
     */
    @Transactional
    public User registerAndSendVerification(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso.");
        }

        // 1. Crear el User (DESHABILITADO) y Profile
        Role userRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "roleName", RoleName.USER));

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
                .orElseThrow(() -> new VerificationCodeInvalidException("Código de verificación inválido o inexistente."));

        // 1. Comprobar Expiración
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            // Manejar la expiración, posiblemente eliminando el token y lanzando un error
            verificationTokenRepository.delete(token);
            throw new VerificationCodeExpiredException("El código de verificación ha expirado. Por favor, solicite un reenvío.");
        }

        // 2. Habilitar el Usuario
        User user = token.getUser();
        if (user.isEnabled()) {
            throw new AccountAlreadyVerifiedException("La cuenta ya ha sido verificada.");
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

        // ⭐ ¡AQUÍ ESTÁ EL CAMBIO CRÍTICO! ⭐
        mailProducer.sendMailMessage(
                user.getEmail(),
                "Verificación de Cuenta SGP",
                "email/verification-template",
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
            Set<String> roles = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(java.util.stream.Collectors.toSet()); // <-- Obtener todos los roles


            return AuthResponse.builder()
                    .token(jwtToken)
                    .email(userDetails.getUsername())
                    .roles(roles)
                    .tokenType("Bearer")
                    .build();

        } catch (BadCredentialsException e) {
            // 4. Si falla: Registrar el intento fallido en Redis(HTTP 401)
            loginAttemptService.recordFailedAttempt(email);

            // 5. Re-lanzar la excepción para que el GlobalExceptionHandler la capture
            throw e;
        }
    }

    //Nuevo MEtodo
    @Transactional
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        if (user.isEnabled()) {
            // Lanza tu excepción de negocio que el ControllerAdvice maneja como 409
            throw new AccountAlreadyVerifiedException("La cuenta de email " + email + " ya está verificada. Puede iniciar sesión.");
        }

        // 1. Eliminar cualquier token existente
        verificationTokenRepository.deleteByUser(user);

        // ⭐ SOLUCIÓN DEFINITIVA: FORZAR EL FLUSH ⭐
        // Esto obliga a Hibernate a ejecutar el DELETE en la DB de inmediato.
        entityManager.flush();

        // 2. Generar y Guardar el nuevo Código
        String newCode = tokenService.generateAlphanumericCode();
        VerificationToken verificationToken = new VerificationToken(newCode, user);
        verificationTokenRepository.save(verificationToken);

        // 3. Enviar el Email usdanod paltilal de reeenvio
        sendVerificationResendEmail(user, newCode);
    }
    // ⭐ NUEVO MÉTODO DE ENVÍO PARA REENVÍO SOLICITADO POR EL USUARIO ⭐
    private void sendVerificationResendEmail(User user, String code) {
        Map<String, Object> model = new HashMap<>();
        model.put("firstName", user.getProfile().getFirstName());
        model.put("code", code);

        //Cambio critico aqui
        mailProducer.sendMailMessage(
                user.getEmail(),
                "Código de Verificación Solicitado (Reenvío)",
                "email/verification-resend-template", // ⬅️ ¡Nueva plantilla para reenvío!
                model
        );
    }

    // ⭐ NUEVO MÉTODO DE ENVÍO PARA RECORDATORIO ⭐
    private void sendVerificationReminderEmail(User user, String code) {
        Map<String, Object> model = new HashMap<>();
        model.put("firstName", user.getProfile().getFirstName());
        model.put("code", code);
        model.put("expireMinutes", 120); // Ejemplo: indicar que el código actual expira pronto

        //Cambio critico aqui
        mailProducer.sendMailMessage(
                user.getEmail(),
                "Recordatorio: ¡Verifica tu Cuenta SGP!",
                "email/verification-reminder-template", // ⬅️ ¡Nueva plantilla!
                model
        );
    }


}
