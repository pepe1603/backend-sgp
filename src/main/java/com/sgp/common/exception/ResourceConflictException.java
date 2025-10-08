package com.sgp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Mapea a HTTP 409
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s ya existe con %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
