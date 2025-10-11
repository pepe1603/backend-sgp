package com.sgp.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgp.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler { /// DEberamso llamaral AuhtEntrypioint?

// 🔴 CAMBIO CLAVE: Ya no se instancia un nuevo ObjectMapper.
// Se inyecta la instancia manejada por Spring (que incluye el JavaTimeModule).
    private final ObjectMapper objectMapper;

    // Inyección por constructor (Autowired implícito por @Component)
    public AuthExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    private void buildResponse(HttpServletResponse response, HttpStatus status, String error, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path) // No podemos acceder al path de request fácilmente aquí, lo omitimos o lo inyectamos si es crucial
                .build();

        // Usamos el ObjectMapper inyectado que ya tiene configurado el soporte para LocalDateTime
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }



    // Maneja 401 Unauthorized (Sin token o token inválido)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // Se añade request.getRequestURI() para el path
        // ⭐ CAMBIO CLAVE: Usar el mensaje de la excepción recibida ⭐
        // Si el mensaje de la excepción es nulo o vacío, usar el mensaje genérico.
        String errorMessage = authException.getMessage();
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "Acceso denegado. Se requiere autenticacion (Token JWT).";
        }

        buildResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                errorMessage, // ⬅️ AHORA USAMOS EL MENSAJE DE LA EXCEPCIÓN
                request.getRequestURI()
        );
    }

    // Maneja 403 Forbidden (Token válido pero rol insuficiente)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // Se añade request.getRequestURI() para el path
        buildResponse(response, HttpStatus.FORBIDDEN, "Forbidden", "No tiene los permisos necesarios (rol insuficiente) para acceder a este recurso.", request.getRequestURI());
    }
}