package com.sgp.user.profile.service;

import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.common.util.SecurityUtil;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.user.profile.dto.ProfileResponse;
import com.sgp.user.model.User;
import com.sgp.user.profile.dto.PasswordUpdateRequest;
import com.sgp.user.profile.dto.ProfileUpdateRequest;
import com.sgp.user.repository.UserRepository;
import com.sgp.user.service.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

// AHORA IMPLEMENTA LA INTERFAZ UserProfileService
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService { // ¡CAMBIO AQUÍ!

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene los datos del perfil del usuario AUTENTICADO.
     */
    @Override // Buena práctica: Añadir @Override
    public ProfileResponse getCurrentUserProfile() {
        // ... (resto del cuerpo del método permanece IGUAL)
        User user = SecurityUtil.getCurrentUserAuthenticated();

        // 1. Buscar la entidad Person asociada al User
        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "usuario", user.getEmail()));

        // 2. Mapear y devolver la respuesta
        Set<String> roles = user.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toSet());

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

    /**
     * Actualiza el perfil del usuario autenticado.
     */
    @Override // Buena práctica: Añadir @Override
    @Transactional
    public ProfileResponse updateMyProfile(ProfileUpdateRequest request) {
        // ... (resto del cuerpo del método permanece IGUAL)
        User user = SecurityUtil.getCurrentUserAuthenticated();

        // 1. Buscar la entidad Person asociada al User
        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "usuario", user.getEmail()));

        // 2. Aplicar actualizaciones solo si el Optional está presente (no vacío)
        request.getFirstName().ifPresent(person::setFirstName);
        request.getLastName().ifPresent(person::setLastName);
        request.getBirthDate().ifPresent(person::setBirthDate);
        request.getGender().ifPresent(person::setGender);
        request.getPhoneNumber().ifPresent(person::setPhoneNumber);
        request.getAddress().ifPresent(person::setAddress);

        // 3. Guardar la entidad Person actualizada
        personRepository.save(person);

        // 4. Devolver la respuesta del perfil actualizado
        return getCurrentUserProfile();
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     */
    @Override // Buena práctica: Añadir @Override
    @Transactional
    public void changeMyPassword(PasswordUpdateRequest request) {
        // ... (resto del cuerpo del método permanece IGUAL)
        User user = SecurityUtil.getCurrentUserAuthenticated();

        // 1. Validar que la nueva contraseña y su confirmación coincidan
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
        }

        // 2. Validar que la contraseña actual proporcionada es correcta
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual proporcionada es incorrecta.");
        }

        // 3. Codificar y establecer la nueva contraseña
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);

        // 4. Guardar la entidad User actualizada
        userRepository.save(user);
    }
}