package com.sgp.common.util;

import com.sgp.common.exception.ResourceNotAuthorizedException;
import com.sgp.user.model.User; // Asume que tu User es el Principal
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

/**
 * Utilidad para acceder al contexto de seguridad de Spring.
 */
public class SecurityUtil {

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * @return Long con el ID del usuario (entidad User).
     * @throws ResourceNotAuthorizedException si el usuario no está logueado.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotAuthorizedException("Usuario no autenticado o no hay sesión activa.");
        }

        // Asume que el principal es la entidad User que tiene el método getId()
        if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }

        // Fallback si el principal es solo el nombre de usuario (debería ser el objeto User en tu setup)
        throw new ResourceNotAuthorizedException("Principal de seguridad no reconocido o inválido.");
    }

    /**
     * Obtiene la entidad User actualmente autenticada (el principal de seguridad).
     * @return User la entidad del usuario.
     * @throws ResourceNotAuthorizedException si el usuario no está logueado o el principal no es un User.
     */
    public static User getCurrentUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotAuthorizedException("Usuario no autenticado o no hay sesión activa.");
        }

        // Asume que el principal es la entidad User que implementa UserDetails.
        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }

        throw new ResourceNotAuthorizedException("Principal de seguridad no reconocido o inválido. Esperaba la entidad User.");
    }

}