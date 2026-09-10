package com.tenderpilot.notification.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity>
    findAllByOrganizationIdOrderByActiveDescLastObservedAtDescCreatedAtDesc(
            UUID organizationId
    );


    Optional<NotificationEntity>
    findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );


    Optional<NotificationEntity>
    findByOrganizationIdAndFingerprint(
            UUID organizationId,
            String fingerprint
    );

}
