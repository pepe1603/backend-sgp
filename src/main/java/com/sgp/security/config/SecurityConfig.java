package com.sgp.security.config;

import com.sgp.security.config.jwt.JwtAuthenticationFilter;
import com.sgp.security.service.LoginAttemptService;
import com.sgp.user.model.User;
import com.sgp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Habilita la configuración de seguridad de Spring
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    // 1. Define la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthFilter, // Inyecta el filtro
            // ⭐ SOLUCIÓN: INYECTAR EL BEAN AuthenticationProvider RESUELTO ⭐
            AuthenticationProvider authenticationProvider // Spring lo inyecta automáticamente
    ) throws Exception {

        // Deshabilita la protección CSRF (necesaria para APIs REST sin estado)
        http.csrf(csrf -> csrf.disable())

                // Define el manejo de sesiones como "STATELESS" (sin estado)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define las reglas de autorización para los endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                // ⭐ USO DEL BEAN INYECTADO: YA NO HAY LLAMADA A UN MÉTODO SIN ARGUMENTOS ⭐
                .authenticationProvider(authenticationProvider) // Usa el bean inyectado

                // Agrega el filtro JWT antes del filtro de usuario/contraseña estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Define el bean para el codificador de contraseñas (PasswordEncoder)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- Nuevos Beans para JWT ---

    // 1. Carga el usuario desde la DB (por email)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // ⭐ 1. COMPROBAR BLOQUEO EN REDIS PRIMERO ⭐
            if (loginAttemptService.isBlocked(username)) {
                // Lanza LockedException si Redis indica que está bloqueada
                throw new LockedException("La cuenta ha sido bloqueada debido a demasiados intentos fallidos. Intente mas tarde .");
            }

            // 2. Cargar User (que implementa UserDetails) desde la DB
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            // ⭐ 3. ASEGURAR QUE USERDETAILS REFLEJE EL ESTADO DE BLOQUEO DE REDIS ⭐
            // Aunque el UserDetails por defecto de Spring no tiene un método setBlocked,
            // al lanzar LockedException aquí, el flujo de autenticación se detiene
            // y el GlobalExceptionHandler lo captura. Por ahora, esto es suficiente.

            return user;
        };
    }

    // 2. Provee la lógica de autenticación (Recibe PasswordEncoder como argumento)
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // 3. El AuthenticationManager que usamos en el Controller para el login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}