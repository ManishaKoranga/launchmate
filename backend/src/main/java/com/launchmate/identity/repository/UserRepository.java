package com.launchmate.identity.repository;

import com.launchmate.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String normalisedEmail);

    boolean existsByEmail(String normalisedEmail);
}
