package com.prakash.clinicos.security;

import com.prakash.clinicos.auth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Bridges your User entity and Spring Security's UserDetails interface.
 *
 * Why a separate class instead of making User implement UserDetails?
 * - Separation of concerns: your entity stays a pure DB model.
 * - If Spring Security changes its interface, only this class changes.
 * - Avoids accidentally serializing security fields (like password hash) in API responses.
 *
 * UserDetails is what Spring Security works with internally.
 * It never touches your User entity directly.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String password,
                         boolean enabled,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /**
     * Factory method: converts your User entity into a UserPrincipal.
     *
     * Role names are prefixed with "ROLE_" because Spring Security's
     * hasRole("CLINIC_ADMIN") internally checks for "ROLE_CLINIC_ADMIN".
     */
    public static UserPrincipal from(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                authorities
        );
    }

    /** Spring Security uses getUsername() for the identifier — we use email. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
