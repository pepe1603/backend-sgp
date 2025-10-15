package com.sgp.user.service;

import com.sgp.common.enums.RoleName;
import com.sgp.common.exception.*;
import com.sgp.common.service.SecurityContextService;
import com.sgp.common.util.SecurityUtil;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.user.dto.UserCreationRequest;
import com.sgp.user.dto.UserManagementResponse;
import com.sgp.user.dto.UserUpdateRequest;
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
    private final PersonRepository personRepository;

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
        user.setForcePasswordChange( // ADMIN ELige si Qeuire cambio de contraseña Obligatorio
                request.getForcePasswordChange() != null ? request.getForcePasswordChange() : true
        );



        // 4. Crear Entidad Person (Remplazo de  Profile)
        Person person = new Person();
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setUser(user); // Asociar el perfil al usuario

        // 5. Guardar ambas (Person debe guardar primero o User debe tener Cascade.ALL)
        // Lo más limpio es guardar Person después de User (si Person tiene @JoinColumn(name="user_id", unique=true))
        User savedUser = userRepository.save(user); // Guardamos User primero para obtener el ID
        personRepository.save(person); // Guardamos Person (asumiendo que tiene los datos obligatorios)

        return userMapper.toManagementResponse(savedUser);
    }

    // --- Paginación (Método optimizado con JOIN FETCH) ---
    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> findAllUsers(Pageable pageable) {
        // USAR EL MÉTODO CON OPTIMIZACIÓN DE FETCH
        Page<User> userPage = userRepository.findAllUsersWithPerson(pageable);

        // Mapear y asignar datos de Person
        return userPage.map(user -> {
            UserManagementResponse response = userMapper.toManagementResponse(user);

            // Buscar la Person asociada (debería estar en la sesión/cache por el FETCH JOIN)
            personRepository.findByUser(user).ifPresent(person -> {
                response.setFirstName(person.getFirstName());
                response.setLastName(person.getLastName());
            });

            return response;
        });
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

        // =========================================================================
        // 1. APLICAR VALIDACIONES DE CONFLICTO (HTTP 409) - Flujo de Control
        // =========================================================================

        // A. VALIDACIÓN: Evitar dejar el sistema sin administrador activo.
        // Esta validación debe ejecutarse si se intenta cambiar el rol O el estado 'isEnabled'.
        // El borrado lógico ('isActive') no afecta el acceso, pero 'isEnabled' sí.

        boolean isCurrentUserAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);

        if (isCurrentUserAdmin) {

            // Determinar si el rol ADMIN será removido:
            boolean isRemovingAdminRole = request.getRoles()
                    .map(newRoles -> !newRoles.contains(RoleName.ADMIN))
                    .orElse(false); // Si 'roles' no se envía, no se remueve.

            // Determinar si el usuario será desactivado:
            boolean isDisablingAdmin = request.getIsEnabled()
                    .map(enabled -> !enabled)
                    .orElse(false); // Si 'isEnabled' no se envía, no se desactiva.

            if (isRemovingAdminRole || isDisablingAdmin) {
                long adminCount = userRepository.countByRolesName(RoleName.ADMIN);

                // Si SOLO queda este usuario como administrador (activo o no):
                if (adminCount == 1) {
                    throw new InvalidStateTransitionException(
                            String.format(
                                    "Conflicto de estado: No se puede modificar el usuario '%s' (ID: %d) ya que es el único administrador restante en el sistema. Debe asignar el rol a otro usuario primero.",
                                    user.getEmail(), userId
                            )
                    );
                }
            }
        }


        // B. VALIDACIÓN: Si se envía el Set de roles, DEBE tener al menos un rol.
        request.getRoles().ifPresent(newRoleNames -> {
            if (newRoleNames.isEmpty()) {
                // Usando la ResourceValidException para un mensaje descriptivo de la validación
                throw new ResourceValidException("La lista de roles para la actualización no puede estar vacía si se envía.");
            }
        });


        // =========================================================================
        // 2. MAPEAR Y APLICAR CAMBIOS - Solo si pasaron las validaciones
        // =========================================================================

        // 1. Manejar la actualización de isEnabled y isActive
        // *Se eliminan las llamadas a userMapper.updateEntityFromRequest si el DTO solo tiene Optionals.*
        // *Se mantienen los ifPresent para la actualización parcial (PATCH).*
        request.getIsEnabled().ifPresent(user::setEnabled);
        request.getIsActive().ifPresent(user::setActive);
        request.getForcePasswordChange().ifPresent(newValue -> {
            if (!securityContextService.hasRole(RoleName.ADMIN)) {
                throw new ResourceNotAuthorizedException("Solo un administrador puede cambiar la política de cambio de contraseña.");
            }
            user.setForcePasswordChange(newValue);
        });



        // Si tu `UserUpdateRequest` tuviera otros campos simples (ej: firstName, lastName)
        // que **NO** son Optionals, podrías usar el mapper para esos, pero para Optionals
        // el `ifPresent` es la mejor práctica.

        // 2. Manejar la actualización de Roles (Solo si está presente en el request)
        request.getRoles().ifPresent(newRoleNames -> {

            // Búsqueda y mapeo de Roles
            Set<Role> newRoles = newRoleNames.stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", roleName.name())))
                    .collect(Collectors.toSet());

            user.setRoles(newRoles);
        });

        // =========================================================================
        // 3. PERSISTIR LOS CAMBIOS
        // =========================================================================
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