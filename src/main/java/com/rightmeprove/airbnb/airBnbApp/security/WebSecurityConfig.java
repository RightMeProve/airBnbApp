package com.rightmeprove.airbnb.airBnbApp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Inject final fields via constructor (JWTAuthFilter)
public class WebSecurityConfig {

    // JWT filter that authenticates each request
    private final JWTAuthFilter jwtAuthFilter;

    // Handler to delegate exceptions (e.g., access denied) to Spring MVC's exception resolver
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Configures Spring Security for HTTP requests.
     * - Disables CSRF (not needed for stateless APIs using JWT)
     * - Sets session policy to stateless (JWT handles authentication)
     * - Adds custom JWT filter before username/password filter
     * - Defines authorization rules for endpoints
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 🔒 Disable CSRF protection for REST API
                .csrf(csrfConfig -> csrfConfig.disable())

                // 📦 Make session stateless — no HTTP session is used
                .sessionManagement(sessionConfig ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔑 Add JWT authentication filter before the standard Spring Security filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 🔐 Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Only Hotel Managers can access /admin/** endpoints
                        .requestMatchers("/admin/**").hasRole("HOTEL_MANAGER")

                        // Any authenticated user can access /bookings/** and /users/**
                        .requestMatchers("/bookings/**").authenticated()
                        .requestMatchers("/users/**").authenticated()

                        // Public endpoints (login, signup, browsing hotels)
                        .anyRequest().permitAll()
                )
                // Exception handling: delegate AccessDeniedException to handlerExceptionResolver
                .exceptionHandling(exceptionHandlingConfig  ->
                        exceptionHandlingConfig.accessDeniedHandler(accessDeniedHandler())
                );

        return httpSecurity.build();
    }

    /**
     * PasswordEncoder bean using BCrypt.
     * - Used for hashing passwords (AuthService during signup/login)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager bean.
     * - Needed for AuthService to perform authentication with email/password.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Custom AccessDeniedHandler.
     * - Delegates access denied exceptions to Spring MVC's exception resolver
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return (request, response, accessDeniedException) -> {
            handlerExceptionResolver.resolveException(request,response,null,accessDeniedException);
        };
    }
}
