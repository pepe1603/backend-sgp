package com.sgp.common.exception;
import com.sgp.common.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.naming.AuthenticationException;
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
                .message("Credenciales de acceso incorrectas. Verifique su usuario o contraseña.")
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

    // 💥 7. MANEJADOR PARA CONFLICTO DE RECURSOS (ResourceConflictException) -> HTTP 409 💥
    // Este manejador cubre duplicidad de emails (reemplaza a EmailAlreadyExistsException) y otros campos únicos (ej: nombre de parroquia, número de documento).
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflictException(
            ResourceConflictException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Resource Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


    // 💥 8. MANEJADOR PARA RECURSO NO ENCONTRADO (ResourceNotFoundException) -> HTTP 404 💥
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    // 💥 9. MANEJADOR PARA ARGUMENTOS INVÁLIDOS (IllegalArgumentException) -> HTTP 400 💥
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Argument")
                .message(ex.getMessage()) // Muestra el mensaje específico de la validación fallida
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 10. Manejo de acceso denegado (AccessDeniedException) -> HTTP 403
    // Manejador para la excepción de Spring Security por falta de permisos (@PreAuthorize)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("No tiene los permisos necesarios para realizar esta acción.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    // 11.- Manejo de autorización denegada (AuthorizationDeniedException) -> HTTP 403
    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            org.springframework.security.authorization.AuthorizationDeniedException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Authorization Denied")
                .message("No tiene los privilegios necesarios (rol insuficiente) para acceder a este recurso.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    //12.- UsernameNotFoundException: Cuando un usuario no se encuentra durante el proceso de autenticación.
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Error")
                .message("Usuario no encontrado: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    //13.- MethodArgumentTypeMismatchException: Captura cuando un argumento no coincide con el tipo esperado.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Argument Type")
                .message("El tipo de argumento proporcionado no es válido.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //14.-ConstraintViolationException:
    //Esta excepción se lanza cuando hay violaciones de restricciones en la base de datos, como claves primarias duplicadas o violaciones de las restricciones de unicidad.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    //15.-DataIntegrityViolationException:
    //
    //Esta excepción puede ocurrir si intentas guardar datos que violan la integridad referencial de la base de datos (por ejemplo, claves foráneas).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Data Integrity Error")
                .message("Error de integridad de datos. Verifique las restricciones de la base de datos.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //16.-Si estás usando JWT para autenticación, esta excepción se lanza cuando el token JWT ha expirado. Sería útil para manejar el caso donde el usuario debe volver a autenticarse.
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(
            ExpiredJwtException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("JWT Expired")
                .message("El token JWT ha expirado. Por favor, inicie sesión nuevamente.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    //17.-HttpRequestMethodNotSupportedException:
    //
    //Si el cliente hace una solicitud con un método HTTP no soportado (por ejemplo, un POST en un endpoint que solo acepta GET).
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message("El método HTTP utilizado no es soportado por este endpoint.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    //18.- HttpMediaTypeNotSupportedException:
    //Captura cuando el cliente envía un tipo de medio (como Content-Type) no soportado.
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error("Unsupported Media Type")
                .message("El tipo de contenido enviado no es soportado.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    //19.- NoHandlerFoundException:
    //
    //Se lanza cuando no hay un manejador disponible para la solicitud.
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Handler Not Found")
                .message("No se encontró un manejador para la solicitud.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 20. Manejo de errores de conversión de mensajes (JSON a Objeto) -> HTTP 400
    @ExceptionHandler(org.springframework.http.converter.HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageConversionException(
            org.springframework.http.converter.HttpMessageConversionException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Message Conversion Error")
                // Un mensaje más útil si el error de tipo es claro
                .message("El cuerpo de la solicitud no es válido o hay un error de tipo en los datos. Detalle: " + ex.getMostSpecificCause().getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //21.-Manejador para código de verificación inválido o expirado -> HTTP 400
    @ExceptionHandler(VerificationCodeInvalidException.class)
    public ResponseEntity<ErrorResponse> handleVerificationCodeInvalidException(
            VerificationCodeInvalidException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Verification Failed")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    //22.-Manejador para código de verificación expirado -> HTTP 400
    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleVerificationCodeExpiredException(
            VerificationCodeExpiredException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Verification Failed")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //23.- Manejador para cuenta ya verificada -> HTTP 409 Conflict
    @ExceptionHandler(AccountAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleAccountAlreadyVerifiedException(
            AccountAlreadyVerifiedException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Resource Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    //24.- Manejador para acceso a un recurso parea RolName no esta autorizado
    @ExceptionHandler(ResourceNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotAuthorizedException(
            ResourceNotAuthorizedException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Authorization Denied")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 25: Transición de estado no válida (InvalidStateTransitionException) -> HTTP 409 ⭐
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(
            InvalidStateTransitionException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Invalid State Transition")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 26: Conflicto de Validación de Recurso (ResourceValidException) -> HTTP 409 ⭐
    @ExceptionHandler(ResourceValidException.class)
    public ResponseEntity<ErrorResponse> handleResourceValidException(
            ResourceValidException ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Resource Validation Conflict")
                .message(ex.getMessage()) // El mensaje claro y descriptivo que definiste (ej: La lista de roles no puede estar vacía si se envía.)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }



    // 2x. Manejo genérico (Fallback) -> HTTP 500
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
