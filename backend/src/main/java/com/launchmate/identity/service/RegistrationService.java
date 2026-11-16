package com.launchmate.identity.service;

import com.launchmate.identity.domain.User;
import com.launchmate.identity.dto.RegisterRequest;
import com.launchmate.identity.repository.UserRepository;
import com.launchmate.shared.exception.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = User.normaliseEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        return users.save(new User(email, passwordEncoder.encode(request.password()), request.fullName()));
    }
}
