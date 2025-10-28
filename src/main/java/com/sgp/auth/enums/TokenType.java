package com.sgp.auth.enums;

/**
 * Define los posibles usos de un VerificationToken.
 */
public enum TokenType {
    /** Token para confirmar el registro de una cuenta. (Usualmente un UUID largo) */
    ACCOUNT_VERIFICATION,

    /** Token de un solo uso para iniciar sesión sin contraseña. (Usualmente un UUID largo) */
    MAGIC_LINK,

    /** Token de un solo uso para reactivar una cuenta suspendida por inactividad. (Usualmente un UUID largo) */
    ACCOUNT_REACTIVATION
}
