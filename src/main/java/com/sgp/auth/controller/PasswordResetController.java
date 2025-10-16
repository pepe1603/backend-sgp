package com.sgp.auth.controller;

import com.sgp.auth.dto.RequestResetDTO;
import com.sgp.auth.dto.ResetPasswordDTO;
import com.sgp.auth.dto.VerifyCodeDTO;
import com.sgp.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request-reset")
    public ResponseEntity<String> requestReset(@RequestBody @Valid RequestResetDTO dto) {
        passwordResetService.requestResetCode(dto.getEmail());
        return ResponseEntity.ok("Se ha enviado un código de recuperación al correo.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody @Valid VerifyCodeDTO dto) {
        passwordResetService.verifyResetCode(dto.getEmail(), dto.getCode());
        return ResponseEntity.ok("Código verificado correctamente.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        passwordResetService.resetPassword(dto.getEmail(), dto.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
}
