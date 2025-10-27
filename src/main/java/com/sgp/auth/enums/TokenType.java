package com.sgp.auth.enums;

/**
 * Define los posibles usos de un VerificationToken.
 */
public enum TokenType {
    /** Token para confirmar el registro de una cuenta. (Usualmente un OTP corto) */
    ACCOUNT_VERIFICATION,

    /** Token de un solo uso para iniciar sesión sin contraseña. (Usualmente un UUID largo) */
    MAGIC_LINK
}
