package com.sgp.common.exception;

public class VerificationCodeInvalidException extends RuntimeException {
    public VerificationCodeInvalidException(String message) {
        super(message);
    }
}