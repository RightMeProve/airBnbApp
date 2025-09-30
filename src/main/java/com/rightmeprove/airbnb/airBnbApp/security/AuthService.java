package com.rightmeprove.airbnb.airBnbApp.security;

import com.rightmeprove.airbnb.airBnbApp.dto.LoginDto;
import com.rightmeprove.airbnb.airBnbApp.dto.SignUpRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.UserDto;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.entity.enums.Role;
import com.rightmeprove.airbnb.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Authentication & Authorization service.
 *
 * Handles:
 *  - User signup (registration)
 *  - User login (authentication)
 *  - JWT token generation
 */
@Service
@RequiredArgsConstructor // generates constructor for all final fields
public class AuthService {

    // Dependencies injected by Spring
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    /**
     * Register a new user.
     * - Checks if email already exists.
     * - Encodes password.
     * - Assigns default role GUEST.
     * - Persists user in database.
     *
     * @param signUpRequestDto signup request (name, email, password)
     * @return saved user mapped to DTO
     */
    public UserDto signUp(SignUpRequestDto signUpRequestDto) {
        // Check if user already exists
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if (user != null) {
            throw new RuntimeException("User is already present with this email ID.");
        }

        // Map DTO → Entity
        User newUser = modelMapper.map(signUpRequestDto, User.class);

        // Assign default role
        newUser.setRoles(Set.of(Role.GUEST));

        // Encrypt password
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));

        // Save user in DB
        newUser = userRepository.save(newUser);

        // Return as DTO
        return modelMapper.map(newUser, UserDto.class);
    }

    /**
     * Authenticate a user and issue JWT tokens.
     * - Uses AuthenticationManager to validate credentials.
     * - Generates access + refresh tokens.
     *
     * @param loginDto login request (email, password)
     * @return array of [accessToken, refreshToken]
     */
    public String[] login(LoginDto loginDto) {
        // Authenticate using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        // ⚠️ Potential issue:
        // `authentication.getPrincipal()` by default returns a UserDetails object,
        // not your custom `User` entity.
        // This will cause a ClassCastException unless you've integrated your User entity
        // with Spring Security's UserDetailsService.
        User user = (User) authentication.getPrincipal();

        // Generate JWT tokens
        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);   // short-lived access token
        arr[1] = jwtService.generateRefreshToken(user);  // long-lived refresh token

        return arr;
    }
}
