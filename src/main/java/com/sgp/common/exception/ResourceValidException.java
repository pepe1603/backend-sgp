package com.sgp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para representar un conflicto (HTTP 409) debido a una
 * **validación fallida** o una **restricción de integridad de datos**.
 */
@ResponseStatus(HttpStatus.CONFLICT) // 🛑 Mapea a HTTP Status 409
public class ResourceValidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor para conflictos de validación con un mensaje claro.
     * @param validationMessage Mensaje descriptivo sobre la restricción violada (ej: "La lista de roles no puede estar vacía.").
     */
    public ResourceValidException(String validationMessage) {
        super(validationMessage);
    }

    // Puedes mantener el constructor anterior si lo necesitas para un patrón específico,
    // pero el que acepta un String simple es más flexible y claro para este caso.
    /*
    public ResourceValidException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s DEBE tener al menos un(a) %s: '%s'", resourceName, fieldName, fieldValue));
    }
    */
}