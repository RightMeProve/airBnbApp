package com.rightmeprove.airbnb.airBnbApp.security;

import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * Responsibilities:
 * - Executes once per HTTP request (extends OncePerRequestFilter).
 * - Reads JWT from Authorization header.
 * - Validates and parses JWT → extracts userId.
 * - Loads User entity from database.
 * - Sets the authenticated user in Spring Security context.
 */
@Configuration
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    // Service to parse/validate JWT tokens
    private final JWTService jwtService;

    // Service to load User entity by ID
    private final UserService userService;

    // Handles exceptions inside filters (since we cannot throw directly)
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1️⃣ Extract Authorization header
            final String requestTokenHeader = request.getHeader("Authorization");

            // 2️⃣ If header is missing or doesn't start with "Bearer ", skip authentication
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return; // exit filter
            }

            // 3️⃣ Remove "Bearer " prefix to get the token
            String token = requestTokenHeader.substring(7);

            // 4️⃣ Extract userId from JWT token
            Long userId = jwtService.getUserIdFromToken(token);

            // 5️⃣ Authenticate only if:
            //    - userId is valid
            //    - SecurityContext does not already contain authentication
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user from DB
                User user = userService.getUserById(userId);

                // 6️⃣ Create authentication object with user roles
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                user,            // principal → the User entity
                                null,            // credentials → already authenticated via token
                                user.getAuthorities() // authorities → roles (requires User implements UserDetails)
                        );

                // 7️⃣ Attach HTTP request details (IP, session info)
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8️⃣ Set authentication in Spring Security context
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            // 9️⃣ Continue filter chain (must call this!)
            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            // Catch any JWT parsing/validation errors
            // Delegate exception handling to Spring's handlerExceptionResolver
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
