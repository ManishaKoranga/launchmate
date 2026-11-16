package com.launchmate.identity.service;

import com.launchmate.identity.domain.User;
import com.launchmate.identity.repository.UserRepository;
import com.launchmate.shared.security.AuthenticatedUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountDetailsService implements UserDetailsService {

    private final UserRepository users;

    public UserAccountDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        User user = users.findByEmail(User.normaliseEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getFullName(), user.getPasswordHash());
    }
}
