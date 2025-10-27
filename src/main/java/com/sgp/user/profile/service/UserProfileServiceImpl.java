package com.sgp.user.profile.service;

import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.InvalidStateTransitionException;
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

        User user = SecurityUtil.getCurrentUserAuthenticated();

        // 1. Buscar la entidad Person asociada al User
        // NOTA: Si el usuario ya se borró lógicamente (isActive=false), esta línea fallará
        // si el filtro @Where está activo. Como esta es una ruta de perfil,
        // asumimos que el usuario autenticado aún está "activo" para el sistema de seguridad.
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
    @Override
    @Transactional
    public void changeMyPassword(PasswordUpdateRequest request) {

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
    // --- NUEVO: Dar de Baja (Borrado Lógico) por el Propio Usuario ---
    @Transactional
    @Override
    public void softDeleteMyAccount() {
        User user = SecurityUtil.getCurrentUserAuthenticated();

        // 1. VALIDACIÓN DE ADMIN ÚNICO: Impedir que el último ADMIN activo se de de baja.
        boolean isCurrentUserAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);

        if (isCurrentUserAdmin) {
            // Reutilizamos el método de conteo del repositorio de administración
            long adminCount = userRepository.countByRolesName(RoleName.ADMIN);

            if (adminCount == 1) {
                throw new InvalidStateTransitionException(
                        "Conflicto de estado: No puedes dar de baja tu cuenta ya que eres el único administrador restante. Contacta a soporte para transferir el rol de administrador antes de proceder."
                );
            }
        }

        // 2. APLICAR BORRADO LÓGICO
        user.softDelete(); // Establece isActive = false y registra deletedAt
        user.setEnabled(false); // También deshabilitamos el inicio de sesión
        userRepository.save(user); // Persistimos los cambios
    }
}