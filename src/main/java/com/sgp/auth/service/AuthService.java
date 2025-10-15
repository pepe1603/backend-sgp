package com.sgp.auth.service;

import com.sgp.auth.dto.LoginRequest;
import com.sgp.auth.dto.LoginResponse;
import com.sgp.auth.dto.RegisterRequest;
import com.sgp.auth.dto.RegisterResponse;

// new file: com.sgp.auth.service.AuthService.java
public interface AuthService {
    RegisterResponse registerAndSendVerification(RegisterRequest request);
    void verifyAccount(String code);
    LoginResponse login(LoginRequest request);
    void resendVerificationCode(String email);
}