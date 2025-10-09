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

    // 🔑 Secret key used for signing and verifying JWT tokens
    // Loaded from application.properties: jwt.secretKey
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    /**
     * Convert the secret string into a SecretKey object.
     * - Required by JJWT library for HMAC signing.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a short-lived Access Token.
     * - Validity: 10 minutes.
     * - Contains userId (as subject), email, and roles as claims.
     * - Signed with HMAC SHA key.
     *
     * @param user The authenticated user
     * @return JWT access token as String
     */
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())       // userId stored as subject
                .claim("email", user.getEmail())        // additional claim
                .claim("roles", user.getRoles().toString()) // additional claim
                .issuedAt(new Date())                    // token creation time
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // 10 min expiry
                .signWith(getSecretKey())                // sign token with secret key
                .compact();                              // build JWT string
    }

    /**
     * Generate a long-lived Refresh Token.
     * - Validity: 30 days.
     * - Only stores userId as subject; no extra claims.
     *
     * @param user The authenticated user
     * @return JWT refresh token as String
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())       // only userId
                .issuedAt(new Date())                    // issue time
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30)) // 30 days
                .signWith(getSecretKey())                // sign token
                .compact();
    }

    /**
     * Extract userId from a JWT token.
     * - Validates signature using secret key.
     * - Parses claims and retrieves subject (userId).
     *
     * @param token JWT string
     * @return userId as Long
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()               // create JWT parser
                .verifyWith(getSecretKey())         // verify signature
                .build()
                .parseSignedClaims(token)           // parse the signed JWT
                .getPayload();                      // get claims (subject, expiration, etc.)

        return Long.valueOf(claims.getSubject());   // convert subject to Long (userId)
    }
}
