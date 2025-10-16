package com.sgp.auth.service;

import com.sgp.auth.dto.RegisterResponse;
import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.*;
import com.sgp.common.queue.MailProducer;
import com.sgp.common.service.RandomDataService;
import com.sgp.common.service.TokenService;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.security.config.jwt.JwtService;
import com.sgp.security.service.LoginAttemptService;
import com.sgp.auth.dto.LoginResponse;
import com.sgp.auth.dto.LoginRequest;
import com.sgp.auth.dto.RegisterRequest;
import com.sgp.security.service.OtpAttemptService;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.auth.model.VerificationToken;
import com.sgp.user.repository.RoleRepository;
import com.sgp.user.repository.UserRepository;
import com.sgp.auth.repository.VerificationTokenRepository;
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
public class AuthServiceImpl implements AuthService {

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
    private final PersonRepository personRepository;
    private final EntityManager entityManager;
    private final OtpAttemptService otpAttemptService;

    /**
     * Lógica de registro con verificación por email.
     */
    @Override
    @Transactional
    public RegisterResponse registerAndSendVerification(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso.");
        }

        // 1. Crear el User (DESHABILITADO) y Person(feligres)
        // 1. Corregir: Asignar el Rol USER (no ADMIN)
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", RoleName.USER));

        /// 2.- Creamos el usuario (DESHABILITADO)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setActive(true); // Asumimos que los recién registrados están activos
        user.setRoles(Collections.singleton(userRole));

        // 3. Crear la entidad Person (¡Reemplazo de Profile!)
        Person person = Person.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .user(user)
                .build();


        User newUser = userRepository.save(user); // Guardar el User y Profile
        personRepository.save(person);
        // 2. Generar y Guardar el Código de Verificación
        String code = tokenService.generateAlphanumericCode(); // Código de 6 dígitos
        VerificationToken verificationToken = new VerificationToken(code, newUser);
        verificationTokenRepository.save(verificationToken);
        // 3. Enviar el Email
        sendVerificationEmail(newUser, code);
        return RegisterResponse.builder()
                .email(newUser.getEmail())
                .fullName(person.getFullName())
                .message("Registro exitoso. Se ha enviado un código de verificación a su email para habilitar la cuenta.")
                .requiresVerification(true)
                .build();
    }

    /**
     * Habilita la cuenta del usuario si el código es válido y no ha expirado.
     */
    @Override
    @Transactional
    public void verifyAccount(String code) {
        VerificationToken token = verificationTokenRepository.findByToken(code)
                .orElseThrow(() -> new VerificationCodeInvalidException("No se encontró un código válido. Solicita uno nuevo o revisa tu bandeja de entrada."));

        otpAttemptService.checkBlockedOrThrow(token.getUser().getEmail(), otpAttemptService.CONTEXT_VERIFY);

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

    // ⭐ NUEVO MÉTODO DE LOGIN EN EL SERVICE ⭐
    @Override
    public LoginResponse login(LoginRequest request) {
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


            return LoginResponse.builder()
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
    @Override
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
        personRepository.findByUser(user).ifPresent(person -> {
            model.put("firstName", person.getFirstName());
        });
        model.put("code", code);

        //Cambio critico aqui
        mailProducer.sendMailMessage(
                user.getEmail(),
                "Código de Verificación Solicitado (Reenvío)",
                "email/verification-resend-template", // ⬅️ ¡Nueva plantilla para reenvío!
                model
        );
    }

    private void sendVerificationEmail(User user, String code) {
        Map<String, Object> model = new HashMap<>();

        personRepository.findByUser(user).ifPresent(person -> {
            model.put("firstName", person.getFirstName());
        });


        model.put("code", code);

        // ⭐ ¡AQUÍ ESTÁ EL CAMBIO CRÍTICO! ⭐
        mailProducer.sendMailMessage(
                user.getEmail(),
                "Verificación de Cuenta SGP",
                "email/verification-template",
                model
        );
    }

    // ⭐ NUEVO MÉTODO DE ENVÍO PARA RECORDATORIO ⭐
    private void sendVerificationReminderEmail(User user, String code) {
        Map<String, Object> model = new HashMap<>();
        personRepository.findByUser(user).ifPresent(person -> {
            model.put("firstName", person.getFirstName());
        });
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
