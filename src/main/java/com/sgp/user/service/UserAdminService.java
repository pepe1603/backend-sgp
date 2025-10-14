
package com.sgp.user.service;

import com.sgp.user.dto.ProfileResponse;
import com.sgp.user.dto.UserCreationRequest;
import com.sgp.user.dto.UserManagementResponse;
import com.sgp.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserAdminService {

    // 1. CREACIÓN (NUEVO)
    UserManagementResponse createUserByAdmin(UserCreationRequest request); // 👈 NUEVO MÉTODO

    //2. Paginación
    /**
     * Obtiene una lista paginada y ordenada de todos los usuarios del sistema.
     * @param pageable Parámetros de paginación y ordenamiento.
     * @return Una Page de UserManagementResponse.
     */
    Page<UserManagementResponse> findAllUsers(Pageable pageable);

    // 3. Obtener un usuario por ID (Existente)
    UserManagementResponse getUserById(Long userId);

    // 4. Actualizar estado/roles (Existente)
    UserManagementResponse updateUser(Long userId, UserUpdateRequest request);

    // 5. Desactivar/Borrado Lógico (Existente)
    void deactivateUser(Long userId);

}