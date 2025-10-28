package com.sgp.auth.service;

import com.sgp.auth.dto.*;
import org.springframework.transaction.annotation.Transactional;

// new file: com.sgp.auth.service.AuthService.java
public interface AuthService {
    RegisterResponse registerAndSendVerification(RegisterRequest request);
    void verifyAccount(String code);
    LoginResponse login(LoginRequest request);
    void resendVerificationCode(String email);

    @Transactional
    void requestReactivationLink(String email);

    @Transactional
    void confirmReactivation(String token);

    // ⭐ NUEVO MÉTODOSS DE MAGIC LINK ⭐
    void requestMagicLink(MagicLinkRequest request);
    // LoginResponse verifyMagicLink(String token);
    LoginResponse verifyMagicLink(String token);
}