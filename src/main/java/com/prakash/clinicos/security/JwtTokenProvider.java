package com.prakash.clinicos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Centralizes all JWT operations.
 *
 * Uses JJWT 0.12.x API — note the differences from older 0.9.x tutorials:
 *   Old: Jwts.builder().setSubject()  →  New: Jwts.builder().subject()
 *   Old: Jwts.parser().setSigningKey() →  New: Jwts.parser().verifyWith()
 *   Old: parseClaimsJws()             →  New: parseSignedClaims()
 *
 * JWT structure: header.payload.signature
 *   payload contains: sub (userId), email, iat (issued at), exp (expiry)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;

    /**
     * Constructor injection — @Value reads from application.yaml.
     *
     * Decoders.BASE64.decode() converts our base64 secret string into raw bytes.
     * Keys.hmacShaKeyFor() validates the key is >= 256 bits for HS256
     * and throws WeakKeyException at startup if it's too short.
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /** Generates a signed JWT access token for the given user. */
    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))   // who the token is about
                .claim("email", principal.getEmail())         // extra data in payload
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)                         // HS256 by default
                .compact();
    }

    /** Extracts the user ID from a validated token. */
    public Long getUserIdFromToken(String token) {
        Claims payload = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(payload.getSubject());
    }

    /**
     * Returns true if the token is well-formed, correctly signed, and not expired.
     * Catches all JJWT exceptions so callers get a simple boolean.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }
}
