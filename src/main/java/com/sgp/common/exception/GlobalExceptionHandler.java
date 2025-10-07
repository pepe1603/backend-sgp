package com.sgp.common.exception;
import com.sgp.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo de errores de validación (@Valid) -> HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(errors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. Manejo de credenciales inválidas (Login) -> HTTP 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Error")
                .message("Credenciales de acceso incorrectas. (usuario o contraseña incorrectos).")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 3. Manejo de Email ya registrado -> HTTP 409
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Resource Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // ⭐ NUEVO MANEJO ⭐
// 4. Manejo de Cuentas Deshabilitadas (No Verificadas) -> HTTP 401
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(
            DisabledException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Account Disabled")
                .message("Su cuenta no ha sido verificada. Por favor, revise su email o solicite un nuevo código.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    // ⭐ NUEVO MANEJO ⭐
    // 5. Manejo de Cuentas Bloqueadas (Por intentos fallidos) -> HTTP 401
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(
            LockedException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Account Locked")
                // El mensaje de la excepción ya informa del bloqueo de 30 minutos
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    //6.-  ⭐ NUEVO MANEJADOR PARA EXCEPCIONES ENCAPSULADAS ⭐
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException ex, HttpServletRequest request) {

        // Verificamos si la causa raíz es una LockedException
        if (ex.getCause() instanceof LockedException) {
            // Creamos la misma respuesta que handleLockedException
            LockedException lockedEx = (LockedException) ex.getCause();

            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Account Locked")
                    .message(lockedEx.getMessage()) // Usa el mensaje que configuraste en SecurityConfig
                    .path(request.getRequestURI())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Si no es LockedException, dejamos que caiga al manejo genérico o lo manejamos como un 500
        // En este caso, lo trataremos como un error interno de servicio.
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Service Error")
                .message("Error interno del servicio de autenticación.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // 7. Manejo genérico (Fallback) -> HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocurrió un error inesperado. Contacte al administrador.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
