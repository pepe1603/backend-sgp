package com.sgp.auth.service;

import com.sgp.auth.model.PasswordResetCode;
import com.sgp.auth.repository.PasswordResetCodeRepository;
import com.sgp.common.exception.ResourceValidException;
import com.sgp.common.exception.VerificationCodeExpiredException;
import com.sgp.common.exception.VerificationCodeInvalidException;
import com.sgp.common.queue.MailProducer;
import com.sgp.common.service.RandomDataService;
import com.sgp.common.service.TokenService;
import com.sgp.person.repository.PersonRepository;
import com.sgp.security.service.OtpAttemptService;
import com.sgp.user.model.User;
import com.sgp.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository resetCodeRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final MailProducer mailProducer;
    private  final PersonRepository personRepository;
    private final OtpAttemptService otpAttemptService;


    private final EntityManager entityManager;

    @Override
    @Transactional
    public void requestResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        resetCodeRepository.deleteByUser(user);
        entityManager.flush(); // Evita duplicados

        String code = tokenService.generateAlphanumericCode();
        PasswordResetCode resetCode = new PasswordResetCode(code, user);
        resetCodeRepository.save(resetCode);

        Map<String, Object> model = new HashMap<>();

        personRepository.findByUser(user).ifPresent(person -> {
            model.put("firstName", person.getFirstName());
        });
        model.put("code", code);

        mailProducer.sendMailMessage(
                email,
                "Código de Recuperación de Contraseña",
                "email/password-reset-template",
                model
        );
    }

    @Override
    public void verifyResetCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        // Verificar si está bloqueado
        otpAttemptService.checkBlockedOrThrow(user.getEmail(), otpAttemptService.CONTEXT_RESET);

        PasswordResetCode resetCode = resetCodeRepository.findByUser(user)
                .orElseThrow(() -> {
                    otpAttemptService.recordFailedAttempt(email, otpAttemptService.CONTEXT_RESET); // Registrar intento fallido
                    return new VerificationCodeInvalidException("No se encontró un código válido. Solicita uno nuevo o revisa tu bandeja de entrada.");
                });

        if (!resetCode.getCode().equals(code)) {
            otpAttemptService.recordFailedAttempt(email, otpAttemptService.CONTEXT_RESET); // Registrar intento fallido
            throw new VerificationCodeInvalidException("El código ingresado es incorrecto.");
        }

        if (resetCode.isExpired()) {
            resetCodeRepository.delete(resetCode);
            otpAttemptService.recordFailedAttempt(email, otpAttemptService.CONTEXT_RESET); // Registrar intento fallido
            throw new VerificationCodeExpiredException("El código ha expirado.");
        }

        // Código válido
        otpAttemptService.recordSuccessfulAttempt(email, otpAttemptService.CONTEXT_RESET); // Limpiar intentos
    }


    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        PasswordResetCode resetCode = resetCodeRepository.findByUser(user)
                .orElseThrow(() -> new ResourceValidException("Debe verificar el código antes de cambiar la contraseña."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetCodeRepository.delete(resetCode); // Invalida el código
    }
}
