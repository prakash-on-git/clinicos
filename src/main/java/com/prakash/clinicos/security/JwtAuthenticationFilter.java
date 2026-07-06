package com.prakash.clinicos.security;

import com.prakash.clinicos.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every HTTP request and checks for a JWT in the Authorization header.
 *
 * Why OncePerRequestFilter?
 * Servlet filters can be called multiple times per request in some scenarios
 * (error dispatches, forwards). OncePerRequestFilter guarantees exactly
 * one execution per HTTP request regardless of dispatch type.
 *
 * Flow per request:
 *   1. Extract token from "Authorization: Bearer <token>" header
 *   2. Validate token (signature + expiry)
 *   3. Load user from DB by ID stored in token subject
 *   4. Set authentication in SecurityContextHolder → request is now authenticated
 *   5. Continue to the next filter / controller
 *
 * If no token or invalid token: just continue without setting auth.
 * Spring Security will then reject the request as 401 for protected endpoints.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                    CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserById(userId);

            // UsernamePasswordAuthenticationToken with 3 args = authenticated (non-null credentials)
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            // Attach request details (IP, session) to the auth token
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Store in SecurityContextHolder — controllers can now call SecurityContextHolder.getContext()
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7); // strip "Bearer " prefix
        }
        return null;
    }
}
