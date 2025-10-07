package com.sgp.user.model;


import com.sgp.common.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean isEnabled = false;

    // Relación ManyToMany con Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Relación OneToOne con Profile (Mapeada en la clase Profile)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    // ====================================================================
    // ⭐ IMPLEMENTACIÓN DE MÉTODOS DE USERDETAILS ⭐
    // ====================================================================

    // Retorna los roles del usuario como GrantedAuthorities
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapea el Set<Role> a una Collection<GrantedAuthority>
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .collect(Collectors.toList());
    }

    // El email se usa como nombre de usuario para el login
    @Override
    public String getUsername() {
        return email;
    }

    // Indica si la cuenta no ha expirado
    @Override
    public boolean isAccountNonExpired() {
        return true; // Asume que la cuenta nunca expira
    }

    // Indica si las credenciales (contraseña) no han expirado
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Asume que la contraseña nunca expira
    }

    // Indica si la cuenta no está bloqueada
    @Override
    public boolean isAccountNonLocked() {
        // ⚠️ ELIMINAR ESTA LÍNEA QUE SIEMPRE DEVUELVE TRUE: ⚠️
        // return true;

        // Dado que no tienes un campo 'isLocked' en la DB, y el bloqueo se maneja con Redis,
        // es correcto dejar la lógica de bloqueo en el UserDetailsService,
        // y mantener este método devolviendo true para evitar complejidades innecesarias con JPA.

        // Sin embargo, si quisieras usar un campo persistente:
        // return !isLocked;

        // Por ahora, tu enfoque de lanzar LockedException en el UserDetailsService es la mejor solución
        // para una arquitectura que usa Redis y no quiere modificar la tabla 'users'.

        return true; // Mantenemos temporalmente, confiando en que UserDetailsService lanzará la excepción.
    }

    // Indica si la cuenta está habilitada (ya tienes este campo)
    @Override
    public boolean isEnabled() {
        return isEnabled; // Utiliza tu campo existente
    }
}