package com.sgp.auth.service;

import com.sgp.auth.dto.*;

// new file: com.sgp.auth.service.AuthService.java
public interface AuthService {
    RegisterResponse registerAndSendVerification(RegisterRequest request);
    void verifyAccount(String code);
    LoginResponse login(LoginRequest request);
    void resendVerificationCode(String email);
    // ⭐ NUEVO MÉTODOSS DE MAGIC LINK ⭐
    void requestMagicLink(MagicLinkRequest request);
    // LoginResponse verifyMagicLink(String token);
    LoginResponse verifyMagicLink(String token);
}