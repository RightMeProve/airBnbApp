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
 * - Runs once per request (extends OncePerRequestFilter).
 * - Extracts JWT token from Authorization header.
 * - Validates and parses token → extracts userId.
 * - Loads User from DB and sets authentication in SecurityContext.
 */
@Configuration
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;

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
            // 1. Extract "Authorization" header
            final String requestTokenHeader = request.getHeader("Authorization");

            // If missing or doesn't start with "Bearer", skip authentication
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extract token (after "Bearer ")
            String token = requestTokenHeader.substring(7);

            // 3. Extract userId from token
            Long userId = jwtService.getUserIdFromToken(token);

            // 4. Authenticate only if userId is valid and no authentication is already set
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserById(userId);

                // Create authentication object with user's authorities (roles)
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities() // ⚠ requires User to implement UserDetails
                        );

                // Attach request details
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 5. Store authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            // Continue filter chain
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
