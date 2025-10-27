package com.sgp.auth.service;

import com.sgp.auth.dto.*;
import com.sgp.auth.enums.TokenType;
import com.sgp.auth.model.VerificationToken;
import com.sgp.auth.repository.VerificationTokenRepository;
import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.*;
import com.sgp.common.queue.MailProducer;
import com.sgp.common.service.TokenService;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.security.config.jwt.JwtService;
import com.sgp.security.service.LoginAttemptService;
import com.sgp.security.service.OtpAttemptService;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.user.repository.RoleRepository;
import com.sgp.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    // Nota: Aunque no lo inyectamos aquí, usamos una variable de configuración para la URL base
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailProducer mailProducer;
    private final LoginAttemptService loginAttemptService;
    private final PersonRepository personRepository;
    private final EntityManager entityManager;
    private final OtpAttemptService otpAttemptService;

    // --- FLUJO DE REGISTRO (CON VERIFICACIÓN POR ENLACE SEGURO) ---

    @Override
    @Transactional
    public RegisterResponse registerAndSendVerification(RegisterRequest request) {

        // 1. VALIDACIÓN: Email ya en uso.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso.");
        }

        // 2. CREACIÓN DEL USUARIO BASE
        User newUser = createUserBase(request);

        // 3. INTENTO DE VINCULACIÓN O REGISTRO SIMPLE
        Optional<Person> personToLink = Optional.empty();

        if (isLinkingAttempt(request)) {
            // CASO 1: VINCULACIÓN (FELIGRÉS)
            personToLink = linkPersonToUser(newUser, request);
        }else{
            //Caso 2: CONTINUAR CON EL REGISTRO DE USER SIN PERSONA VINCULADA
        }

        // 4. GUARDAR EL NUEVO USUARIO
        User savedUser = userRepository.save(newUser);

        // 5. GENERAR Y ENVIAR ENLACE SEGURO DE VERIFICACIÓN
        String token = generateAndSaveVerificationToken(savedUser, TokenType.ACCOUNT_VERIFICATION);
        sendVerificationLinkEmail(savedUser, token); // Nuevo método de envío

        // 6. CONSTRUIR RESPUESTA
        String fullName = personToLink.map(Person::getFullName)
                .orElse(request.getFirstName() + " " + request.getLastName());

        return RegisterResponse.builder()
                .email(savedUser.getEmail())
                .fullName(fullName)
                .message("Registro exitoso. Se ha enviado un enlace seguro de verificación a su email para habilitar la cuenta.")
                .requiresVerification(true)
                .build();
    }

    /**
     * Habilita la cuenta del usuario si el token es válido y no ha expirado.
     * Ahora utiliza el mismo flujo de verificación de token largo que MagicLink.
     */
    @Override
    @Transactional
    public void verifyAccount(String token) {
        // Buscar el token por el valor y el tipo.
        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndType(token, TokenType.ACCOUNT_VERIFICATION)
                .orElseThrow(() -> new VerificationCodeInvalidException("El enlace de verificación es inválido o ya ha sido utilizado."));

        // No se necesita checkBlockedOrThrow aquí, ya que no es un intento de OTP.

        // 1. Comprobar Expiración
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new VerificationCodeExpiredException("El enlace de verificación ha expirado. Por favor, solicite un reenvío.");
        }

        // 2. Habilitar el Usuario
        User user = verificationToken.getUser();
        if (user.isEnabled()) {
            // 3. Eliminar token y lanzar excepción si ya está verificada
            verificationTokenRepository.delete(verificationToken);
            throw new AccountAlreadyVerifiedException("La cuenta ya ha sido verificada.");
        }
        user.setEnabled(true);
        userRepository.save(user);

        // 3. Eliminar el Token (consumido)
        verificationTokenRepository.delete(verificationToken);
    }

    /**
     * Genera un nuevo token de verificación (UUID) y lo reenvía al usuario.
     */
    @Override
    @Transactional
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        if (user.isEnabled()) {
            throw new AccountAlreadyVerifiedException("La cuenta de email " + email + " ya está verificada. Puede iniciar sesión.");
        }

        // 1. Eliminar cualquier token existente
        verificationTokenRepository.deleteByUser(user);
        entityManager.flush();

        // 2. Generar y Guardar el nuevo Token UUID
        String newToken = generateAndSaveVerificationToken(user, TokenType.ACCOUNT_VERIFICATION);

        // 3. Enviar el Email con el nuevo enlace seguro
        sendVerificationLinkResendEmail(user, newToken);
    }

    // --- MÉTODOS PRIVADOS AUXILIARES ---

    /**
     * Genera un token UUID seguro, lo guarda en la DB con el tipo especificado y lo retorna.
     */
    private String generateAndSaveVerificationToken(User user, TokenType type) {
        String token = tokenService.generateSecureToken(); // Usa token UUID largo
        VerificationToken verificationToken = new VerificationToken(token, user, type);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    private User createUserBase(RegisterRequest request) {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", RoleName.USER));

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(false);
        newUser.setActive(true);
        newUser.setRoles(Collections.singleton(userRole));
        return newUser;
    }

    private boolean isLinkingAttempt(RegisterRequest request) {
        return request.getIdentificationType() != null && !request.getIdentificationType().isEmpty() &&
                request.getIdentificationNumber() != null && !request.getIdentificationNumber().isEmpty();
    }

    private Optional<Person> linkPersonToUser(User newUser, RegisterRequest request) {

        Optional<Person> personToLink = personRepository
                .findByIdentificationTypeAndIdentificationNumberAndUserIsNull(
                        request.getIdentificationType(),
                        request.getIdentificationNumber());

        if (personToLink.isEmpty()) {
            throw new ResourceNotFoundException("Identificación", "número", "La identificación proporcionada no corresponde a un registro de feligrés disponible. Verifique sus datos o regístrese como usuario simple si aplica.");
        }

        Person existingPerson = personToLink.get();
        if (!existingPerson.getFirstName().equalsIgnoreCase(request.getFirstName()) ||
                !existingPerson.getLastName().equalsIgnoreCase(request.getLastName())) {
            throw new ResourceConflictException("Datos", "Vinculación", "Los nombres y apellidos proporcionados no coinciden con la persona registrada bajo esa identificación.");
        }

        existingPerson.setUser(newUser);
        newUser.setPerson(existingPerson);

        return Optional.of(existingPerson);
    }

    // --- MÉTODOS DE ENVÍO DE EMAILS (ADAPTADOS) ---

    // 1. Envío inicial de verificación
    private void sendVerificationLinkEmail(User user, String token) {
        Map<String, Object> model = createEmailModel(user);

        // Crear el enlace de verificación. La URL debe apuntar al endpoint frontend que llama a verifyAccount.
        String verificationLink = frontendBaseUrl + "/auth/verify-account?token=" + token;

        model.put("verificationLink", verificationLink);

        mailProducer.sendMailMessage(
                user.getEmail(),
                "Verificación de Cuenta SGP (Paso 1)",
                "email/verification-link-template", // Se asume una plantilla que usa un link
                model
        );
    }

    // 2. Reenvío de verificación
    private void sendVerificationLinkResendEmail(User user, String token) {
        Map<String, Object> model = createEmailModel(user);

        String verificationLink = frontendBaseUrl + "/auth/verify-account?token=" + token;

        model.put("verificationLink", verificationLink);

        mailProducer.sendMailMessage(
                user.getEmail(),
                "Reenvío de Enlace de Verificación SGP",
                "email/verification-resend-link-template", // Se asume una nueva plantilla para reenvío de link
                model
        );
    }


    // --- FLUJOS DE AUTENTICACIÓN  ---

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();

        try {

            //verficar si esta habilitado
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));

            if (!user.isEnabled()) {
                throw new AccountNotVerifiedException("Su cuenta no ha sido verificada. Por favor, verifique su email o solicite un reenvío.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            loginAttemptService.recordSuccessfulAttempt(email);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);
            Set<String> roles = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(java.util.stream.Collectors.toSet());

            return LoginResponse.builder()
                    .token(jwtToken)
                    .email(userDetails.getUsername())
                    .roles(roles)
                    .tokenType("Bearer")
                    .build();

        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailedAttempt(email);
            throw e;
        }
    }

    @Override
    @Transactional
    public void requestMagicLink(MagicLinkRequest request) {
        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Su cuenta no ha sido verificada. Por favor, verifique su email o solicite un reenvío.");
        }

        verificationTokenRepository.deleteByUser(user);
        entityManager.flush();

        String token = generateAndSaveVerificationToken(user, TokenType.MAGIC_LINK);
        sendMagicLinkEmail(user, token);
        log.info("Magic Link generado (UUID) para {} con tipo: {}", email, TokenType.MAGIC_LINK);
    }

    @Override
    @Transactional
    public LoginResponse verifyMagicLink(String token) {
        VerificationToken magicToken = verificationTokenRepository.findByTokenAndType(token, TokenType.MAGIC_LINK)
                .orElseThrow(() -> new VerificationCodeInvalidException("El Magic Link es inválido o ya ha sido utilizado."));

        if (magicToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(magicToken);
            throw new VerificationCodeExpiredException("El Magic Link ha expirado. Por favor, solicite uno nuevo.");
        }

        User user = magicToken.getUser();
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Su cuenta no está activa. No puede iniciar sesión.");
        }

        verificationTokenRepository.delete(magicToken);

        UserDetails userDetails = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Error interno: Usuario verificado no encontrado."));

        String jwtToken = jwtService.generateToken(userDetails);
        Set<String> roles = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(java.util.stream.Collectors.toSet());

        return LoginResponse.builder()
                .token(jwtToken)
                .email(userDetails.getUsername())
                .roles(roles)
                .tokenType("Bearer")
                .build();
    }

    // --- MÉTODOS DE ENVÍO DE EMAILS (EXISTENTES) ---

    // Método para crear el modelo base y obtener el nombre
    private Map<String, Object> createEmailModel(User user) {
        Map<String, Object> model = new HashMap<>();
        // Intenta obtener el nombre de la persona vinculada para el email
        personRepository.findByUser(user).ifPresent(person -> {
            model.put("firstName", person.getFirstName());
        });
        return model;
    }

    private void sendMagicLinkEmail(User user, String token) {
        Map<String, Object> model = createEmailModel(user);

        // Usar la propiedad de configuración
        String magicLink = frontendBaseUrl + "/auth/magic-login?token=" + token;

        model.put("magicLink", magicLink);

        mailProducer.sendMailMessage(
                user.getEmail(),
                "Inicia Sesión sin Contraseña",
                "email/magic-link-template",
                model
        );
    }

    // El método de recordatorio no usa el token corto, sino que reenviaría el enlace seguro.
    // Lo adapto para que use el nuevo flujo de link, si es llamado.
    private void sendVerificationReminderEmail(User user, String token) {
        Map<String, Object> model = createEmailModel(user);

        String verificationLink = frontendBaseUrl + "/auth/verify-account?token=" + token;

        model.put("verificationLink", verificationLink);
        model.put("expireMinutes", 120);

        mailProducer.sendMailMessage(
                user.getEmail(),
                "Recordatorio: ¡Verifica tu Cuenta SGP!",
                "email/verification-reminder-template",
                model
        );
    }
}
