package com.sgp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Mapea a HTTP 409
public class InvalidStateTransitionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}