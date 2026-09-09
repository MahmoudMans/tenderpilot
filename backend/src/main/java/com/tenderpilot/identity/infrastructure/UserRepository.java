package com.tenderpilot.identity.infrastructure;

import com.tenderpilot.identity.domain.UserEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository
        extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByNormalizedEmail(
            String normalizedEmail
    );


    boolean existsByNormalizedEmail(
            String normalizedEmail
    );

}
