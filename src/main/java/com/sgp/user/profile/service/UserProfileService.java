package com.sgp.user.profile.service;

import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.common.util.SecurityUtil;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.user.dto.ProfileResponse;
import com.sgp.user.model.User;
import com.sgp.user.repository.UserRepository;
import com.sgp.user.service.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service // Marca la clase como un componente de servicio de Spring
@RequiredArgsConstructor // Lombok: Genera un constructor con todos los campos 'final' (Inyección de dependencias)
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper; // Suponiendo que inyectaste el UserMapper para usarlo
    private final PersonRepository personRepository;
    // ... (Otros campos, puedes eliminarlos si mueves el registro a AuthService) ...

    // ELIMINAR O MOVER a AuthService: public User registerNewUser(RegisterRequest request) { ... }

    /**
     * Obtiene los datos del perfil del usuario AUTENTICADO.
     * Método ideal para el endpoint /api/v1/users/me.
     */
    public ProfileResponse getCurrentUserProfile() { // Cambiado el nombre para ser específico de /me
        // 1. Obtener el objeto User directamente desde el contexto
        User user = SecurityUtil.getCurrentUserAuthenticated();

        // 2. Dado que el User del contexto es un objeto persistente, se puede acceder a la info perfil en la misma transacción (si fuera necesaria)
        // 2. Buscar la entidad Person asociada al User
        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "usuario", user.getEmail()));

        // Usamos el MapStruct Mapper si tienes un método de mapeo de User a ProfileResponse
        // Si no, mantenemos la lógica manual:
        Set<String> roles = user.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.toSet());

        return ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .roles(roles)
                .address(person.getAddress())
                .phoneNumber(person.getPhoneNumber())
                .build();
    }

    // Aquí iría el método para actualizar el perfil:
    /* @Transactional
    public ProfileResponse updateMyProfile(ProfileUpdateRequest request) { ... }
    */
    // Más adelante agregaremos métodos para actualizar perfil, cambiar roles, etc.
}