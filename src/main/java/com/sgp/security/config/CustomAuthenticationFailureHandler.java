package com.sgp.security.config;

import com.sgp.security.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        // Solo manejamos BadCredentialsException (fallo de usuario/contraseña)
        if (exception instanceof BadCredentialsException) {
            // Extraer el email del intento fallido (asumiendo que viene en la petición)
            String email = request.getParameter("email");
            if (email != null) {
                loginAttemptService.recordFailedAttempt(email);
            }
        }

        // Continuar con el manejo de errores estándar de Spring Security
        // En una API REST, simplemente dejamos que el filtro/exception handler lo maneje.
        // Aquí no redirigimos, solo devolvemos el error 401.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
    }
}