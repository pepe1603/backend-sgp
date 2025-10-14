package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.EmailAlreadyExistsException;
import com.sgp.common.exception.ResourceConflictException;
import com.sgp.common.exception.ResourceNotAuthorizedException;
import com.sgp.common.exception.ResourceNotFoundException;
import com.sgp.common.service.SecurityContextService;
import com.sgp.common.util.SecurityUtil;
import com.sgp.user.dto.UserCreationRequest;
import com.sgp.user.dto.UserManagementResponse;
import com.sgp.user.dto.UserUpdateRequest;
import com.sgp.user.model.Profile;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.user.repository.RoleRepository; // NUEVA INYECCIÓN
import com.sgp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // 👈 NECESITAMOS ESTO PARA MANEJAR ROLES
    private final UserMapper userMapper; // 👈 INYECTAMOS EL MAPPER
    private final SecurityContextService securityContextService;
    private final PasswordEncoder passwordEncoder;

    private static final String RESOURCE_NAME = "Usuario";
    private static final String UNAUTHORIZED_MSG = "Solo un usuario con el rol ADMIN puede realizar esta operación de gestión de usuarios.";


    // --- NUEVO: Creación por Admin ---
    @Override
    @Transactional
    public UserManagementResponse createUserByAdmin(UserCreationRequest request) {

        // 1. Validación de unicidad
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Obtener Roles (manejar excepción si no existen)
        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", roleName.name())))
                .collect(Collectors.toSet());

        // 3. Crear Entidad User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);
        user.setEnabled(true); // Cuentas de personal se habilitan inmediatamente
        user.setActive(true);  // Y están activas

        // 4. Crear Entidad Profile
        Profile profile = new Profile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUser(user); // Asociar el perfil al usuario
        user.setProfile(profile); // Asegurar la relación bidireccional

        // Nota: Spring Data JPA (si tienes CascadeType.ALL en User.profile) guardará el perfil automáticamente.
        User savedUser = userRepository.save(user);

        return userMapper.toManagementResponse(savedUser);
    }

    // --- Paginación (Método existente, pero ahora usa el Mapper) ---
    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> findAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);

        // Usamos el MapStruct Mapper
        return userPage.map(userMapper::toManagementResponse);
    }

    // --- Nuevo: Obtener por ID ---
    @Override
    @Transactional(readOnly = true)
    public UserManagementResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", userId));

        return userMapper.toManagementResponse(user);
    }

    // --- Nuevo: Actualizar (Roles y Estado) ---
    @Override
    @Transactional
    public UserManagementResponse updateUser(Long userId, UserUpdateRequest request) {

        // 🛑 DEFENSA EN PROFUNDIDAD: Solo ADMIN puede modificar roles/estado.
        if (!securityContextService.hasRole(RoleName.ADMIN)) {
            throw new ResourceNotAuthorizedException(UNAUTHORIZED_MSG);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", userId));

        // 1. Mapear campos simples (isEnabled, isActive)
        userMapper.updateEntityFromRequest(request, user);

        // 2. Manejar la actualización de Roles (Requiere buscar los roles por nombre)
        Set<Role> newRoles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", roleName.name())))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);

        User updatedUser = userRepository.save(user);
        return userMapper.toManagementResponse(updatedUser);
    }

    // --- Nuevo: Borrado Lógico (Desactivar) ---
    @Override
    @Transactional
    public void deactivateUser(Long userId) {

// 1. 🛑 REGLA DE NEGOCIO: Solo el ADMIN puede desactivar usuarios.
        // El @PreAuthorize en el Controller debería manejarlo, pero la lógica de negocio lo defiende.
        if (!securityContextService.hasRole(RoleName.ADMIN)) {
            throw new ResourceNotAuthorizedException("Solo un usuario con el rol ADMIN puede desactivar cuentas.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", userId));

        // 2. Opcional: Impedir la autodesactivación
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (user.getId().equals(currentUserId)) {
            throw new ResourceConflictException(RESOURCE_NAME, "acción", "No puedes desactivar tu propia cuenta.");
        }

        // Borrado lógico: Establecer isActive a false
        user.setActive(false);
        userRepository.save(user); // Guardar el cambio de estado
    }
}