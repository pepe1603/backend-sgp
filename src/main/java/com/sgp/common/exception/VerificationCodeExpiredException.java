package com.sgp.common.exception;

public class VerificationCodeExpiredException extends RuntimeException{
    public VerificationCodeExpiredException (String message) {
        super(message);
    }
}
