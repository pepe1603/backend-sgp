package com.sgp.auth.service;

public interface PasswordResetService {
    void requestResetCode(String email);
    void verifyResetCode(String email, String code);
    void resetPassword(String email, String newPassword);
}
