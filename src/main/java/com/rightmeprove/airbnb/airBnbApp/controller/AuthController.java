package com.rightmeprove.airbnb.airBnbApp.controller;

import com.rightmeprove.airbnb.airBnbApp.dto.LoginDto;
import com.rightmeprove.airbnb.airBnbApp.dto.LoginResponseDto;
import com.rightmeprove.airbnb.airBnbApp.dto.SignUpRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.UserDto;
import com.rightmeprove.airbnb.airBnbApp.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Authentication controller that manages user registration, login, and token refresh.
 *
 * Routes:
 *  - POST /auth/signup → create a new user account
 *  - POST /auth/login → authenticate user & issue JWTs
 *  - POST /auth/refresh → refresh access token using HttpOnly cookie
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // Auto-generates constructor for final fields (injects AuthService)
public class AuthController {

    private final AuthService authService;

    /**
     * Handles new user signup.
     * @param signUpRequestDto user signup data
     * @return created UserDto (without sensitive info)
     */
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        return new ResponseEntity<>(authService.signUp(signUpRequestDto), HttpStatus.CREATED);
    }

    /**
     * Authenticates user credentials and issues JWT tokens.
     * - Access token returned in response body.
     * - Refresh token stored securely in an HttpOnly cookie.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginDto loginDto,
            HttpServletResponse httpServletResponse
    ) {
        // AuthService returns [0]=access token, [1]=refresh token
        String[] tokens = authService.login(loginDto);

        // Securely store refresh token in HttpOnly cookie
        Cookie cookie = new Cookie("refreshToken", tokens[1]);
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // accessible for all routes
        cookie.setMaxAge(60 * 60 * 24 * 30); // 30 days validity

        // In production, always enable the following:
        // cookie.setSecure(true); // send only via HTTPS
        // cookie.setSameSite("Strict"); // mitigate CSRF/token leakage

        httpServletResponse.addCookie(cookie);

        // Return access token (used for authenticated API calls)
        return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
    }

    /**
     * Generates a new access token using the refresh token from cookies.
     * @throws AuthenticationServiceException if refresh token is missing or invalid
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
        // Extract refresh token from cookies
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

        // Get a new access token using AuthService
        String accessToken = authService.refreshToken(refreshToken);

        // Return updated access token to client
        return ResponseEntity.ok(new LoginResponseDto(accessToken));
    }
}
