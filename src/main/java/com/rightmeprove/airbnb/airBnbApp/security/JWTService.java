package com.rightmeprove.airbnb.airBnbApp.security;

import com.rightmeprove.airbnb.airBnbApp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JWTService {

    // 🔑 Load secret key from application.properties (jwt.secretKey)
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    // Utility method: returns a SecretKey for signing/validating JWT
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a short-lived Access Token (10 minutes).
     */
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString()) // userId as subject
                .claim("email", user.getEmail())  // add email claim
                .claim("roles", user.getRoles().toString()) // add roles claim
                .issuedAt(new Date()) // issue time
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // 10 minutes
                .signWith(getSecretKey()) // sign with secret key
                .compact();
    }

    /**
     * Generate a long-lived Refresh Token (30 days).
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString()) // only store userId
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30)) // 30 days
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * Parse JWT and extract userId (subject).
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()               // ✅ use parser for new jjwt
                .verifyWith(getSecretKey())         // validate signature
                .build()
                .parseSignedClaims(token)           // parse claims
                .getPayload();

        return Long.valueOf(claims.getSubject());   // subject = userId
    }
}
