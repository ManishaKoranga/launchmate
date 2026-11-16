package com.launchmate.identity.controller;

import com.launchmate.identity.dto.LoginRequest;
import com.launchmate.identity.dto.RegisterRequest;
import com.launchmate.identity.dto.UserResponse;
import com.launchmate.identity.service.RegistrationService;
import com.launchmate.identity.service.SessionAuthenticator;
import com.launchmate.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final RegistrationService registrationService;
    private final SessionAuthenticator sessionAuthenticator;

    public AuthController(RegistrationService registrationService, SessionAuthenticator sessionAuthenticator) {
        this.registrationService = registrationService;
        this.sessionAuthenticator = sessionAuthenticator;
    }

    @Operation(summary = "Issue a CSRF token cookie (call once before the first state-changing request)")
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName());
    }

    @Operation(summary = "Create an account and start a session")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest body,
                                 HttpServletRequest request, HttpServletResponse response) {
        registrationService.register(body);
        return toResponse(sessionAuthenticator.login(body.email(), body.password(), request, response));
    }

    @Operation(summary = "Start a session")
    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest body,
                              HttpServletRequest request, HttpServletResponse response) {
        return toResponse(sessionAuthenticator.login(body.email(), body.password(), request, response));
    }

    @Operation(summary = "Current user")
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return toResponse(user);
    }

    private static UserResponse toResponse(AuthenticatedUser user) {
        return new UserResponse(user.id(), user.email(), user.fullName());
    }
}
