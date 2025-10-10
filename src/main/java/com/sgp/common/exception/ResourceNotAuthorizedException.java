package com.sgp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción personalizada para manejar casos donde el usuario está autenticado,
 * pero no tiene permiso (autorización) para acceder a un recurso específico o a los datos de otro usuario.
 * Retorna HTTP 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceNotAuthorizedException extends RuntimeException {

    public ResourceNotAuthorizedException(String message) {
        super(message);
    }
}
