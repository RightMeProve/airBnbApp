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
 * Controller handling user authentication (signup/login).
 *
 * - /auth/signup → register a new user
 * - /auth/login → authenticate a user and return JWT tokens
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Signup endpoint
     * @param signUpRequestDto signup payload (name, email, password)
     * @return created user DTO
     */
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        return new ResponseEntity<>(authService.signUp(signUpRequestDto), HttpStatus.CREATED);
    }

    /**
     * Login endpoint
     * @param loginDto login payload (email, password)
     * @param httpServletResponse used to set HttpOnly refresh token cookie
     * @return LoginResponseDto containing the access token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginDto loginDto,
            HttpServletResponse httpServletResponse
    ) {
        // [0] = Access Token, [1] = Refresh Token
        String[] tokens = authService.login(loginDto);

        // Store refresh token in HttpOnly cookie (cannot be accessed by JS)
        Cookie cookie = new Cookie("refreshToken", tokens[1]);
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // important: ensures cookie is sent for all requests
        cookie.setMaxAge(60 * 60 * 24 * 30); // 30 days (matches refresh token validity)

        // In production, add:
        // cookie.setSecure(true); // only over HTTPS
        // cookie.setSameSite("Strict"); // prevent CSRF/token leakage

        httpServletResponse.addCookie(cookie);

        // Return access token in response body
        return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request){
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(()-> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDto(accessToken));
    }


}
