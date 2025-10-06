package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.EmailAlreadyExistsException;
import com.sgp.user.dto.RegisterRequest;
import com.sgp.user.model.Profile;
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

        // 1. Validación de Unicidad (Lanza nuestra nueva excepción)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso."); // ⬅️ Usar la nueva excepción
        }
        // 2. Obtener el Rol por Defecto
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role no encontrado. La DB debe ser pre-poblada."));

        // 3. Crear el nuevo Usuario
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false); // ⬅️ Inicia DESHABILITADO, a la espera de verificación
        user.setRoles(Collections.singleton(userRole));

        // ⭐ 4. CREAR E INICIALIZAR EL PERFIL POR DEFECTO ⭐
        Profile profile = new Profile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setAddress(""); // Campos por defecto
        profile.setPhone("");   // Campos por defecto
        profile.setUser(user);

        // Asignar el perfil al usuario (relación bidireccional)
        user.setProfile(profile);

        // 5. Guardar el User (el Profile se guardará en cascada)
        return userRepository.save(user);
    }

    // Más adelante agregaremos métodos para actualizar perfil, cambiar roles, etc.
}