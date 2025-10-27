package com.sgp.common.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder o realizar una acción
 * y su cuenta está deshabilitada (no verificada).
 */
public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException(String message) {
        super(message);
    }
}
