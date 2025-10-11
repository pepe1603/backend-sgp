package com.sgp.security.config.jwt;
import com.sgp.security.config.AuthExceptionHandler;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SignatureException;

@Component // Para que Spring lo gestione como un Bean
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Lo definimos en SecurityConfig
    // 💡 NECESITAMOS LA INSTANCIA DEL MANEJADOR DE ERRORES PARA DELEGAR
    private final AuthExceptionHandler authExceptionHandler; // ⬅️ INYECTAR

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener el encabezado 'Authorization'
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Comprobar si el token existe y tiene el formato 'Bearer <token>'
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (eliminar "Bearer ")
        jwt = authHeader.substring(7);

        // ⭐ Bloque TRY-CATCH para manejar las excepciones de JWT ⭐
        try {
            userEmail = jwtService.extractUsername(jwt); // Obtener el email del token, porque al estar en un Bloque TryCatch Puede lanzar ExpiredJwtException, SignatureException, etc.

            // 4. Si el email existe y el usuario no está ya autenticado
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Cargar los detalles del usuario desde la base de datos (PostgreSQL)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 5. Validar el token y el usuario
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Si es válido, crear un objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // La contraseña se pasa como null en autenticación de token
                            userDetails.getAuthorities()
                    );

                    // Establecer detalles de la petición
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. Almacenar la autenticación en el Contexto de Seguridad
                    // El usuario está autenticado y Spring Security puede autorizar las peticiones
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // Continuar con el siguiente filtro en la cadena si todo va bien
            filterChain.doFilter(request, response);

        }catch (ExpiredJwtException ex){
            // 💡 Token expirado: DELEGAR el manejo al AuthExceptionHandler (401)
            // Lanza una excepción de Spring Security que el EntryPoint puede manejar,
            // O DELEGA la escritura de la respuesta directamente.

            // Usaremos el AuthExceptionHandler para escribir la respuesta 401:
            authExceptionHandler.commence(request, response, new AuthenticationServiceException("JWT token has expired.", ex));
            // IMPORTANTE: NO llamar a filterChain.doFilter(request, response) después de manejar el error.

        } catch (JwtException e) {
            // 💡 Otra excepción JWT (Ej. SignatureException, MalformedJwtException):
            // DELEGAR el manejo al AuthExceptionHandler (401)
            authExceptionHandler.commence(request, response, new AuthenticationServiceException("Invalid JWT token structure or signature.", e));
            // IMPORTANTE: NO llamar a filterChain.doFilter(request, response) después de manejar el error.

        } catch (UsernameNotFoundException e) {
            // 💡 Usuario no encontrado (aunque el token sea válido):
            // DELEGAR el manejo al AuthExceptionHandler (401)
            authExceptionHandler.commence(request, response, new UsernameNotFoundException("User not found from valid JWT token.", e));
            // IMPORTANTE: NO llamar a filterChain.doFilter(request, response) después de manejar el error.
        }
        catch (Exception e) {
            // Captura cualquier otra excepción no esperada de la cadena JWT (Good Practice)
            AuthenticationException authException = new AuthenticationServiceException("Error inesperado en el procesamiento del token JWT.", e);
            authExceptionHandler.commence(request, response, authException);
        }
    }
}