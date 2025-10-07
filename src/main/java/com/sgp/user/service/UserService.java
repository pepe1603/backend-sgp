package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.EmailAlreadyExistsException;
import com.sgp.user.dto.ProfileResponse;
import com.sgp.user.dto.RegisterRequest;
import com.sgp.user.model.Profile;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.user.repository.RoleRepository;
import com.sgp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

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

    /**
     * Obtiene los datos del perfil del usuario por su email.
     * @param email El email del usuario autenticado (Username).
     */
    public ProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Profile profile = user.getProfile();
        if (profile == null) {
            // Esto no debería suceder si el registro es correcto, pero es buena práctica
            throw new RuntimeException("El perfil de usuario no existe.");
        }

      // Obtener TODOS los roles del usuario
        Set<String> roles = user.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.toSet());

        return ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .roles(roles)
                .address(profile.getAddress())
                .phone(profile.getPhone())
                .build();
    }
    // Más adelante agregaremos métodos para actualizar perfil, cambiar roles, etc.
}