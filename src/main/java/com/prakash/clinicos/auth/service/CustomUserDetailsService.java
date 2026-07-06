package com.prakash.clinicos.auth.service;

import com.prakash.clinicos.auth.entity.User;
import com.prakash.clinicos.auth.repository.UserRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements Spring Security's UserDetailsService.
 *
 * Spring Security calls loadUserByUsername() during the login flow
 * (AuthenticationManager.authenticate()). It passes the email, and we
 * return a UserPrincipal — Spring Security then compares the stored
 * password hash with the submitted password using BCryptPasswordEncoder.
 *
 * loadUserById() is called by JwtAuthenticationFilter on every
 * authenticated request. Loading by primary key (id) is faster than
 * loading by email and is what the JWT subject contains.
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return UserPrincipal.from(user);
    }

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "Session is invalid. Please log in again."));
        return UserPrincipal.from(user);
    }
}
