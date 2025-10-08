package com.sgp.common.exception;

public class AccountAlreadyVerifiedException extends RuntimeException {
    public AccountAlreadyVerifiedException(String message) {
        super(message);
    }
}