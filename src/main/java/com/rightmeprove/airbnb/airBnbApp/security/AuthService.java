package com.rightmeprove.airbnb.airBnbApp.security;

import com.rightmeprove.airbnb.airBnbApp.dto.LoginDto;
import com.rightmeprove.airbnb.airBnbApp.dto.SignUpRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.UserDto;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.entity.enums.Role;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
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
 * Handles user signup, login, and JWT token generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // Repository for User entity
    private final UserRepository userRepository;

    // Maps between DTOs and entity objects
    private final ModelMapper modelMapper;

    // Encodes user passwords
    private final PasswordEncoder passwordEncoder;

    // Authenticates credentials via Spring Security
    private final AuthenticationManager authenticationManager;

    // JWT token generator & validator
    private final JWTService jwtService;

    /**
     * Registers a new user
     *
     * @param signUpRequestDto DTO containing name, email, password
     * @return saved user as UserDto
     */
    public UserDto signUp(SignUpRequestDto signUpRequestDto) {
        // Check if user already exists in DB
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if (user != null) {
            throw new RuntimeException("User is already present with this email ID.");
        }

        // Map DTO → Entity
        User newUser = modelMapper.map(signUpRequestDto, User.class);

        // Assign default role GUEST
        newUser.setRoles(Set.of(Role.GUEST));

        // Hash the password before storing
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));

        // Save user to DB
        newUser = userRepository.save(newUser);

        // Map Entity → DTO for response
        return modelMapper.map(newUser, UserDto.class);
    }

    /**
     * Authenticates a user and generates JWT tokens
     *
     * @param loginDto DTO containing email and password
     * @return String array: [accessToken, refreshToken]
     */
    public String[] login(LoginDto loginDto) {
        // Authenticate credentials using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        // ⚠ authentication.getPrincipal() should return your User entity
        User user = (User) authentication.getPrincipal();

        // Generate JWT access & refresh tokens
        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);   // short-lived token
        arr[1] = jwtService.generateRefreshToken(user);  // long-lived token

        return arr;
    }

    /**
     * Generates a new access token from a valid refresh token
     *
     * @param refreshToken JWT refresh token
     * @return new access token
     */
    public String refreshToken(String refreshToken) {
        // Extract user ID from refresh token
        Long id = jwtService.getUserIdFromToken(refreshToken);

        // Retrieve user from DB
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Generate new access token
        return jwtService.generateAccessToken(user);
    }
}
