package com.sgp.auth.service;

import com.sgp.auth.dto.*;
import com.sgp.auth.enums.TokenType;
import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.*;
import com.sgp.common.queue.MailProducer;
import com.sgp.common.service.RandomDataService;
import com.sgp.common.service.TokenService;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.security.config.jwt.JwtService;
import com.sgp.security.service.LoginAttemptService;
import com.sgp.security.service.OtpAttemptService;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.auth.model.VerificationToken;
import com.sgp.user.repository.RoleRepository;
import com.sgp.user.repository.UserRepository;
import com.sgp.auth.repository.VerificationTokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j // Agregado para el log en Magic Link
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
                .user(user)// ⭐ 1. Establece la bidireccionalidad en el lado 'dueño' (Person)
                .build();

        // ⭐ 2. ESTABLECER BIDIRECCIONALIDAD EN EL LADO INVERSO (User)
        user.setPerson(person); // ¡Nuevo paso para completar la bidireccionalidad!
        // ⭐ 3. Guardar SOLO el User (el Cascade.ALL en User guardará Person automáticamente)
        User newUser = userRepository.save(user); // Guardar el User y la Person asociada (profile)
        //personRepository.save(person); //LINEA ELIMINADA por redundancia de cascade.  NO NECESARIO SI SE ESTA USANDO CASCADEaLL EN UNA RELACIÓN BIDIRECCIONAL.

        // 2. Generar y Guardar el Código de Verificación
        String code = tokenService.generateAlphanumericCode(); // Código de 6 dígitos
        VerificationToken verificationToken = new VerificationToken(code, newUser, TokenType.ACCOUNT_VERIFICATION);//Establecer el tyipo de token
        verificationTokenRepository.save(verificationToken);

        // 3. Enviar el Email
        sendVerificationEmail(newUser, code);
        return RegisterResponse.builder()
                .email(newUser.getEmail())
                .fullName(person.getFullName())
                .message("Registro exitoso. Se ha enviado un código de verificación a su email para habilitar la cuenta y completar el registro.")
                .requiresVerification(true)
                .build();
    }

    /**
     * Habilita la cuenta del usuario si el código es válido y no ha expirado.
     */
    @Override
    @Transactional
    public void verifyAccount(String code) {
        VerificationToken token = verificationTokenRepository.findByTokenAndType(code, TokenType.ACCOUNT_VERIFICATION) //Verificar por Token y tipo
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
        VerificationToken verificationToken = new VerificationToken(newCode, user, TokenType.ACCOUNT_VERIFICATION);//Establecer el tipo de token
        verificationTokenRepository.save(verificationToken);

        // 3. Enviar el Email de plantilla de reenvio
        sendVerificationResendEmail(user, newCode);
    }


    // ⭐ IMPLEMENTACIÓN DEL MAGIC LINK - PASO 1: SOLICITUD ⭐
    @Override
    @Transactional
    public void requestMagicLink(MagicLinkRequest request) {
        String email = request.getEmail();

        // 1. Verificar si el usuario existe
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));

        // 2. Verificar si la cuenta está habilitada
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Su cuenta no ha sido verificada, no puede iniciar sesión. Por favor, verifique su email o solicite un reenvío para completar su registro.");
        }

        // 3. Eliminar tokens de verificación antiguos para este usuario (incluyendo el OTP de registro si existía)
        verificationTokenRepository.deleteByUser(user);
        entityManager.flush();

        // 4. Generar y Guardar un token seguro (UUID)
        String token = tokenService.generateSecureToken(); // Usamos un token largo, no un OTP

        VerificationToken magicToken = new VerificationToken(token, user, TokenType.MAGIC_LINK); //usamos toke UUID y establecer el tipo Magic_Link
        // El tiempo de expiración es crucial aquí. Por defecto, VerificationToken usa 2 horas.
        // Si necesitas que el Magic Link expire antes (ej: 15 minutos), debes ajustar ese tiempo en el constructor de VerificationToken.
        verificationTokenRepository.save(magicToken);

        // 5. Enviar el Email con el link
        sendMagicLinkEmail(user, token);
        log.info("Magic Link generado (UUID) para {} con tipo: {}", email, TokenType.MAGIC_LINK);
    }

    // ⭐ IMPLEMENTACIÓN DEL MAGIC LINK - PASO 2: VERIFICACIÓN Y LOGIN ⭐
    @Override
    @Transactional
    public LoginResponse verifyMagicLink(String token) {

        // 1. Buscar y validar el token (tiempo de expiración incluido por el repository)
        VerificationToken magicToken = verificationTokenRepository.findByTokenAndType(token, TokenType.MAGIC_LINK)//Busdcar por token y tipo Magic_Link
                .orElseThrow(() -> new VerificationCodeInvalidException("El Magic Link es inválido o ya ha sido utilizado."));

        // 2. Comprobar Expiración (aunque el repository debería hacerlo, doble check)
        if (magicToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(magicToken);
            throw new VerificationCodeExpiredException("El Magic Link ha expirado. Por favor, solicite uno nuevo.");
        }

        User user = magicToken.getUser();

        // 3. Reconfirmar si el usuario está habilitado (aunque se verificó al solicitarlo)
        if (!user.isEnabled()) {
            // Este caso es poco probable si se verificó en requestMagicLink, pero añade seguridad.
            throw new AccountNotVerifiedException("Su cuenta no está activa. No puede iniciar sesión.");
        }

        // 4. ELIMINAR EL TOKEN (CRUCIAL para seguridad: solo se usa una vez)
        verificationTokenRepository.delete(magicToken);

        // 5. Generar JWT de sesión (equivalente a un login exitoso)
        UserDetails userDetails = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Error interno: Usuario verificado no encontrado."));

        String jwtToken = jwtService.generateToken(userDetails);
        Set<String> roles = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(java.util.stream.Collectors.toSet());

        // 6. Devolver respuesta de login exitoso
        return LoginResponse.builder()
                .token(jwtToken)
                .email(userDetails.getUsername())
                .roles(roles)
                .tokenType("Bearer")
                .build();
    }


    // ⭐ NUEVO MÉTODO DE ENVÍO DE EMAIL PARA MAGIC LINK ⭐
    private void sendMagicLinkEmail(User user, String token) {
        Map<String, Object> model = new HashMap<>();

        personRepository.findByUser(user).ifPresent(person -> {
            model.put("firstName", person.getFirstName());
        });

        // Usar una propiedad para la URL base es la mejor práctica
        String baseUrl = "http://localhost:3000"; // Usar el valor hardcodeado hasta que tengamos un @Value
        String magicLink = baseUrl + "/auth/magic-login?token=" + token; // Simplificado, el email se puede obtener del token en el cliente

        model.put("magicLink", magicLink);

        mailProducer.sendMailMessage(
                user.getEmail(),
                "Inicia Sesión sin Contraseña",
                "email/magic-link-template", // <-- Asume que esta plantilla existe
                model
        );
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
