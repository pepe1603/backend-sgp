package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.user.dto.RegisterRequest;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.user.repository.RoleRepository;
import com.sgp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service // Marca la clase como un componente de servicio de Spring
@RequiredArgsConstructor // Lombok: Genera un constructor con todos los campos 'final' (Inyección de dependencias)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // Necesitamos configurarlo más adelante

    /**
     * Lógica de registro para un nuevo feligrés/usuario estándar.
     */
    public User registerNewUser(RegisterRequest request) {

        // 1. Validación de Unicidad
        if (userRepository.existsByEmail(request.getEmail())) {
            // En un caso real, lanzaríamos una excepción específica (ej. EmailAlreadyExistsException)
            throw new RuntimeException("El email ya está en uso.");
        }

        // 2. Obtener el Rol por Defecto
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role no encontrado. La DB debe ser pre-poblada."));

        // 3. Crear el nuevo Usuario
        User user = new User();
        user.setEmail(request.getEmail());

        // IMPORTANTE: Codificar la contraseña antes de guardarla
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true); // Asumimos activación inmediata por ahora
        user.setRoles(Collections.singleton(userRole)); // Asigna el rol 'USER'

        // 4. Guardar y retornar
        return userRepository.save(user);
    }

    // Más adelante agregaremos métodos para actualizar perfil, cambiar roles, etc.
}