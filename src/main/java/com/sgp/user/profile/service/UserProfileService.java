package com.sgp.user.profile.service;

import com.sgp.user.profile.dto.ProfileResponse;
import com.sgp.user.profile.dto.PasswordUpdateRequest;
import com.sgp.user.profile.dto.ProfileUpdateRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interfaz para la gestión del perfil del usuario autenticado.
 * Define las operaciones de negocio relacionadas con la visualización y
 * modificación del propio perfil (My Profile).
 */
public interface UserProfileService {

    /**
     * Obtiene los datos del perfil del usuario AUTENTICADO.
     * @return DTO con la información del perfil.
     */
    ProfileResponse getCurrentUserProfile();

    /**
     * Actualiza el perfil del usuario autenticado.
     * @param request DTO con los campos a actualizar.
     * @return DTO del perfil actualizado.
     */
    ProfileResponse updateMyProfile(ProfileUpdateRequest request);

    /**
     * Cambia la contraseña del usuario autenticado.
     * @param request DTO con la contraseña actual y la nueva contraseña.
     */
    void changeMyPassword(PasswordUpdateRequest request);

    // --- NUEVO: Dar de Baja (Borrado Lógico) por el Propio Usuario ---
    @Transactional
    void softDeleteMyAccount();
}