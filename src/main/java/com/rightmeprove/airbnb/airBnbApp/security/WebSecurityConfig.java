package com.rightmeprove.airbnb.airBnbApp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 🔒 Disable CSRF since we're stateless (JWT handles security)
                .csrf(csrfConfig -> csrfConfig.disable())

                // 📦 Make session stateless — every request must carry its JWT
                .sessionManagement(sessionConfig ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔑 Add our JWT filter before Spring Security's built-in username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 🔐 Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Only Hotel Managers can access admin APIs
                        .requestMatchers("/admin/**").hasRole("HOTEL_MANAGER")

                        // Bookings require authentication (any logged-in user)
                        .requestMatchers("/bookings/**").authenticated()

                        // Everything else (login, signup, browsing hotels) is public
                        .anyRequest().permitAll()
                );

        return httpSecurity.build();
    }

    // Password hashing using BCrypt (recommended)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Needed for authentication in AuthService (login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
